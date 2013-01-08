package server.threads;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.bouncycastle.util.encoders.Base64;

import methods.Methods;
import model.Auction;
import model.Bid;
import model.User;
import security.Base64Channel;
import security.CipherChannel;
import security.TCPChannel;
import server.AuctionServer;
import server.DataHandler;
import debug.Debug;
import events.Event;
import events.UserEvent;


public class AuctionServerThread extends Thread {
	private Socket s;
	
	private CipherChannel cipher;
	private String challange = null;
		
	User user = null;
	String username = "";
	int port;
	
	public AuctionServerThread(Socket s) throws IOException {
		this.s = s;
		
		try {
			cipher = new CipherChannel(new Base64Channel(new TCPChannel(s)));
			cipher.setKey(AuctionServer.privatekey);
			cipher.setalgorithm("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
 
	public synchronized void run() {
		
		while(AuctionServer.active) {
			if(AuctionServer.stop) {
				//Debug.printDebug("No action - Server stopped");
			}
			else {
				String input = null;
				
				byte[] temp = cipher.receive();
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
	
	synchronized void processInput(String input) throws UnsupportedEncodingException {
		
		StringTokenizer st = new StringTokenizer(input);
		String token = st.nextToken();
		boolean completed = false;
		
		if(user == null) {
			if(token.equals("!login")) {
				
				String name = null;
				int portUser;
				
				if(!st.hasMoreTokens()) {
					//sendText("enter your username!");
					Debug.printDebug("username is missing");
				}
				
				else{
					
					name = st.nextToken();
					username = name;
					portUser = Integer.parseInt(st.nextToken());
					port = portUser;
					String clientChallange = st.nextToken();
					
					// send 2nd Message: !ok <client-challenge> <server-challenge> <secret-key> <iv-parameter>.
					String serverChallange= Methods.getRandomNumber(32);
					 challange = serverChallange;
					 
					 SecretKey secretKey=null;
                     try {
                       KeyGenerator generator;
                       generator = KeyGenerator.getInstance("AES");
                       generator.init(256);// KEYSIZE is in bits
                       secretKey = generator.generateKey();
                     } catch (NoSuchAlgorithmException ex) {
                       ex.printStackTrace();
                     }
					 
					 String ivParam= Methods.getRandomNumber(16);
					 
                     String secondMessage=("!ok "+clientChallange+" "+serverChallange+" "+new String(Base64.encode(secretKey.getEncoded()),"UTF8")+" "+ivParam);
                     assert secondMessage.matches("!ok ["+Methods.B64+"]{43}= ["+Methods.B64+"]{43}= ["+Methods.B64+"]{43}= ["+Methods.B64+"]{22}==") : "2nd message";
                                          
                     Debug.printDebug("second: "+ secondMessage);
                     
                     cipher.send(secondMessage.getBytes());
                     
                     cipher.setalgorithm("AES/CTR/NoPadding");
                     cipher.setKey(secretKey);
                     cipher.setInitVector(Base64.decode(ivParam.getBytes()));
                                          
				}				
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
				
			}
			else if(token.equals("!getClientList")) {
				
				sendText("!clientListStart\r\n" + DataHandler.users.toString() + "\r\n!clientListEnd");
				
				Debug.printDebug("\r\n!clientListStart\r\n" + DataHandler.users.toString() + "\r\n!clientListEnd");
			}
			else if(token.startsWith("!")){
				sendText("Unknown command! - test");
				Debug.printDebug("Unknown command from " + s.toString());
			}
			
			else {			
				String serverChallangefromClient = token;
					                   
					Debug.printDebug("User tries to Log In");
					Debug.printDebug("3rd msg from Client: "+ serverChallangefromClient);
					
					if(challange.equals(serverChallangefromClient)){
						Debug.printDebug("3rd msg is ok !");
					
							Debug.printInfo("Login User " + username + " - " + s.toString());
							
							user = new User(username, s.getInetAddress(), port);
							
							synchronized(DataHandler.users) {
								if(!DataHandler.users.login(user)) {
						//			sendText("You're already logged in on another machine!");
									Debug.printDebug(user.getName()+ ": You're already logged in on another machine!");
									user = null;
								}
								else {
									Event temp = new UserEvent(UserEvent.types.USER_LOGIN, username);
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
							//		Debug.printDebug("Successfully logged in as " + user.getName());
								}
							}
						
							completed = true;
					}
					else{
						Debug.printDebug("not allowed to Log In user ..." + username);
						Debug.printDebug("c: "+challange);
					}
					
			}
		}
	}
	
	public synchronized void terminateClient() throws IOException {
		
		//Logout User
		synchronized(DataHandler.users) {
			DataHandler.users.logout(user.getName());
		}
		
		s.close();
	}
	
	public void sendText(String text) {
		
		try {
		//	cipher.unsetSendEncrypted();
			cipher.setSendEncrypted();
			cipher.send(text.getBytes("UTF8"));
		//	if(user==null) cipher.setSendEncrypted();
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}