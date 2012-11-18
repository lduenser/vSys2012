package events;

public class BidEvent extends Event{
	
	String userName;
	long auctionID;
	Double price;
	
	enum types {
		BID_PLACED,
		BID_OVERBID,
		BID_WON
	}
	
	/*
	 * type is either of:
	 * - BID_PLACED
	 * - BID_OVERBID
	 * - BID_WON
	 */

}
