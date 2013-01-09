package model;

import java.util.ArrayList;

public class SignedBidList {
	
	ArrayList<SignedBid> signedBids = null;
	
	public SignedBidList() {
		signedBids = new ArrayList<SignedBid>();
	}
	
	public void addBid(SignedBid signedBid) {
		signedBids.add(signedBid);
	}
	
	public void removeBid(SignedBid signedBid) {
		signedBids.remove(signedBid);
	}
	
	public SignedBid getFirstBid() {
		if(!signedBids.isEmpty()) return signedBids.get(0);
		else return null;
	}
	
	public int getSize() {
		return signedBids.size();
	}
	public SignedBid getBidByBid(int auctionId, double bid) {
		for(SignedBid signedBid:signedBids) {
			if(signedBid.getMoney().equals(bid) && signedBid.getAuction() == auctionId) {
				return signedBid;
			}
		}
		
		return null;
	}
	
	public boolean isEmpty() {
		return signedBids.isEmpty();
	}
	
	public void clear() {
		signedBids.clear();
	}
	
	public void updateBid(SignedBid bid) {
		for(SignedBid signedBid:signedBids) {
			if(signedBid.getMoney()==bid.getMoney() && signedBid.getAuction() == bid.getAuction()) {
				signedBids.set(signedBids.indexOf(signedBid), bid);
			}
		}
	}
}
