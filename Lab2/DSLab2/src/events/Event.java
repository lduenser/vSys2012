package events;

import java.io.Serializable;
import java.sql.Timestamp;

public abstract class Event implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8543522213236676010L;
	
	String id; 
	String type;
	long timestamp;
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getType() {
		return type;
	}
	
	public long getTimeStamp() {
		return this.timestamp;
	}
	
	public Event() {
		
		java.util.Date date= new java.util.Date();
		timestamp = new Timestamp(date.getTime()).getTime();
	}
	
	public String toString() {
		return getHead() + "Event";
	}
	
	protected String getHead() {
		return type + ": " + new Timestamp(timestamp).toString() + " - ";
	}
}
