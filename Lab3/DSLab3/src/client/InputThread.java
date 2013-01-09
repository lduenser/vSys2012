package client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.Key;
import java.security.MessageDigest;
import java.util.StringTokenizer;

import org.bouncycastle.util.encoders.Base64;

import security.IntegrityCheck;

import methods.Methods;
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
							
							Debug.printDebug("clientlist wanted");
							
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
						
	            		else if(token.equals("!ok")) {
	            			
	            			String clientChallange = st.nextToken();
							String serverChallange = st.nextToken();							
							byte[] secretKey = Base64.decode(st.nextToken().getBytes());
							Key key = new javax.crypto.spec.SecretKeySpec(secretKey, "AES");							
			                byte[] iv=  Base64.decode(st.nextToken().getBytes());
										                
					//		Debug.printDebug("cc: "+ clientChallange);
					//		Debug.printDebug("sc: "+ serverChallange);
					//		Debug.printDebug("key: "+ key);
					//		Debug.printDebug("iv: "+ iv);
						
							// clientChallanges vergleichen mit versendetem ! ok? -> send 3rd msg							
							if(!clientChallange.equals(parentClient.random)){
								Debug.printInfo("clientCh nicht ident!");
					//			Debug.printDebug("pr: "+parentClient.random);
							}
							else{ // send 3rd msg														
								parentClient.createAESChannel(iv, key);															
								String thirdMessage= serverChallange;
	                    		assert thirdMessage.matches("["+Methods.B64+"]{43}=") : "3rd message";	                    		
	                //    		Debug.printDebug("third: "+thirdMessage);	                    		
	                    		parentClient.channel.send(thirdMessage.getBytes());
							}
						}
	            		
	            		else if(token!=null) {
	            			if((token.startsWith("-List") || token.startsWith("-No")) && parentClient.user != null){
	            				            				
	            				int count = st.countTokens();
	            				String plaintext = "'"+token;	            		            				
	            				for(int i = 0; i<= count-2; i++){
	            					plaintext+=" "+st.nextToken();
	            				}
	            				plaintext+="'";
	            				Debug.printDebug("plaintext: "+plaintext);
	            				
	            				String receivedHash = st.nextToken();	            				
	            				Debug.printDebug("hmac from server: "+receivedHash);
	            				
	            				IntegrityCheck checkUser = new IntegrityCheck(Client.clientskeydir, parentClient.user.getName());	            				
	            				checkUser.output = plaintext;	            				
	            				checkUser.updateHMac();
	            				
	            				Debug.printDebug("hmac from user: "+ checkUser.hash);	            			
	            				Debug.printDebug("server hash decoded: "+Base64.decode(receivedHash.getBytes()));
	            				
	            				boolean validHash = MessageDigest.isEqual(checkUser.hash,Base64.decode(receivedHash.getBytes()));
	            				
	            				if(validHash){
	            					Debug.printDebug("valid");
	            				}
	            				else{
	            					Debug.printDebug("not valid");
	            				}
	            				
	            				System.out.println("liste: " + input);
	            			}	            			
	            			else{
	            				System.out.println("from server: "+input);
	            			}	            			
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