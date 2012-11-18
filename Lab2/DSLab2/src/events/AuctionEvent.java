package events;

public class AuctionEvent extends Event{

	long auctionID;
	
	enum types {
		AUCTION_STARTED,
		AUCTION_ENDED
	}
	
	/*
	 * type is either of:
	 * - AUCTION_STARTED
	 * - AUCTION_ENDED
	 */
}
