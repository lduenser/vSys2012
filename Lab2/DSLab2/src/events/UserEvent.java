package events;

import events.BidEvent.types;

public class UserEvent extends Event{
	
	String userName;
	
	public enum types {
		USER_LOGIN,
		USER_LOGOUT,
		USER_DISCONNECTED
	}
	
	public UserEvent(types type, String userName) {
		super();
		
		this.type = type.toString();
		this.userName = userName;
	}
	
	public String getUserName() {
		return this.userName;
	}
	
	/*
	 * type is either of:
	 * - USER_LOGIN
	 * - USER_LOGOUT
	 * - USER_DISCONNECTED
	 */

}
