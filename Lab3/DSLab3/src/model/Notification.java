package model;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import server.DataHandler;

import debug.Debug;

public class Notification {
	User user;
	String code;
	String message;
	
	public Notification(User user, String code, String message) {
		this.user = user;
		this.code = code;
		this.message = message;
	}
	
	public User getUser() {
		return user;
	}
	
	public String toString() {
		return code + " " + message + ";\r\n";
	}
	
	public String toCompleteString() {
		return user.getName() + " - " + code + " " + message + "\r\n";
	}
	
	public static boolean sendNotification(Notification temp) {
    	User user = DataHandler.users.getUser(temp.getUser().getName());
		
    	String messageStr = temp.toString();
    	int port = user.getPort();
    	InetAddress local = user.getIp();
    	DatagramSocket s;
    	
    	try {
			s = new DatagramSocket();
			
	    	int length = messageStr.length();
	    	byte[] message = messageStr.getBytes();
	    	
	    	DatagramPacket p = new DatagramPacket(message, length,local,port);
	    	s.send(p);
	    	
	    	return true;
		} catch (Exception e) {
			Debug.printError("Couldn't send notification: " + temp.toString());
		} 
    	
    	return false;
    }
	
}
