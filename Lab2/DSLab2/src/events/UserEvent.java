package events;

public class UserEvent extends Event{
	
	String userName;
	
	enum types {
		USER_LOGIN,
		USER_LOGOUT,
		USER_DISCONNECTED
	}
	
	/*
	 * type is either of:
	 * - USER_LOGIN
	 * - USER_LOGOUT
	 * - USER_DISCONNECTED
	 */

}
