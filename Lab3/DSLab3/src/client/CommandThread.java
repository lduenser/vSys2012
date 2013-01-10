package client;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.crypto.Mac;

import org.bouncycastle.util.encoders.Base64;

import security.Channel;
import security.TCPChannel;

import methods.Methods;
import model.SignedBid;
import model.Timestamp;
import model.User;
import debug.Debug;

public class CommandThread implements Runnable {
	
	Client parentClient;
	
	int clientPort;
	PrintWriter socketWriter = null;
	Scanner scanner;
	
	boolean isRunning = true;
	
	public CommandThread(Socket s, int clientPort, Client current) {
		this.clientPort = clientPort;
		this.parentClient = current;
		
		scanner = new Scanner(System.in);
	}
	
	public void updateStreams() {		
	}
 
	public void run() {
		
		 try {
			 
			 this.getClientList();
			 
			do {
				if(System.in.available()>0) {
					String output = scanner.nextLine();
					
					if(output.contains("!logout")){
						this.parentClient.createTCPChannel();
						this.parentClient.user = null;
					}
					
					if(output.contains("!login")) {
						output += (" " + clientPort);
						
						StringTokenizer st = new StringTokenizer(output);
						String name = null;
						
						if(st.countTokens()>1) {
							name = st.nextToken();
							name = st.nextToken();
						}	            		
						
						parentClient.setUser(name, clientPort);
						Debug.printDebug("user: " + name);
						
						if(parentClient.getKeysClient()){
							this.createLoginCommand();
						}
						else{
							Debug.printError("Login Denied!");							
						}
						
					}
					else if(output.contains("!end")) {
						Client.active = false;
					}
					else if(output.contains("!getClientlist")) {
						this.getClientList();
						Debug.printDebug(parentClient.getUserList().toString());
					}
					else parentClient.channel.send(output.getBytes());
					
					//Wenn Fehler bei der †bertragung aufgetreten ist
					if(parentClient.channel.getError()) {

						parentClient.setOffline();
						
						if(output.contains("!bid") && parentClient.user!=null) {
							Debug.printDebug("Bid couldn't be sent... obtaining Timestamps");
							
							Debug.printDebug("users: " + parentClient.getUserList().toString());
							
							if(parentClient.getUserList().size()<2) {
								Debug.printDebug("obtaining Timestamps failed... not enough users");
							}
							else {
								StringTokenizer st = new StringTokenizer(output);
								String auctionId = null;
								String bid = null;
								
								if(st.countTokens()>2) {
									st.nextToken();
									auctionId = st.nextToken();
									bid = st.nextToken();
								}
								
								this.obtainTimestamps(auctionId, bid);
									Debug.printDebug("timestamp requests sent");
								}
						}
					}
				 }
				Thread.sleep(100);
			} while(Client.active);
				 
		} catch (Exception e) {
			e.printStackTrace();
			Debug.printError("Error at CommandThread");
		} 
		 
	}
	
	synchronized void sendSignedBids() {
		
		SignedBid signedBid = null;
		boolean bidsLeft = true;
		
		Debug.printDebug("Bids Left: " + parentClient.signedBids.getSize());
		
		while(bidsLeft) {
			signedBid = parentClient.signedBids.getFirstBid();

			if(signedBid==null) return;
			
			if(signedBid.getComplete()) {
				Debug.printDebug("Send bid to Server");
				
				String buffer = null;
				buffer = "!signedBid " + signedBid.getAuction() + 
						" " + signedBid.getTimestamp(1).getPrice() + 
						" " + signedBid.getTimestamp(1).getUser() + 
						":" +  signedBid.getTimestamp(1).getTimestamp() + 
						":" + new String(Base64.encode(signedBid.getTimestamp(1).getSignature().getBytes())) + 
						" " + signedBid.getTimestamp(2).getUser() + 
						":" + signedBid.getTimestamp(2).getTimestamp() + 
						":" + new String(Base64.encode(signedBid.getTimestamp(2).getSignature().getBytes()));
				
				Debug.printDebug("Send: " + buffer);
				parentClient.channel.send(buffer.getBytes());
			}
			
			parentClient.signedBids.removeBid(signedBid);
		}
	} 
	
	synchronized void createLoginCommand() {
		
		parentClient.createCipherChannel();
		
		String clientChallangeBase64 = parentClient.createRandom(32);
        String firstMessage=("!login "+ parentClient.user.getName() + " "+ parentClient.user.getPort() + " " +clientChallangeBase64);
        
        assert firstMessage.matches("!login [a-zA-Z0-9_\\-]+ [0-9]+ ["+Methods.B64+"]{43}=") : "1st message";
        
        parentClient.channel.send(firstMessage.getBytes());	
	}
	
