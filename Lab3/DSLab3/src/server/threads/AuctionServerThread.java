package server.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.rmi.RemoteException;
import java.security.Key;
import java.util.StringTokenizer;

import security.CipherChannel;
import server.AuctionServer;
import server.DataHandler;
import security.Base64Channel;
import security.TCPChannel;

import model.Auction;
import model.Bid;
import model.User;

import debug.Debug;
import events.Event;
import events.UserEvent;


public class AuctionServerThread extends Thread {
	private Socket s;
	PrintWriter out = null;
	BufferedReader in = null;
	private CipherChannel cipher;
	private String sChallangeBase64;
	
	
	User user = null;
	String username = "";
	
	public AuctionServerThread(Socket s, Key key, String challange) throws IOException {
		this.s = s;
		
		this.cipher= new CipherChannel(new Base64Channel(new TCPChannel(s)));
		cipher.setKey(key);
		cipher.setalgorithm("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
		
		this.sChallangeBase64=challange;
		
		try {
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			out = new PrintWriter(s.getOutputStream(), true);
		} catch (IOException e) {
			Debug.printError(e.toString());
		}		
	}
 
	@SuppressWarnings("unused")
	public synchronized void run() {
		
		while(AuctionServer.active) {
			if(AuctionServer.stop) {
				//Debug.printDebug("No action - Server stopped");
			}
			else {
				String input = null;
				
				byte[] temp= cipher.receive();
				if(temp != null){
					try{
						input = new String(temp, "UTF8");
						
					}
					catch(UnsupportedEncodingException uns) {
						uns.printStackTrace();
					}
				}
				
				if(input!=null) {
					Debug.printDebug(this.username + " command: " + input);
					
					try {
						processInput(input);
					}
					catch(Exception e) {
						sendText("Error while processing your input!");
						e.printStackTrace();
					}
				}
			}
		}
		
		Debug.printInfo("Client " + s.toString() + " disconnected");
		
		try {
			terminateClient();
		} catch (IOException e) {
			Debug.printError("Client connection to " + user.getName() + " couldn't be terminated!");
		}
		
		Event temp2 = new UserEvent(UserEvent.types.USER_DISCONNECTED, username);
		
		try {
			if(user!=null)
				if(AuctionServer.analytics != null){
					AuctionServer.analytics.processEvent(temp2);
				}
				else{
					Debug.printError("no communication with AnalyticsServer");
				}
				
		} catch (RemoteException e) {			
			e.printStackTrace();
		}
	}
	
	synchronized void processInput(String input) {
		
		StringTokenizer st = new StringTokenizer(input);
		String token = st.nextToken();
		boolean completed = false;
		
		if(user == null) {
			if(token.equals("!login")) {
				if(st.countTokens() < 2) {
					sendText("enter your username and a tcp-port!");
				}
				else {
					String name = st.nextToken();
					int port = Integer.parseInt(st.nextToken());
					
					Debug.printInfo("Login User " + name + " - " + s.toString());
					
					user = new User(name, s.getInetAddress(), port);
					
					synchronized(DataHandler.users) {
						if(!DataHandler.users.login(user)) {
							sendText("You're already logged in on another machine!");
							user = null;
						}
						else {
							Event temp = new UserEvent(UserEvent.types.USER_LOGIN, name);
							try {
								if(AuctionServer.analytics != null){
									AuctionServer.analytics.processEvent(temp);
								}
								else{
									Debug.printError("no communication with AnalyticsServer");
								}
							} catch (RemoteException e) {
								e.printStackTrace();
							}
							username = user.getName();
							sendText("Successfully logged in as " + user.getName());
						}
					}
				}
				completed = true;
			}
		} 
		
		else {
			if(token.equals("!logout")) {
				synchronized(DataHandler.users) {
					DataHandler.users.logout(user.getName());
				}
				sendText("Successfully logged out as "+user.getName()+"!");
				Event temp = new UserEvent(UserEvent.types.USER_LOGOUT, user.getName());
				try {
					if(AuctionServer.analytics != null){
						AuctionServer.analytics.processEvent(temp);
					}
					else{
						Debug.printError("no communication with AnalyticsServer");
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				user = null;
				
				completed = true;
			}
			else if(token.equals("!create")) {
				
				if(st.countTokens() < 2) {
					sendText("Please enter duration and a name for your auction!");
				}
				else {
					int time = Integer.parseInt(st.nextToken());
					
					String name = st.nextToken();
					
					while(st.hasMoreTokens()) {
						name+=" "+st.nextToken();
					}
					
					Auction temp = new Auction(name, user, time);
					synchronized(DataHandler.auctions) {
						temp = DataHandler.auctions.addAuction(temp);
					}
					
					sendText(temp.toCreatedString());
				}
				completed = true;
			}
			else if(token.equals("!bid")) {
				DataHandler.auctions.updateList();
				
				if(st.countTokens() < 2) {
					sendText("Please enter an id and a bid!");
				}
				else {
					int id = Integer.parseInt(st.nextToken());
					double price = Double.parseDouble(st.nextToken());
					
					if(DataHandler.auctions.getAuctionById(id) != null) {
						synchronized(DataHandler.auctions) {
							if(DataHandler.auctions.getAuctionById(id).bid(new Bid(user, price))) {
								sendText("You successfully bid with "+ price +" on '"+DataHandler.auctions.getAuctionById(id).getName()+"'.");
							}
							else sendText("Your bid was not high enough or auction is over!");
						}
					}	
					else sendText("Couldn't find auction with id " + id);
				}
				
				completed = true;
			}
		}
		
		if(!completed) {
			if(token.equals("!list")) {
				synchronized(DataHandler.auctions) {
					sendText(DataHandler.auctions.listAuctions(false));
				}
				completed = true;
			}
			
			else if(token.equals("!end")) {
				sendText("Connection closed");
				
				try {
					terminateClient();
					Debug.printDebug("Socket " + s.toString() + " closed!");
				} catch (IOException e) {
					// Debug.printError(e.toString());
				}
			}
			else if(token.equals("!notifications")) {
				sendText("Open notifications: \r\n" + DataHandler.pendingNotifications.getList());
			}
			else if(token.equals("!alive")) {
				//Debug.printDebug("Keep alive message from: " + s.getInetAddress().toString());
			}
			else if(token.equals("!getClientList")) {
				sendText("!clientListStart\r\n" + DataHandler.users.toString() + "\r\n!clientListEnd");
				Debug.printDebug("\r\n!clientListStart\r\n" + DataHandler.users.toString() + "\r\n!clientListEnd");
			}
			else {
				sendText("Unknown command!");
				Debug.printDebug("Unknown command from " + s.toString());
			}
		}
	}
	
	public synchronized void terminateClient() throws IOException {
		
		//Logout User
		synchronized(DataHandler.users) {
			DataHandler.users.logout(user.getName());
		}
		
		in.close();
		out.close();
		s.close();
	}
	
	public void sendText(String text) {
		out.println(text);
		out.flush();
	}
}