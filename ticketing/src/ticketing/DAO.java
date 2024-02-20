package ticketing;

import java.sql.Connection;
import java.sql.DriverManager;

//Connection: DB 연결 객체
public class DAO {
	public static Connection conn;
	
	public static Connection getConn() {
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "Personal", "123");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
}
