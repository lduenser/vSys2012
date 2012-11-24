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
	
	public Double getPrice() {
		return this.price;
	}
	
	public String toString() {
		String info = "";
		
		if(this.type.equals(types.BID_PLACED.toString())) {
			info = "User " + this.userName + " placed bid " + this.price.toString() + " on auction " + this.auctionID;
		}
		if(this.type.equals(types.BID_OVERBID.toString())) {
			info = "User " + this.userName + " overbid recent bid on auction " + this.auctionID;
		}
		if(this.type.equals(types.BID_WON.toString())) {
			info = "User " + this.userName + " won auction " + this.auctionID + " with bid " + this.price.toString();
		}
		
		return this.getHead() + info;
	}

}
