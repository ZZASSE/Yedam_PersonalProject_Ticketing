package ticketing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
	private String seat_id;
	private String seatgrade_name;
	private int price;
	private int reserve_id; //해당 좌석 정보를 담고 있는 예매_id
	private boolean isReserved;
}
