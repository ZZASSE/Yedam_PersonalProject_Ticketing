package ticketing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

//DB처리 기능
public class LoginManager {
	//필드
	private Connection conn;
	private PreparedStatement psmt;
	private ResultSet rs;
	private String sql;
	
	//Connection 객체 초기화
	void disconn() {
		try {
			if (conn != null) {
				conn.close();
			}
			if (psmt != null) {
				psmt.close();
			}
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//로그인
	public Client login() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Scanner scn = new Scanner(System.in);
		conn = DAO.getConn();
		
		//아이디 비밀번호 입력 (Members 테이블의 member_id 및 pw와 비교)
		System.out.println("아이디입력>");
		String id = scn.nextLine();
		System.out.println("비밀번호입력>");
		String pw = scn.nextLine();

		System.out.println(id + " " + pw);
		System.out.println("조회 중>");
		
		//쿼리 진행
		sql = "SELECT c.client_id,\r\n"
				+ "       c.name,\r\n"
				+ "       c.tel,\r\n"
				+ "       c.email,\r\n"
				+ "       c.birth,\r\n"
				+ "       c.create_date,\r\n"
				+ "       c.member_id,\r\n"
				+ "       m.pw,\r\n"
				+ "       m.mileage,\r\n"
				+ "       m.membergrade_id\r\n"
				+ "FROM   Clients c JOIN Members m\r\n"
				+ "                   ON (c.member_id = m.member_id)\r\n"
				+ "WHERE  c.member_id = ?\r\n"
				+ "AND    m.pw = ?";
		
		try {
			psmt = conn.prepareStatement(sql);
			psmt.setString(1, id);
			psmt.setString(2, pw);
			
			rs = psmt.executeQuery();
			
			
			if(rs.next()) {
				Client client = new Client();
				client.setClient_id(rs.getInt("client_id"));
				client.setName(rs.getString("name"));
				client.setTel(rs.getString("tel"));
				client.setEmail(rs.getString("email"));
				client.setBirth(rs.getDate("birth"));
				client.setCreate_Date(rs.getDate("create_date"));
				client.setMember_id(rs.getString("member_id"));
				client.setPw(rs.getString("pw"));
				client.setMileage(rs.getInt("mileage"));
				client.setMemberGrade_id(rs.getInt("membergrade_id"));
				
				return client;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disconn();
		}
		return null;
	}
	
	//비회원 
	public Client createNonMemberAccount() {
		//Clients테이블에 새로운 비회원 정보 추가 => member_id 가 비어있다는 뜻
		Scanner scn = new Scanner(System.in);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		conn = DAO.getConn();
		
		try {
			//비회원 정보 입력받아서 member_id를 제외한 Client 객체 생성
			System.out.println("(필수)이름입력> ");
			String name = scn.nextLine();
			System.out.println("(필수)전화번호입력> ");
			String tel = scn.nextLine();
			
			//이메일 중복 검사
			String email = "";
			while(true) {
				System.out.println("(필수)이메일입력> ");
				email = scn.nextLine();
				
				if (checkEmailIsUnique(email)) {
					break;
				} else {
					System.out.println("중복된 이메일입니다. 다시 입력해주세요.");
				}
			}
			
			System.out.println("(필수)출생일자입력> 예 : 1900-09-09");
			String birth = scn.nextLine();
			
			//sql작업
			sql = "INSERT INTO Clients(client_id,\r\n"
					+ "                    name,\r\n"
					+ "                    tel,\r\n"
					+ "                    email,\r\n"
					+ "                    birth)\r\n"
					+ "VALUES             (client_id_seq.NEXTVAL,\r\n"
					+ "                    ?,\r\n"
					+ "                    ?,\r\n"
					+ "                    ?,\r\n"
					+ "                    ?)";
			
			psmt = conn.prepareStatement(sql);
			psmt.setString(1, name);
			psmt.setString(2, tel);
			psmt.setString(3, email);
			psmt.setString(4, birth);
			
			int r = psmt.executeUpdate();
			if (r > 0) {
				//비회원 정보를 추가하는데 성공했다면 생성된 정보들을 모두 불러와서 Client객체에 넣어주고 반환
				sql = "SELECT client_id,\r\n"
						+ "       name,\r\n"
						+ "       tel,\r\n"
						+ "       email,\r\n"
						+ "       birth,\r\n"
						+ "       create_date\r\n"
						+ "FROM   Clients\r\n"
						+ "WHERE  email = ?";
				
				psmt = conn.prepareStatement(sql);
				psmt.setString(1,  email);
				
				rs = psmt.executeQuery();
				if (rs.next()) {
					Client nonMember = new Client();
					nonMember.setClient_id(rs.getInt("client_id"));
					nonMember.setName(rs.getString("name"));
					nonMember.setTel(rs.getString("tel"));
					nonMember.setEmail(rs.getString("email"));
					nonMember.setBirth(rs.getDate("birth"));
					nonMember.setCreate_Date(rs.getDate("create_date"));
					
					return nonMember;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disconn();
		}
		return null;
	}
	//이메일 중복 검사 : 중복된 것이 있다면 false반환, 없으면 true 반환
	public boolean checkEmailIsUnique(String _email) {
		conn = DAO.getConn();
		sql = "SELECT email\r\n"
				+ "FROM   Clients\r\n"
				+ "WHERE  email = ?";
		
		try {
			psmt = conn.prepareStatement(sql);
			psmt.setString(1, _email);
			
			rs = psmt.executeQuery();
			if (rs.next()) {
				return false;
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	//회원가입
	public boolean createMemberAccount() {
		//입력값을 받아서 Clients정보와 Members정보를 생성 및 테이블에 저장
		Scanner scn = new Scanner(System.in);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		conn = DAO.getConn();
		
		try {
			//아이디 중복검사
			String member_id = "";
			while(true) {
				System.out.println("(필수)아이디입력> ");
				member_id = scn.nextLine();
				if (checkMemberIdIsUnique(member_id)) {
					break;
				} else {
					System.out.println("중복된 아이디입니다. 다시 입력해주세요.");
				}
			}
			System.out.println("(필수)비밀번호입력> ");
			String pw = scn.nextLine();
			System.out.println("(필수)이름입력> ");
			String name = scn.nextLine();
			System.out.println("(필수)전화번호입력> ");
			String tel = scn.nextLine();
			
			//이메일 중복 검사
			String email = "";
			while(true) {
				System.out.println("(필수)이메일입력> ");
				email = scn.nextLine();
				
				if (checkEmailIsUnique(email)) {
					break;
				} else {
					System.out.println("중복된 이메일입니다. 다시 입력해주세요.");
				}
			}
			
			System.out.println("(필수)출생일자입력> 예 : 1900-09-09");
			String birth = scn.nextLine();
			
			Client client = new Client();
			client.setMember_id(member_id);
			client.setPw(pw);
			client.setName(name);
			client.setTel(tel);
			client.setEmail(email);
			client.setBirth(sdf.parse(birth));
			
			sql = "INSERT ALL\r\n"
					+ "INTO Members(member_id,\r\n"
					+ "             pw,\r\n"
					+ "             membergrade_id)\r\n"
					+ "VALUES      (?,\r\n"
					+ "             ?,\r\n"
					+ "            (SELECT MIN(membergrade_id)\r\n"
					+ "                    FROM   MemberGrade))\r\n"
					+ "INTO Clients(client_id,\r\n"
					+ "                    name,\r\n"
					+ "                    tel,\r\n"
					+ "                    email,\r\n"
					+ "                    birth,\r\n"
					+ "                    member_id)\r\n"
					+ "VALUES             (client_id_seq.NEXTVAL,\r\n"
					+ "                    ?,\r\n"
					+ "                    ?,\r\n"
					+ "                    ?,\r\n"
					+ "                    ?,\r\n"
					+ "                    ?)\r\n"
					+ "SELECT *\r\n"
					+ "FROM   DUAL";
			
			psmt = conn.prepareStatement(sql);
			psmt.setString(1, client.getMember_id());
			psmt.setString(2, client.getPw());
			psmt.setString(3, client.getName());
			psmt.setString(4, client.getTel());
			psmt.setString(5, client.getEmail());
			psmt.setString(6, sdf.format(client.getBirth()));
			psmt.setString(7, client.getMember_id());
			
			int r = psmt.executeUpdate();
			
			if (r > 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disconn();
		}
		return false;
	}
	
	//아이디 중복 검사 : 중복된 것이 있다면 false반환, 없으면 true 반환
	public boolean checkMemberIdIsUnique(String _member_id) {
			conn = DAO.getConn();
			sql = "SELECT member_id\r\n"
					+ "FROM   Members\r\n"
					+ "WHERE  member_id = ?";
			
			try {
				psmt = conn.prepareStatement(sql);
				psmt.setString(1, _member_id);
				
				rs = psmt.executeQuery();
				if (rs.next()) {
					return false;
				}	
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
	}
		
	//이메일 인증
	public boolean ContactEmail(String _email) {
		Scanner scn = new Scanner(System.in);
		conn = DAO.getConn();

		
		
		sql = "SELECT      c.email,"
				+ "        c.member_id"
				+ " FROM   Clients c JOIN Members m"
				+ "                    ON (c.member_id = m.member_id)"
				+ " WHERE  c.email = ?"
				+ " AND    c.member_id IS NOT NULL";
		try {
			psmt = conn.prepareStatement(sql);
			psmt.setString(1, _email);
			
			rs = psmt.executeQuery();
			
			while(rs.next()) {
				//이메일 발송 및 인증
				
				String randomCode = "";
				for(int i = 0; i < 4; ++i) {
					int randValue = (int)(Math.random() * 10);
					randomCode += randValue;
				}
				
				MailSendManager msr = new MailSendManager();
				
				System.out.println("이메일 발송 중> ");
				
				if (msr.Send(_email, randomCode)) {
					System.out.println("발송된 인증코드를 입력> ");
					String inputCode = scn.nextLine();
					
					if (inputCode.equals(randomCode)) {
						System.out.println("인증 완료");
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disconn();
		}
		System.out.println("인증 실패");
		return false;
	}
	//아이디 찾기 (메일 인증 방식)
	public void findMember_id() {
		Scanner scn = new Scanner(System.in);

		System.out.println("회원가입 시 사용한 이메일 주소 입력> (해당 이메일로 인증코드가 발송됩니다.)");
		String inputEmail = scn.nextLine();
		
		if (ContactEmail(inputEmail)) {
			conn = DAO.getConn();
		
			sql = "SELECT      m.member_id"
					+ " FROM   Members m JOIN Clients c"
					+ "                    ON (m.member_id = c.member_id)"
					+ " WHERE  c.email = ?";
			
			try {
				psmt = conn.prepareStatement(sql);
				psmt.setString(1, inputEmail);
				
				rs = psmt.executeQuery();
				
				if(rs.next()) {
					System.out.println("아이디: " + rs.getString(1));
				} else {
					System.out.println("해당하는 아이디 정보가 없습니다.");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				disconn();
			}	
		} else {
			return;
		}
	}
	
	//비밀번호 찾기 및 변경(아이디로)
	public boolean findPw() {
		Scanner scn = new Scanner(System.in);
		conn = DAO.getConn();
		boolean isCorrect = false;
		
		System.out.println("아이디 입력> ");
		String inputId = scn.nextLine();
		
		sql = "SELECT      c.name,"
				+ "        c.tel,"
				+ "        c.email"
				+ " FROM   Clients c JOIN Members m"
				+ "                    ON (c.member_id = m.member_id)"
				+ " WHERE  c.member_id = ?";
		
		try {
			System.out.println("조회 중> ");
			psmt = conn.prepareStatement(sql);
			psmt.setString(1, inputId);
			
			rs = psmt.executeQuery();
			
			if (rs.next()) {
				//이름, 전화번호, 이메일이 맞는지 교차 검증
				System.out.println("계정을 찾았습니다. 비밀번호 변경을 위해 추가적인 정보를 입력해주세요.");
				System.out.println("이름입력> ");
				String inputName = scn.nextLine();
				System.out.println("전화번호 입력> (예: 000-0000-0000)");
				String inputTel = scn.nextLine();
				System.out.println("이메일 입력> (예: example@email.com)");
				String inputEmail = scn.nextLine();
				
				if (rs.getString(1).equals(inputName) &&
					rs.getString(2).equals(inputTel) &&
					rs.getString(3).equals(inputEmail)) {
					isCorrect = true;
				} else {
					System.out.println("사용자 정보가 일치하지 않습니다.");
				}
			} else {
				System.out.println("해당하는 계정을 찾을 수 없습니다.");
				disconn();
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//비밀번호 변경
		if (isCorrect) {
			System.out.println("변경하고자 하는 비밀번호 입력> ");
			String newPw = scn.nextLine();
			
			sql = "UPDATE      Members"
					+ " SET    pw = ?"
					+ " WHERE  member_id = ?";
			
			try {
				System.out.println("변경 중> ");
				psmt = conn.prepareStatement(sql);
				psmt.setString(1, newPw);
				psmt.setString(2, inputId);
				
				int r = psmt.executeUpdate();
				
				if (r > 0) {
					System.out.println("비밀번호 변경 완료");
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				disconn();
			}
		}
		System.out.println("비밀번호 변경 실패");
		return false;
	}
}
