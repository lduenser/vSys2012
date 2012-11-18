package model;

import java.net.InetAddress;

public class User {
	
	private String name = null;
	
	private InetAddress ip;
	private int port;
	
	private Boolean online = false;
	
	public User(String name, InetAddress ip, int port) {
		this.name = name;
		this.ip = ip;
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public InetAddress getIp() {
		return ip;
	}

	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Boolean getOnline() {
		return online;
	}

	public void setOnline(Boolean online) {
		this.online = online;
	}
	
}
