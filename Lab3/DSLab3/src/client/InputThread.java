package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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
							
	            			this.getUserList(input);
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
	            				String plaintext = ""+token;	            		            				
	            				for(int i = 0; i<= count-2; i++){
	            					plaintext+=" "+st.nextToken();
	            				}
	            				plaintext+="";
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
	            					System.out.println(" "+ input);
	            				}
	            				else{
	            					Debug.printDebug("not valid");
	            					Debug.printInfo("!list nochmal anfordern! (ohne hash)");
	            				}
	            				
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
	
	void getUserList(String input) throws IOException {
		
		String[] lines = input.split("[\\r\\n]+");
		String line = null;
		UserList list = new UserList();
		
		for(int i=0; i<lines.length; i++) {
			line = lines[i];
			
			if(line.startsWith("!clientList")) {
				
			}
			else {
				
				InetAddress inet = InetAddress.getByName(line.substring(0, line.indexOf(":")));
        		int port = Integer.parseInt(line.substring(line.lastIndexOf(":")+1, line.indexOf(" ")));
        		String name = line.substring(line.indexOf(" - ")+3);
				
				User user = new User(name, inet, port);
				
				list.login(user);
			}
		}
		
		parentClient.setUserList(list);
	}
}