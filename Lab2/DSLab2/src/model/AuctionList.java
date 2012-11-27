package model;

import java.rmi.RemoteException;
import java.util.ArrayList;

import debug.Debug;

import events.AuctionEvent;
import events.Event;

import server.AuctionServer;

public class AuctionList {
	static ArrayList<Auction> auctionList = new ArrayList<Auction>();
	
	public Auction addAuction(Auction auction) {
		auctionList.add(auction);
		auction.setId(auctionList.size());
		
		Event temp = new AuctionEvent(AuctionEvent.types.AUCTION_STARTED, auction.id, auction.getDuration());
		try {
			if(AuctionServer.analytics != null){
				AuctionServer.analytics.processEvent(temp);
			}
			else{
				Debug.printError("no communication with AnalyticsServer");
			}
			
		} catch (RemoteException e) {			
			e.printStackTrace();
		}
		return auction;
	}
	
	public synchronized boolean updateList() {
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
