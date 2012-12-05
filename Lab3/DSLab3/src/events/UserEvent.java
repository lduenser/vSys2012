package events;

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
	
	public String toString() {
		String info = "";
		
		if(this.type.equals(types.USER_LOGIN.toString())) {
			info = "User " + this.userName + " logged in";
		}
		if(this.type.equals(types.USER_LOGOUT.toString())) {
			info = "User " + this.userName + " logged out";
		}
		if(this.type.equals(types.USER_DISCONNECTED.toString())) {
			info = "User " + this.userName + " disconnected";
		}
		
		return this.getHead() + info;
	}


}
