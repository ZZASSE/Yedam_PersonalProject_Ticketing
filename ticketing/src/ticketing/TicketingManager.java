package ticketing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class TicketingManager {
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
		
		//영화 스케줄표 생성 함수
		public void addSchedule() {
			conn = DAO.getConn();
			
			//Seats테이블에서 모든 seat_id를 가져와서 list에 보관시키기
			List<String> seats_idList = new ArrayList<String>();
			sql = "SELECT seat_id,"
					+ "   seatgrade_id"
					+ " FROM Seats";
			
			try {
				psmt = conn.prepareStatement(sql);
				
				rs = psmt.executeQuery();
				
				while(rs.next()) {
					seats_idList.add(rs.getString("seat_id"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				disconn();
			}
			
			for(int i = 0; i < seats_idList.size(); ++i)
			{
				conn = DAO.getConn();
				
				sql = "INSERT INTO Theater(reserve_id,\r\n"
						+ "                    upload_date,\r\n"
						+ "                    unload_date,\r\n"
						+ "                    reserve_date,\r\n"
						+ "                    movie_id,\r\n"
						+ "                    seat_id)\r\n"
						+ "VALUES             (reserve_id_seq.NEXTVAL,\r\n"
						+ "                    TO_DATE('2024-02-20 00:00', 'YYYY-MM-DD HH24:MI'),\r\n"
						+ "                    TO_DATE('2024-03-20 23:59', 'YYYY-MM-DD HH24:MI'),\r\n"
						+ "                    TO_DATE('2024-03-02 18:00', 'YYYY-MM-DD HH24:MI'),\r\n"
						+ "                    1,\r\n"
						+ "                    ?)";
				
				try {
					psmt = conn.prepareStatement(sql);
					psmt.setString(1, seats_idList.get(i));
					int r = psmt.executeUpdate();
					
					if (r > 0) {
						continue;
					}else {
						System.out.println("오류 발생");
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					disconn();
				}
				
			}
		}

		////예매 가능한 영화의 날짜 및 시간, 예약 가능 좌석 수를 보여주기
		public void showReservationData() {
			conn = DAO.getConn();
			sql = "SELECT DISTINCT       m.name,"
					+ "                  TO_CHAR(reserve_date, 'YYYY-MM-DD HH24:MI'),"
					+ "                  COUNT(*)"
					+ " FROM             Theater t JOIN Movies m"
					+ "                            ON (t.movie_id = m.movie_id)"
					+ " WHERE            t.reserve_id NOT IN (SELECT reserve_id"
					+ "                                       FROM   Reservations)"
					+ "  GROUP BY        t.reserve_date, m.name"
					+ "  ORDER BY        1";
			
			try {
				psmt = conn.prepareStatement(sql);
				rs = psmt.executeQuery();
				System.out.printf("%-10s | %-14s | %-8s \n", "영화", "예매 가능 날짜", "빈 좌석 수");
				System.out.println("===========================================");
				while(rs.next()) {
					System.out.printf("%-10s | %-14s | %-8s \n", rs.getString(1), rs.getString(2), rs.getString(3));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				disconn();
			}
		}
		
		//예매
		public boolean ticketing(Client _client) {
			conn = DAO.getConn();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Scanner scn = new Scanner(System.in);
			String font_Red = "\u001B[31m";
		    String font_Reset = "\u001B[0m";
		    
			//예매를 희망하는 영화,시간, 좌석의 모든 좌석 정보 저장하기
			String movieName = "";
			Seat seat = new Seat();
			Date date = new Date();
			
			List<Seat> seats = new ArrayList<Seat>();
			sql = "SELECT      s.seat_id,"
					+ "        g.name,"
					+ "        g.price,"
					+ "        t.reserve_id"
					+ " FROM   Theater t JOIN Movies m"
					+ "                   ON (t.movie_id = m.movie_id)"
					+ "                 JOIN Seats s"
					+ "                   ON (t.seat_id = s.seat_id)"
					+ "                 JOIN SeatGrade g"
					+ "                   ON (s.seatgrade_id = g.seatgrade_id)"
					+ " WHERE  t.reserve_date = TO_DATE(?, 'YYYY-MM-DD HH24:MI')"
					+ " AND    m.name = ?";
			
			try {
				System.out.println("예매를 원하시는 영화와 날짜를 선택해주세요.");
				System.out.println("영화선택> ");
				movieName = scn.nextLine();
				System.out.println("날짜선택> 예: 0000-00-00 00:00");
				date = sdf.parse(scn.nextLine());
						
				psmt = conn.prepareStatement(sql);
				psmt.setString(1, sdf.format(date));
				psmt.setString(2, movieName);
				
				rs = psmt.executeQuery();
				
				while(rs.next()) {
					seats.add(new Seat(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4), false));
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//위에서 담은 예매 희망 특정 시간, 영화의 모든 좌석 정보 중 예약된 좌석들만 true시켜주기
			sql = "SELECT     r.reservation_id,"
					+ "       m.name,"
					+ "       t.reserve_date,"
					+ "       t.seat_id"
					+ " FROM   Reservations r JOIN Theater t"
					+ "                        ON (r.reserve_id = t.reserve_id)"
					+ "                      JOIN Movies m"
					+ "                        ON (t.movie_id = m.movie_id)"
					+ "                      JOIN Seats s"
					+ "                        ON (t.seat_id = s.seat_id)"
					+ " WHERE  m.name = ?"
					+ " AND    t.reserve_date = TO_DATE(?, 'YYYY-MM-DD HH24:MI')";
			
			try {
				psmt = conn.prepareStatement(sql);
				psmt.setString(1, movieName);
				psmt.setString(2, sdf.format(date));
				
				rs = psmt.executeQuery();
				
				while(rs.next()) {
					for(int i = 0; i < seats.size(); ++i) {
						if (seats.get(i).getSeat_id().equals(rs.getString(4))) {
							seats.get(i).setReserved(true);
							break;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				disconn();
			}
			
			//좌석 표시해주기
			if (seats != null && seats.size() > 0) {
				System.out.println("앞");
				String seatStr = "";
				for(int i = 0; i < seats.size(); ++i) {
					
					//좌석 열 비교문자가 비어있다면 최초 한번 담아주기
					if (seatStr.equals("")) {
						seatStr = seats.get(i).getSeat_id().substring(0, 1);
					}
					
					//다음 좌석 열로 내려간다면 줄바꿈해주기
					if (!seatStr.equals(seats.get(i).getSeat_id().subSequence(0, 1))) {
						seatStr = seats.get(i).getSeat_id().substring(0, 1);
						System.out.println();
					}
					
					//해당 좌석이 예약되어있다면 빨간색으로 표시해주기
					if(seats.get(i).isReserved()) {
						System.out.printf(font_Red + "%5s" + font_Reset, seats.get(i).getSeat_id());					
					} else {
						System.out.printf("%5s", seats.get(i).getSeat_id());
					}
				}
				System.out.println("\n뒤");

				//좌석 선택
				boolean isChoosedSeatProcess = true;
				while(isChoosedSeatProcess) {
					System.out.println("좌석선택> ");
					String inputSeat = scn.nextLine();

					for(int i = 0; i < seats.size(); ++i) {
						if (seats.get(i).getSeat_id().equals(inputSeat) &&
								seats.get(i).isReserved() == false) {
							seat = seats.get(i);
							isChoosedSeatProcess = false;
							break;
						}
					}
					
					if (isChoosedSeatProcess) {
						System.out.println("없는 좌석이거나 이미 예약된 좌석입니다.");						
					}
				}
				//선택된 좌석의 가격 정보를 가져와서 결제를 한다.
				
				//티켓 가격
				int ticketPrice = seat.getPrice();

				//회원 or 비회원 유무 파악
				if (_client.getMember_id() != null && !_client.getMember_id().equals("")) {

					//마일리지 사용유무 파악 및 사용
					if (_client.getMileage() > 0) {
						boolean useMileageProcess = true;
						while(useMileageProcess) {
							try {
								System.out.println("사용 할 수 있는 마일리지" + _client.getMileage());
								System.out.println("사용하시겠습니까? 1.예 2.아니오");
								int menuInput = Integer.parseInt(scn.nextLine());
								
								switch(menuInput) {
								case 1:
									//실제 마일리지 차감시키기
									if (useMileage(_client)) {
										//티켓가격 차감시키기
										ticketPrice -= _client.getMileage();
										System.out.println("할인된 예매 가격: " + seat.getPrice() + " => " + ticketPrice);
										useMileageProcess = false;
									} else {
										System.out.println("마일리지 차감 실패");
									}
									break;
								case 2:
									useMileageProcess = false;
									break;
								}								
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				
				//결제
				boolean isPaymentProcess = true;
				while(isPaymentProcess) {
					System.out.println("결제 시도");
					
					if (payment(ticketPrice)) {
						isPaymentProcess = false;
					} else {
						System.out.println("결제 실패");
					}
				}
				
				//영화 예매 정보 생성 (Reservations.reservation_id = 시퀀스 자동 부여, Clients.client_id, Theater.reserve_id)
				if (reservation(_client.getClient_id(), seat.getReserve_id())) {
					System.out.println("=================================");
					System.out.println("고객 이름: " + _client.getName());
					System.out.println("예매 번호: " + seat.getReserve_id());
					System.out.println("예매된 영화 이름: " + movieName);
					System.out.println("예약 날짜 및 시간: " + sdf.format(date));
					System.out.println("좌석: " + seat.getSeat_id());
					System.out.println("=================================");
					
					return true;
				} else {
					System.out.println("예매 실패: 알 수 없는 오류");
					System.out.println("결제된 금액 및 마일리지 복구 프로세스 실행 (미구현)");
				}
				
			} else {
				System.out.println("해당하는 날짜 및 시간에 상영하는 영화정보가 없습니다.");
			}
			
			return false;
		}
		
		//마일리지 차감함수
		public boolean useMileage(Client _client) {
			//sql의 Members테이블의 member_id와 _client의 member_id를 비교 후, 마일리지 차감시키기
			conn = DAO.getConn();
			
			sql = "UPDATE      Members"
					+ " SET    mileage = mileage - ?"
					+ " WHERE  member_id = ?";
			try {
				psmt = conn.prepareStatement(sql);
				psmt.setInt(1, _client.getMileage());
				psmt.setString(2, _client.getMember_id());
				
				int r = psmt.executeUpdate();
				
				if (r > 0) {
					System.out.println("마일리지 차감 완료");
					return true;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				disconn();
			}
			return false;
		}
		
		//티켓 결제 함수
		public boolean payment(int _ticketPrice) {
			Scanner scn = new Scanner(System.in);
			try {
				System.out.println("결제 할 금액: " + _ticketPrice);
				System.out.println("결제하시려면 알맞은 금액을 입력해주세요.");
				String inputPrice = scn.nextLine(); 
				
				//입력한 가격값과 비교 후 결제
				if (inputPrice.equals(Integer.toString(_ticketPrice))) {
					System.out.println("결제 완료");
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
		
		//영화 예약 정보 생성 함수
		public boolean reservation(int _client_id, int _reserve_id) {
			conn = DAO.getConn();
			sql = "INSERT INTO Reservations(reservation_id,"
					+ "                         client_id,"
					+ "                         reserve_id)"
					+ " VALUES                  (reservation_id_seq.NEXTVAL,"
					+ "                         ?,"
					+ "                         ?)";
			
			try {
				System.out.println("예매 시도");
				psmt = conn.prepareStatement(sql);
				psmt.setInt(1, _client_id);
				psmt.setInt(2, _reserve_id);
				
				int r = psmt.executeUpdate();
				
				if (r > 0) {
					System.out.println("예매 완료");
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}  finally {
				disconn();
			}
			return false;
		}
		
		//예매정보 확인 함수
		public void findReservation() {
			Scanner scn = new Scanner(System.in);
			conn = DAO.getConn();
			
			System.out.println("이메일 입력> ");
			String inputEmail = scn.nextLine();
			
			sql = "SELECT      reservation_id,"
					+ "        c.name,"
					+ "        c.tel,"
					+ "        TO_CHAR(t.reserve_date, 'YYYY-MM-DD HH24:MI'),"
					+ "        m.name,"
					+ "        s.seat_id,"
					+ "        sg.name"
					+ " FROM   Reservations r JOIN Clients c"
					+ "                        ON (r.client_id = c.client_id)"
					+ "                      JOIN Theater t"
					+ "                        ON (r.reserve_id = t.reserve_id)"
					+ "                      JOIN Movies m"
					+ "                        ON (t.movie_id = m.movie_id)"
					+ "                      JOIN Seats s"
					+ "                        ON (t.seat_id = s.seat_id)"
					+ "                      JOIN SeatGrade sg"
					+ "                        ON (s.seatgrade_id = sg.seatgrade_id)"
					+ " WHERE  c.email = ?";
			
			try {
				psmt = conn.prepareStatement(sql);
				psmt.setString(1, inputEmail);
				
				rs = psmt.executeQuery();
				
				System.out.println("===========================");

				while(rs.next()) {
					System.out.println("예매번호: " + rs.getInt(1));
					System.out.println("예약자명: " + rs.getString(2));
					System.out.println("연락처: " + rs.getString(3));
					System.out.println("날짜 및 시간: " + rs.getString(4));
					System.out.println("영화 이름: " + rs.getString(5));
					System.out.println("좌석: " + rs.getString(6));
					System.out.println("좌석 등급: " + rs.getString(7));
					System.out.println("===========================");
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				disconn();
			}
		}
}
