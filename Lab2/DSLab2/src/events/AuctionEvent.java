package events;

public class AuctionEvent extends Event{

	long auctionID;
	
	public enum types {
		AUCTION_STARTED,
		AUCTION_ENDED
	}
	
	public AuctionEvent(types type, long auctionID) {
		super();
		
		this.type = type.toString();
		this.auctionID = auctionID;
	}
	
	/*
	 * type is either of:
	 * - AUCTION_STARTED
	 * - AUCTION_ENDED
	 */
}
