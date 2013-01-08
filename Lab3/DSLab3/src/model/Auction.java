package model;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import server.AuctionServer;
import server.DataHandler;

import debug.Debug;
import events.AuctionEvent;
import events.BidEvent;
import events.Event;


public class Auction {
	
	int id = 0;

	private String name = null;
	
	private User owner;

	private Date endDate = null;
	
	private Bid highest = null;
	
	private Boolean open = false;
	
	private int duration = 0;
	
	public Auction(String name, User owner, int seconds) {
		this.name = name;
		this.endDate = new Date(System.currentTimeMillis() + seconds*1000);
		this.owner = owner;
		this.open = true;
		this.highest = new Bid(owner, 0.00);
		this.duration = seconds;
	}
	
	public int getDuration() {
		return duration;
	}
	
	Bid getHighestBid() {
		return highest;
	}
	
	public boolean checkOpen() {
		
		//Check mit Datum
		if(Calendar.getInstance().getTime().before(this.endDate)) {
			this.open = true;
		}
		else if(this.open)this.endAuction();
		
		return this.open;
	}
	
	public boolean close() {
		this.open = false;
		return !this.open;
	}
	
	boolean endAuction() {
		this.open = false;
		
		Debug.printInfo("Closing auction: " + this.getName());
		
		Notification owner = new Notification(this.owner, "!auction-ended", "The auction '" + this.name + "' has ended. "+this.highest.getUser().getName()+" won with "+this.highest.getMoney().toString()+".");
		DataHandler.pendingNotifications.addNotification(owner);
		Notification winner = new Notification(this.highest.getUser(), "!auction-ended", "The auction '" + this.name + "' has ended. you won with "+this.highest.getMoney().toString()+".");
		DataHandler.pendingNotifications.addNotification(winner);
		
		try {
			Event temp = null;
			if(this.highest.getMoney() > 0) {
				temp = new BidEvent(BidEvent.types.BID_WON, this.highest.getUser().getName(), this.id, this.highest.getMoney());
				
				if(AuctionServer.analytics != null){
					AuctionServer.analytics.processEvent(temp);	
				}
				else{
					Debug.printError("no communication with AnalyticsServer");
				}
			}
			temp = new AuctionEvent(AuctionEvent.types.AUCTION_ENDED, this.id, 0);
			if(AuctionServer.analytics != null){
				AuctionServer.analytics.processEvent(temp);	
			}
			else{
				Debug.printError("no communication with AnalyticsServer");
			}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		try {
			AuctionServer.billingServer.billAuction(this.highest.getUser().getName(), this.id, this.highest.getMoney());
		}
		catch(Exception e) {
			Debug.printInfo("No connection to Billing Server");
		}
		return true;
	}

	public boolean bid(Bid newBid) {
		
		if(newBid.getMoney() > highest.getMoney() && this.checkOpen()) {
			
			Notification over = new Notification(highest.getUser(), "!new-bid", "You have been overbid on '" + this.getName() + "'");
			DataHandler.pendingNotifications.addNotification(over);
			
			Event temp;
			
			if(this.highest.getMoney() > 0.00) {
				temp = new BidEvent(BidEvent.types.BID_OVERBID, this.highest.getUser().getName(), this.id, this.highest.getMoney());
				try {
					AuctionServer.analytics.processEvent(temp);
				} catch (RemoteException e) {					
					Debug.printInfo("No connection to Analytics Server");
				}
				catch(NullPointerException nl){
					Debug.printDebug("null pointer 1");
				}
			}
			
			this.highest = newBid;
			
			temp = new BidEvent(BidEvent.types.BID_PLACED, this.highest.getUser().getName(), this.id, this.highest.getMoney());
			try {
				AuctionServer.analytics.processEvent(temp);
			} catch (RemoteException e) {				
				Debug.printInfo("No connection to Analytics Server");
			}
			catch(NullPointerException nl){
				Debug.printDebug("null pointer 2");
			}
			return true;
		}
		return false;
	}
	
	public String toString() {
		String text = "";
		
		text = this.id + ". '" + this.name + "' " + this.owner.getName() + " " + this.endDate.toString() + " " + this.getHighest().getMoney() + " " + this.highest.getUser().getName();
		
		return text;
	}
	
	public String toCreatedString() {
		String text = "";
		
		text = "An auction '"+name+"' with id "+id+" has been created and will end on "+endDate.toString()+".";
		
		return text;
	}
	
	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}
	
	public Bid getHighest() {
		return highest;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public Date getEndDate() {
		return endDate;
	}
}

