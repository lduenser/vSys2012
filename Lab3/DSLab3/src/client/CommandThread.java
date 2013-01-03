package client;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;

import model.SignedBid;
import model.User;

import methods.Methods;

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
		
		try {
			socketWriter = new PrintWriter(new OutputStreamWriter(parentClient.socket.getOutputStream()));
			scanner = new Scanner(System.in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void updateStreams() {
		try {
			socketWriter = new PrintWriter(new OutputStreamWriter(parentClient.socket.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
 
	public void run() {
		
		 try {
			 
			 //Obtain Client List
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
					}
					else if(output.contains("!end")) {
						Client.active = false;
					}
					output+= "\r\n";
					
					socketWriter.write(output);
					socketWriter.flush();
					
					if(socketWriter.checkError()) {
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
				socketWriter.write("!signedBid\r\n");
				socketWriter.flush();
			}
			
			parentClient.signedBids.removeBid(signedBid);
		}
	} 
	
	synchronized void login(User user) {
		socketWriter.write("!login " + user.getName() + " " + user.getPort() + "\r\n");
		socketWriter.flush();
	}
	
	synchronized void getClientList() {
		socketWriter.write("!getClientList\r\n");
		socketWriter.flush();
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
				// TODO Auto-generated catch block
				Debug.printError("Couldn't send timestamp requests");
			}
			 
		}
		
		return true;
	
	}
	
	synchronized void stop() {
		 
		 try {
			scanner.close();
			socketWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		 Debug.printInfo("Shutdown CommandThread complete");
	}
}