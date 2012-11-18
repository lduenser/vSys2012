package model;

import java.util.ArrayList;

public class PendingNotifications {
	static private ArrayList<Notification> notifications = new ArrayList<Notification>();
	
	public boolean addNotification(Notification notification) {
		
		notifications.add(notification);
		
		return true;
	}
	
	public Notification getNotificationFromOnlineUser(UserList users) {
		
		if(!notifications.isEmpty()) {
			for(Notification notification:notifications){
				if(users.getOnline(notification.getUser())) {
					return notification;
				}
			}
		}
		return null;
	}	
	
	public Notification getNotifications() {
		if(!notifications.isEmpty()) {
			return notifications.get(notifications.size()-1);
		}
		return null;
	}
	
	public Notification getNotification(User user) {
		
		for(Notification notification:notifications) {
			if(notification.getUser().getName().equals(user.getName())) {
				return notification;
			}
		}
		
		return null;
	}	
	
	public String getList() {
		
		String temp = new String();
		
		for(Notification notification:notifications) {
			temp+= notification.toCompleteString();
		}
		
		return temp;
	}
	
	public boolean removeNotification(Notification notification) {
		notifications.remove(notification);
		
		return true;
	}
	
	public int getSize() {
		return notifications.size();
	}
}