	synchronized void login(User user) {
		createLoginCommand();
	}
	
	synchronized void getClientList() {
		parentClient.channel.send("!getClientList".getBytes());
	}
	
	boolean obtainTimestamps(String auctionId, String bid) {
		
		User user1 = parentClient.getUserList().getAll().get(0);
		User user2 = parentClient.getUserList().getAll().get(1);
		
		Socket s1 = null;
		Socket s2 = null;
		
		
		try {
			s1 = new Socket(user1.getIp(), user1.getPort());
			s2 = new Socket(user2.getIp(), user2.getPort());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Debug.printError("Couldn't connect to user");
		}
		
		if(s1 == null || s2 == null) {
			Debug.printError("Users not online");
			return false;
		}
		else {
			 try {
				Channel client1 = createTCPChannel(s1);
				Channel client2 = createTCPChannel(s2);
				
				String command = "!getTimestamp "+auctionId+" "+Double.parseDouble(bid)+"\r\n";
				client1.send(command.getBytes());
				client2.send(command.getBytes());
				
				if(client1.getError() || client2.getError()) {
					Debug.printError("Error while sending timestamp requests");
				}
				
				else {
					//Waiting for Responses
					boolean timestampComplete = false;
					int run = 0;
					
					while(!timestampComplete && run < 20) {
						run++;
						
						Timestamp stamp1 = recieveTimestamp(client1, user1);
						Timestamp stamp2 = recieveTimestamp(client2, user2);
						
						if(stamp1!=null) {
							SignedBid signedBid = parentClient.signedBids.getBidByBid(stamp1.getAuctionId(), stamp1.getPrice());
							if(signedBid==null) {
	            				signedBid = new SignedBid(null, stamp1.getPrice(), stamp1.getAuctionId());
	            				parentClient.signedBids.addBid(signedBid);
	            			}
							signedBid.addTimestamp(stamp1);
	            			parentClient.signedBids.updateBid(signedBid);
	            			if(signedBid.getComplete()) {
	            				timestampComplete = true;
	            				Debug.printDebug("Timestamps complete");
	            			}
	            			
	            			Debug.printDebug("Timestamp 1 recieved");
						}
						if(stamp2!=null) {
							SignedBid signedBid = parentClient.signedBids.getBidByBid(stamp2.getAuctionId(), stamp2.getPrice());
							if(signedBid==null) {
	            				signedBid = new SignedBid(null, stamp2.getPrice(), stamp2.getAuctionId());
	            				parentClient.signedBids.addBid(signedBid);
	            			}
							signedBid.addTimestamp(stamp2);
	            			parentClient.signedBids.updateBid(signedBid);
	            			if(signedBid.getComplete()) {
	            				timestampComplete = true;
	            				Debug.printDebug("Timestamps complete");
	            			}
	            			
	            			Debug.printDebug("Timestamp 2 recieved");
						}
						
						Thread.sleep(100);
					}
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
		}
		
		return true;
	
	}
	
	private Timestamp recieveTimestamp(Channel client, User user) {
		Timestamp stamp = null;
		String input = null;
		byte[] temp = client.receive();
		
		if(temp != null){
			try{
				input = new String(temp, "UTF8");						
			}
			catch(UnsupportedEncodingException uns) {
				uns.printStackTrace();
			}
		}
		 
		if(input!=null) {
			StringTokenizer st = new StringTokenizer(input);
			String token = null;
			int countToken = st.countTokens();
	
			if(st.hasMoreTokens()) {
				token = st.nextToken();
				
				if(token.equals("!timestamp") && countToken == 5) {
					int responseAuction = Integer.parseInt(st.nextToken());
        			double responseBid = Double.parseDouble(st.nextToken());
        			long responseTimestamp = Long.parseLong(st.nextToken());
        			String responseHash = st.nextToken();
        			
        			Debug.printDebug("Auktion: " + responseAuction);
        			
        			stamp = new Timestamp(responseTimestamp, responseAuction, responseBid, user.getName(), responseHash);
				}
			}
		}
		return stamp;
	}

	private Channel createTCPChannel(Socket socket) {
		Channel channel = null;
		
		try {
			channel = new TCPChannel(socket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return channel;
	}
	
	synchronized void stop() {
		 
		 try {
			scanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		 Debug.printInfo("Shutdown CommandThread complete");
	}
}