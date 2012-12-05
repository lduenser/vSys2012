package model;

import java.net.InetAddress;
import java.util.ArrayList;

public class UserList {
	static ArrayList<User> userlist = new ArrayList<User>();
	
	public boolean login(String username, InetAddress ip, int port) {
		
		int userId = searchUser(username);
		if(userId > 0) {
			User temp = userlist.get(userId);
			
			temp.setOnline(false);
			temp.setIp(ip);
			temp.setPort(port);
			
			userlist.remove(userId);
			userlist.add(userId, temp);
		}
		else userlist.add(new User(username, ip, port));
		
		return true;
	}
	
	public boolean login(User user) {
		
		user.setOnline(true);
		
		int userId = searchUser(user.getName());
		if(userId >= 0) {
			User temp = userlist.get(userId);
			
			if(temp.getOnline()) return false;
			
			temp.setOnline(true);
			temp.setIp(user.getIp());
			temp.setPort(user.getPort());
			
			userlist.remove(userId);
			userlist.add(userId, temp);
		}
		else userlist.add(user);
		
		return true;
	}
	
	public boolean getOnline(User user) {
		return getUserById(searchUser(user.getName())).getOnline();
	}
	
	public boolean logout(String username) {
		int userId = searchUser(username);
		if(userId >= 0) {
			userlist.get(userId).setOnline(false);
			return true;
		}
		else return false;
	}
	
	public int searchUser(String username) {
		if(userlist==null) return -1;
		
		for(User user:userlist) {
			if(user.getName().equals(username)) return userlist.indexOf(user);
		}
		
		return -1;
	}
	
	public User getUser(String username) {
		return userlist.get(searchUser(username));
	}
	
	public User getUserById(int id) {
		if(id > -1) {
			if(userlist.get(id) != null) return userlist.get(id);
		}
		return null;
	}
	
	public int size() {
		return userlist.size();
	}
}

