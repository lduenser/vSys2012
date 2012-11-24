package events;

public class AuctionEvent extends Event{

	long auctionID;
	int duration;
	
	public enum types {
		AUCTION_STARTED,
		AUCTION_ENDED
	}
	
	public AuctionEvent(types type, long auctionID, int duration) {
		super();
		
		this.type = type.toString();
		this.auctionID = auctionID;
		this.duration = duration;
	}
	
	public int getDuration() {
		return this.duration;
	}
	
	public String toString() {
		String info = "";
		
		if(this.type.equals(types.AUCTION_STARTED.toString())) {
			info = "Auction " + id + " started";
		}
		if(this.type.equals(types.AUCTION_ENDED.toString())) {
			info = "Auction " + id + " ended";
		}
		
		return this.getHead() + info;
	}
}
