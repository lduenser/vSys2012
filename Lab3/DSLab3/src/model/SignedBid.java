package model;

public class SignedBid extends Bid {
	
	int auctionId = 0;
	Timestamp timestamp1 = null;
	Timestamp timestamp2 = null;
	double meanTimestamp = 0d;
	
	boolean complete = false;
	
	public SignedBid(User user, Double money, int auctionId) {
		super(user, money);
		this.auctionId = auctionId;
	}
	
	public double getMeanTimestamp() {
		if(meanTimestamp==0) {
			calculateMeanTimestamp();
		}		
		return meanTimestamp;
	}
	
	public int getAuction() {
		return auctionId;
	}
	
	void calculateMeanTimestamp() {
		double result = 0d;
		
		result = (timestamp1.getTimestamp() + timestamp2.getTimestamp())/2;
		
		if(result>0) meanTimestamp = result;
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
