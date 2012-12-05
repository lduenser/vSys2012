package analytics.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import events.UserEvent;

public class StatisticsEventData {
	
	private double startTime = 0;
	
	private ArrayList<UserEvent> USER_LOGGED_IN = null;
	
	private double USER_SESSIONTIME_MIN = 0;
	private double USER_SESSIONTIME_MAX = 0;
		private int USER_SESSIONTIME_COUNT = 0;
	private double USER_SESSIONTIME_AVG = 0;
	
	private double BID_PRICE_MAX = 0;
		private double BID_COUNT = 0;
	private double BID_COUNT_PER_MINUTE = 0;
	
		private double AUCTION_COUNT = 0;
	private double AUCTION_TIME_AVG = 0;
		private double AUCTION_END_COUNT = 0;
		private double BID_WON_COUNT = 0;
	private double AUCTION_SUCCESS_RATIO = 0;
	
	public StatisticsEventData() {
		Date date = new java.util.Date();
		startTime = new Timestamp(date.getTime()).getTime();
		
		USER_LOGGED_IN = new ArrayList<UserEvent>();
	}
	
	public boolean addUserToList(UserEvent temp) {
		USER_LOGGED_IN.add(temp);
		return true;
	}
	
	public boolean logoutUser(UserEvent temp) {
		
		for(UserEvent user:USER_LOGGED_IN) {
			if(user.getUserName().equals(temp.getUserName())) {
				
				long time = temp.getTimeStamp() - user.getTimeStamp();
				this.addSessionTime(time);
				USER_LOGGED_IN.remove(user);
				
				return true;
			}
		}
		
		return false;
	}
	
	public boolean endAuction() {
		AUCTION_END_COUNT++;
		this.calculateSuccessRatio();
		return true;
	}
	public boolean bidWon() {
		BID_WON_COUNT++;
		this.calculateSuccessRatio();
		return true;
	}
	

	
	private void calculateSuccessRatio() {
		AUCTION_SUCCESS_RATIO = BID_WON_COUNT/AUCTION_END_COUNT;
	}
	
	public boolean addAuction(double time) {
		AUCTION_COUNT++;
		
		double avg = ((AUCTION_TIME_AVG*(AUCTION_COUNT-1))+time)/AUCTION_COUNT;
		
		AUCTION_TIME_AVG = avg;
		
		return true;
	}
	
	public boolean addSessionTime(double time) {
		time = time/1000;
		if(time > USER_SESSIONTIME_MAX) USER_SESSIONTIME_MAX = time;
		if(time < USER_SESSIONTIME_MIN || USER_SESSIONTIME_MIN == 0) USER_SESSIONTIME_MIN = time;
		
		USER_SESSIONTIME_COUNT++;
		
		double avg = ((USER_SESSIONTIME_AVG*(USER_SESSIONTIME_COUNT-1))+time)/USER_SESSIONTIME_COUNT;
		
		USER_SESSIONTIME_AVG = avg;
		
		return true;
	}
	
	
	
	public boolean checkPriceMax(double newPrice) {
		if(newPrice > this.BID_PRICE_MAX) {
			this.BID_PRICE_MAX = newPrice;
			return true;
		} 
		return false;
	}
	
	public void addBid() {
		this.BID_COUNT++;
	}
	
	
	public double getBID_PRICE_MAX() {
		return this.BID_PRICE_MAX;
	}
	
	public double getBID_COUNT_PER_MINUTE() {
		Date date = new java.util.Date();
		double time = new Timestamp(date.getTime()).getTime();
		
		double duration = (time-startTime)/1000/60;
		
		this.BID_COUNT_PER_MINUTE = this.BID_COUNT/duration;
		
		return this.BID_COUNT_PER_MINUTE;
	}

	public double getUSER_SESSIONTIME_MIN() {
		return USER_SESSIONTIME_MIN;
	}

	public double getUSER_SESSIONTIME_MAX() {
		return USER_SESSIONTIME_MAX;
	}

	public double getUSER_SESSIONTIME_AVG() {
		return USER_SESSIONTIME_AVG;
	}
	
	public double getAUCTION_SUCCESS_RATIO() {
		return this.AUCTION_SUCCESS_RATIO;
	}
	
	public double getAUCTION_TIME_AVG() {
		return this.AUCTION_TIME_AVG;
	}
	
}
