package model;

import java.util.Calendar;
import java.util.Date;

import server.DataHandler;

import debug.Debug;


public class Auction {
	
	int id = 0;

	private String name = null;
	
	private User owner;

	private Date endDate = null;
	
	private Bid highest = null;
	
	private Boolean open = false;
	
	public Auction(String name, User owner, int seconds) {
		this.name = name;
		this.endDate = new Date(System.currentTimeMillis() + seconds*1000);
		this.owner = owner;
		this.open = true;
		this.highest = new Bid(owner, 0.00);
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
		
		return true;
	}

	public boolean bid(Bid newBid) {
		
		if(newBid.getMoney() > highest.getMoney() && this.checkOpen()) {
			
			Notification over = new Notification(highest.getUser(), "!new-bid", "You have been overbid on '" + this.getName() + "'");
			DataHandler.pendingNotifications.addNotification(over);
			
			this.highest = newBid;
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

