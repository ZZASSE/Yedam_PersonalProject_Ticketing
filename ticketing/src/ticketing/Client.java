package ticketing;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Client {
	private int client_id;
	private String name;
	private String tel;
	private String email;
	private Date birth;
	private Date create_Date;
	private String member_id;
	private String pw;
	private int mileage;
	private int memberGrade_id;
	
	
}
