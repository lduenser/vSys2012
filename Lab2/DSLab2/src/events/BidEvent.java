package events;

import events.AuctionEvent.types;

public class BidEvent extends Event{
	
	String userName;
	long auctionID;
	Double price;
	
	public enum types {
		BID_PLACED,
		BID_OVERBID,
		BID_WON
	}
	
	public BidEvent(types type, String userName, long auctionID, Double price) {
		super();
		
		this.type = type.toString();
		this.userName = userName;
		this.auctionID = auctionID;
		this.price = price;
	}
	
	/*
	 * type is either of:
	 * - BID_PLACED
	 * - BID_OVERBID
	 * - BID_WON
	 */

}
