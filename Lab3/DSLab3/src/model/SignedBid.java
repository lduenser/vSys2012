package model;

public class SignedBid extends Bid {
	
	int auctionId = 0;
	Timestamp timestamp1 = null;
	Timestamp timestamp2 = null;
	String meanTimestamp = null;
	
	boolean complete = false;
	
	public SignedBid(User user, Double money, int auctionId) {
		super(user, money);
		this.auctionId = auctionId;
	}
	
	public Timestamp getTimestamp(int id) {
		switch(id) {
		case 1: return timestamp1;
		case 2: return timestamp2;
		}
		return null;
	}
	
	public String getMeanTimestamp() {
		if(meanTimestamp==null) {
			calculateMeanTimestamp();
		}		
		return meanTimestamp;
	}
	
	public int getAuction() {
		return auctionId;
	}
	
	void calculateMeanTimestamp() {
		long result = 0;
		
		result = (Long.parseLong(timestamp1.getTimestamp()) + Long.parseLong(timestamp2.getTimestamp()))/2;
		
		if(result>0) meanTimestamp = Long.toString(result);
	}
	
	public void addTimestamp(Timestamp stamp) {
		
		if(timestamp1==null) timestamp1 = stamp;
		else {
			timestamp2 = stamp;
			complete = true;
		} 
	}
	
	public boolean getComplete() {
		return complete;
	}
	
}
