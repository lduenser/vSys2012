package model;

import java.security.Signature;

public class Timestamp {

	double timestamp;
	int auctionId;
	double price;
	User user;
	Signature signature;
	
	public Timestamp(double timestamp, int aucitonId, double price, String signature) {
		setTimestamp(timestamp);
		setAuctionId(auctionId);
		setPrice(price);
		//setUser(user);
	}
	
	public double getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(double timestamp) {
		this.timestamp = timestamp;
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
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
}
