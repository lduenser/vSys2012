package events;

public class BidEvent extends Event{
	
	String userName;
	long auctionID;
	Double price;
	
	/*
	 * type is either of:
	 * - BID_PLACED
	 * - BID_OVERBID
	 * - BID_WON
	 */

}
