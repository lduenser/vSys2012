package model;

import java.security.Signature;

public class Timestamp {

	String timestamp;
	int auctionId;
	double price;
	String user;
	String signature;
	
	public Timestamp(long timestamp, int aucitonId, double price, String user, String signature) {
		setTimestamp(timestamp);
		setAuctionId(auctionId);
		setPrice(price);
		setSignature(signature);
		setUser(user);
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = Long.toString(timestamp);
	}
	public int getAuctionId() {
		return auctionId;
	}
	public void setAuctionId(int auctionId) {
		this.auctionId = auctionId;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
	
}
