package server.threads;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.util.StringTokenizer;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.util.encoders.Base64;

import methods.Methods;
import model.Auction;
import model.Bid;
import model.User;
import security.Base64Channel;
import security.CipherChannel;
import security.IntegrityCheck;
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
	public PublicKey publickey = null;
	
	public AuctionServerThread(Socket s) throws IOException {
		this.s = s;		
		initCipher();
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
					sendText("enter your username!");
				//	Debug.printDebug("username is missing");
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
                                          
                //     Debug.printDebug("second: "+ secondMessage);
                    
                     
                     if(getPublicKeyUser()){
            //        	 Debug.printDebug("read public key from user:" +username);
                    	 Debug.printInfo("sending 2nd msg");
                    	 cipher.send(secondMessage.getBytes());
                         
                         cipher.setalgorithm("AES/CTR/NoPadding");
                         cipher.setKey(secretKey);
                         cipher.setInitVector(Base64.decode(ivParam.getBytes()));
                     }
                     else{
                    	 Debug.printError("Login Denied!");
                    	 sendText("Login Denied!");
                     }                     
                                          
				}				
			}				
		} 
		
		else {
			if(token.equals("!logout")) {
				synchronized(DataHandler.users) {
					DataHandler.users.logout(user.getName());
				}
				
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
				String name = user.getName();
				user = null;
				sendText("Successfully logged out as "+name+"!");
				initCipher();
				completed = true;
			}
			else if(token.equals("!create")) {
	//			String algo = cipher.getalgorithm();
	//			Debug.printDebug("algo is: "+algo);
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
			else if(token.equals("!signedBid")) {
				DataHandler.auctions.updateList();
				
				if(st.countTokens() < 4) {
					sendText("Error with signed bid");
				}
				else {
					sendText("Signed bid recieved");
					
					String auction = st.nextToken();
					String bid = st.nextToken();
					
					String userString1 = st.nextToken();
					String userString2 = st.nextToken();
					
					Debug.printDebug(userString1);
					Debug.printDebug(userString2);
					
					if(verifyString(userString1, auction, bid) && verifyString(userString2, auction, bid)) {
						Debug.printDebug("User verified");
						
						String[] items1 = userString1.split(":");
						long timestamp1 = Long.parseLong(items1[1]);
						String[] items2 = userString2.split(":");
						long timestamp2 = Long.parseLong(items2[1]);
						
						long timestamp = (timestamp1 + timestamp2) / 2;
						
						int id = Integer.parseInt(auction);
						double price = Double.parseDouble(bid);
						
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
				} completed = true;
			}
			
		}
		
		if(!completed) {
			if(token.equals("!list")) {
				Debug.printDebug("in !list (server)");
				if(user != null){
					Debug.printDebug(" hMAC anhŠngen ");
					IntegrityCheck check = new IntegrityCheck(AuctionServer.clientskeydir, user.getName());
					
					synchronized(DataHandler.auctions) {
						check.output = DataHandler.auctions.listAuctions(false);
						
						Debug.printDebug("sending: '"+DataHandler.auctions.listAuctions(false)+"'");
					}
					
					check.updateHMac();
					String sendToServer = check.getAttachedHMac();
					sendText(sendToServer);
				}
				else{
					Debug.printDebug(" ohne hMac ");
					synchronized(DataHandler.auctions) {
						sendText(DataHandler.auctions.listAuctions(false));
					}
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
			else if(token.startsWith("!") ){
				if(token.equals("!login")){
				}
				else{
					sendText("!Unknown command! - " + token);
					Debug.printDebug("Unknown command from " + s.toString());
				}				
			}
			
			else {			
				String serverChallangefromClient = token;
					                   
					Debug.printInfo("User " + username + " tries to Log In");
					
					if(challange.equals(serverChallangefromClient)){
										
							Debug.printInfo("Login User " + username + " - " + s.toString());
							
							user = new User(username, s.getInetAddress(), port);
							
							synchronized(DataHandler.users) {
								if(!DataHandler.users.login(user)) {
									sendText("You're already logged in on another machine!");
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
									
									sendText("!loggedIn - Successfully logged in as " + username);
									Debug.printDebug("Successfully logged in as " + username);
								}
							}
						
							completed = true;
					}
					else{
						Debug.printDebug("not allowed to Log In user ..." + username);
		//				Debug.printDebug("c: "+challange);
						sendText("login failed - please try again");
					}
					
			}
		}
	}
	
	private boolean verifyString(String userString, String auctionId, String bid) {
		// TODO Auto-generated method stub
		String[] items = userString.split(":");
		String name = items[0];
		String timestamp = items[1];
		String hash = new String(Base64.decode(items[2].getBytes()));
		
		Debug.printDebug(hash);
		
		String message = "!timestamp " + auctionId +  " " + bid + " " + timestamp;
		
		Signature sha_rsa;
		
		try {
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			
			sha_rsa = Signature.getInstance("SHA512withRSA");
			sha_rsa.initVerify(getPublicKeyFromUser(name));
			sha_rsa.update(message.getBytes());
			
			if(sha_rsa.verify(hash.getBytes())) {
				Debug.printDebug(userString + " - verified");
				return true;
			}
			
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;		
	}
		
	@SuppressWarnings("finally")
	public PublicKey getPublicKeyFromUser(String userName){
		PEMReader inPublic = null;
		PublicKey key = null;
		try {
			//public key from user
			
			try {
				String path = (AuctionServer.clientskeydir+userName+".pub.pem");
		//		Debug.printDebug("user public key name is: "+username);
				inPublic = new PEMReader(new FileReader(path));			
			} catch (Exception e) {
				System.out.println("Can't read file for public key!");
			}
			key = (PublicKey) inPublic.readObject();
			
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally 
		{
			try {
				if (inPublic!=null) {
					inPublic.close();
				}
	                
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
		}
		return key;
	}

	public synchronized void terminateClient() throws IOException {
		
		//Logout User
		synchronized(DataHandler.users) {
			DataHandler.users.logout(user.getName());
		}
		
		s.close();
	}
	
	@SuppressWarnings("finally")
	public boolean getPublicKeyUser(){
		PEMReader inPublic = null;
		try {
			//public key from user
			try {
				String path = (AuctionServer.clientskeydir+username+".pub.pem");
		//		Debug.printDebug("user public key name is: "+username);
				inPublic = new PEMReader(new FileReader(path));			
			} catch (Exception e) {
				System.out.println("Can't read file for public key!");
				return false;
			}
			publickey= (PublicKey) inPublic.readObject();
        }
		finally 
		{
			try {
				if (inPublic!=null) {
					inPublic.close();
				}
	                
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			return true;
		}
	}
	
	public void initCipher(){
		try {
			
			cipher = new CipherChannel(new Base64Channel(new TCPChannel(s)));
			cipher.setKey(AuctionServer.privatekey);
			cipher.setalgorithm("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public void sendText(String text) {		
		try {
			if(user==null) {
				cipher.unsetSendEncrypted();
			}
			
			cipher.send(text.getBytes("UTF8"));
			
			if(user==null) {
				cipher.setSendEncrypted();
			}			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}