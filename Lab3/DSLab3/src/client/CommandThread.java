package client;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.security.Key;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.bouncycastle.util.encoders.Base64;

import methods.Methods;
import model.SignedBid;
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
					
					if(output.contains("!login")) {
						output += (" " + clientPort);
						
						StringTokenizer st = new StringTokenizer(output);
						String name = null;
						
						if(st.countTokens()>1) {
							name = st.nextToken();
							name = st.nextToken();
						}	            		
						
						this.parentClient.setUser(name, clientPort);
						

						if(parentClient.getKeysClient()){
							Debug.printInfo("reading client keys success");
							parentClient.createCipherChannel();
							
							String clientChallangeBase64 = parentClient.createRandom(32);
						//	String clientChallangeBase64 = Methods.getRandomNumber(32);
		                    
							// !login <username> <tcpPort> <client-challange>							
		                    String firstMessage=("!login "+ parentClient.user.getName() + " "+ parentClient.user.getPort() + " " +clientChallangeBase64);
		                    
		                    assert firstMessage.matches("!login [a-zA-Z0-9_\\-]+ [0-9]+ ["+Methods.B64+"]{43}=") : "1st message";
		                    Debug.printDebug("first: "+ firstMessage);
		                    
		                    parentClient.channel.send(firstMessage.getBytes());	
		                    
						}		
						
					}
					else if(output.contains("!end")) {
						Client.active = false;
					}
					else parentClient.channel.send(output.getBytes());
					

					//Wenn Fehler bei der †bertragung aufgetreten ist
					if(parentClient.channel.getError()) {

						parentClient.setOffline();
						
						if(output.contains("!bid") && parentClient.user!=null) {
							Debug.printDebug("Bid couldn't be sent... obtaining Timestamps");
							
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
								
								if(this.obtainTimestamps(auctionId, bid)) {
									Debug.printDebug("timestamp requests sent");
								}
								else Debug.printDebug("obtaining Timestamps failed... couldn't connect to users");
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
		
		while(bidsLeft) {
			signedBid = parentClient.signedBids.getFirstBid();

			if(signedBid==null) bidsLeft = false;
			
			//TODO: Send to Server
			if(signedBid.getComplete()) {
				parentClient.channel.send("!signedBid\r\n".getBytes());
			}
			
			parentClient.signedBids.removeBid(signedBid);
		}
	} 
	
	synchronized void login(User user) {
		socketWriter.write("!login " + user.getName() + " " + user.getPort() + "\r\n");
		socketWriter.flush();
	}
	
	synchronized void getClientList() {
		parentClient.channel.send("!getClientList".getBytes());
	}
	
	boolean obtainTimestamps(String auctionId, String bid) {
		User user1 = parentClient.getUserList().getAll().get(0);
		User user2 = parentClient.getUserList().getAll().get(1);
		
		Socket s1 = null;
		Socket s2 = null;
		
		PrintWriter socketWriter1 = null;
		PrintWriter socketWriter2 = null;
	
		try {
			s1 = new Socket(user1.getIp(), user1.getPort());
			s2 = new Socket(user2.getIp(), user2.getPort());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Debug.printError("Couldn't connect to user");
		}
		
		if(s1 == null || s2 == null) {
			return false;
		}
		else {
			 try {
				socketWriter1 = new PrintWriter(new OutputStreamWriter(s1.getOutputStream()));
				socketWriter2 = new PrintWriter(new OutputStreamWriter(s2.getOutputStream()));
				
				socketWriter1.write("!getTimestamp "+auctionId+" "+bid+"\r\n");
				socketWriter1.flush();
				
				socketWriter2.write("!getTimestamp "+auctionId+" "+bid+"\r\n");
				socketWriter2.flush();
				
			} catch (IOException e) {
				Debug.printError("Couldn't send timestamp requests");
			}
			 
		}
		
		return true;
	
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