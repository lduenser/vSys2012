package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.StringTokenizer;

import model.SignedBid;
import model.Timestamp;
import model.User;
import model.UserList;

import debug.Debug;



public class InputThread implements Runnable {
	
	Client parentClient;
	
	boolean isRunning = true;
	
	public InputThread(Socket s, Client parent) {
		this.parentClient = parent;
		
		
		
	}
	
	public void updateStreams() {
		
	}
	
	public void run() {
		
		 try {
			 while(Client.active) {
				
				String input = null;
				byte[] temp = parentClient.channel.receive();
				
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
			
					if(st.hasMoreTokens()){
						token = st.nextToken();
						
						if(token.equals("!clientListStart")) {
	            			//Obtain Client List
	            			this.getUserList();
	            		}
	            		
	            		else if(token.equals("!timestamp")) {
	            			
	            			double timestamp = Double.parseDouble(st.nextToken());
	            			int auction = Integer.parseInt(st.nextToken());
	            			double bid = Double.parseDouble(st.nextToken());
	            			String hash = st.nextToken();
	            			
	            			Timestamp stamp = new Timestamp(timestamp, auction, bid, hash);
	            			
	            			SignedBid signedBid = parentClient.signedBids.getBidByBid(auction, bid);
	            			
	            			//Auction not in List
	            			if(signedBid==null) {
	            				signedBid = new SignedBid(null, bid, auction);
	            				parentClient.signedBids.addBid(signedBid);
	            			}
	            			
	            			signedBid.addTimestamp(stamp);
	            			parentClient.signedBids.updateBid(signedBid);
	            		}
						
	            		else  if(token!=null) {
							System.out.println(input);
						}

					}
										
            		
				 }
				Thread.sleep(100);
			}
				 
		} catch (Exception e) {
			e.printStackTrace();
			
		} 
	}
	
	synchronized void stop() {
		
		 Debug.printInfo("Shutdown InputThread complete");
		 
	}
	
	void getUserList() throws IOException {
		String output = "";
		UserList list = new UserList();
		
		boolean recieveList = true;
		
		while(recieveList) {
			
			String input = null;
			byte[] temp = parentClient.channel.receive();
			
			if(temp != null){
				try{
					input = new String(temp, "UTF8");
					
				}
				catch(UnsupportedEncodingException uns) {
					uns.printStackTrace();
				}
			}
			else break;
			
			if(input.trim().equals("")) {
				
			}
			else if (input.startsWith("!clientListEnd")){
				Debug.printDebug("End of List");
				recieveList = false;
			}
			else {
				InetAddress inet = InetAddress.getByName(output.substring(0, output.lastIndexOf(":")));
        		int port = Integer.parseInt(output.substring(output.lastIndexOf(":")+1, output.indexOf(" ")));
        		String name = output.substring(output.indexOf(" - ")+3);
				
				User user = new User(name, inet, port);
				list.login(user);
			}
		}
		
		parentClient.setUserList(list);
	}
}