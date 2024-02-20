package ticketing;

import java.util.Date;
import java.util.Scanner;

public class App {
	public static void main(String[] args) {
		Scanner scn = new Scanner(System.in);
		LoginManager loginMgr = new LoginManager();
		TicketingManager ticketingMgr = new TicketingManager();

		//영화 스케줄 추가함수
		//ticketingMgr.addSchedule();
		
		//로그인 정보 선언
		Client client = null;
		boolean isloginProcessing = true;
		
		//로그인 or 비회원 예매 or 회원 가입 or 종료
		while(isloginProcessing)
		{
			try {
				System.out.println("영화 예매를 위해 다음을 선택해주세요.");
				System.out.println("1.로그인 2.비회원 예매 3.회원가입 4.아이디 찾기 5.비밀번호 찾기 6.예매확인 7.나가기");
				int select = Integer.parseInt(scn.nextLine());
				
				switch(select) {
				case 1: //로그인
					client = loginMgr.login();
					
					if (client != null) {
						System.out.println("로그인 성공");
						isloginProcessing = false;
					} else {
						System.out.println("로그인 실패");						
					}
					break;
				case 2: //비회원 예매
					client = loginMgr.createNonMemberAccount();
					
					if (client != null) {
						System.out.println("비회원 로그인 성공");
						isloginProcessing = false;
					} else {
						System.out.println("비회원 로그인 실패");
					}
					break;
				case 3: //회원가입
					if (loginMgr.createMemberAccount()) {
						System.out.println("회원가입 성공");						
					} else {
						System.out.println("회원가입 실패");						
					}
					break;
				case 4: //아이디 찾기
					loginMgr.findMember_id();
					break;
				case 5: //비밀번호 찾기
					loginMgr.findPw();
					break;
				case 6: //예매확인
					ticketingMgr.findReservation();
					break;
				case 7: //나가기
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//예매
		
		//예매 가능한 영화의 날짜 및 시간, 예약 가능 좌석 수를 보여주기
		ticketingMgr.showReservationData();
		boolean isTicketingProcessing = true;
		
		while(isTicketingProcessing) {
			if (ticketingMgr.ticketing(client)) {
				isTicketingProcessing = false;
			} else {
				System.out.println("예매 실패");
			}
		}
	}
}
