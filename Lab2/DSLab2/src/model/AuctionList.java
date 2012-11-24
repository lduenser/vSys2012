package model;

import java.rmi.RemoteException;
import java.util.ArrayList;

import events.AuctionEvent;
import events.Event;
import events.UserEvent;

import server.AuctionServer;

public class AuctionList {
	static ArrayList<Auction> auctionList = new ArrayList<Auction>();
	
	public Auction addAuction(Auction auction) {
		auctionList.add(auction);
		auction.setId(auctionList.size());
		
		Event temp = new AuctionEvent(AuctionEvent.types.AUCTION_STARTED, auction.id, auction.getDuration());
		try {
			AuctionServer.analytics.processEvent(temp);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return auction;
	}
	
	public boolean updateList() {
		for (Auction auction:auctionList) {
			auction.checkOpen();
		}
		return true;
	}
	
	public String listAuctions(boolean all) {
		String temp = "";
		for (Auction auction:auctionList) {
			if(auction.checkOpen()) temp+=auction.toString() + "\r\n";
		}
		if(temp.equals("")) return "No active Auctions";
		return temp;
	}
	
	public Auction getAuctionById(int id) {
		
		if(id>auctionList.size()) return null;
		
		if(auctionList.get(id-1) != null) return auctionList.get(id-1);
		
		return null;
	}
}
