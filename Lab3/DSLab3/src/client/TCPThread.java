package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.util.StringTokenizer;

import security.Base64Channel;
import security.Channel;
import security.TCPChannel;

import methods.Methods;
import debug.Debug;

public class TCPThread implements Runnable {
	
	ServerSocket tcpSocket = null;
	private int port;
	Client parentClient;
		
	public TCPThread(int port, Client current) {
		this.port = port;
		openServerSocket();
		this.parentClient = current;
		
	}
 
	public void run() {
		Debug.printDebug("TCPClient started on Port " + port);
	
		if(tcpSocket!=null) {
			
		}
		
        while(Client.active) {
          
        	Socket clientSocket = null;
           
        	try {
            	clientSocket = this.tcpSocket.accept();
            	
            	Channel channel = this.createTCPChannel(clientSocket);
            	
            	while(clientSocket.isConnected()) {
            		
            		String input = null;
    				byte[] temp = channel.receive();
    				
    				if(temp != null){
    					input = new String(temp);
    				}
    				 
    				if(input!=null) {
    					StringTokenizer st = new StringTokenizer(input);
                		int countToken = st.countTokens();
                		
                		if(countToken<3)
                			break;
                		
                		if(st.nextToken().equals("!getTimestamp") && countToken==3) {
                			String auction = st.nextToken();
                			String price = st.nextToken();
                			String timestamp = Long.toString(Methods.getTimeStamp());
            
                			String response = "!timestamp " + auction +  " " + price + " " + timestamp;
                			
                			Signature sha_rsa;
                			byte[] hash = null;
                			
							try {
								Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
								
								sha_rsa = Signature.getInstance("SHA512withRSA");
								sha_rsa.initSign(parentClient.privatekey);
								
	        					sha_rsa.update(response.getBytes());
	        					hash = sha_rsa.sign();
	                			
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
							
							response = response + " " + Methods.bytes2String(hash);
							
							Debug.printDebug(response);
                			channel.send(response.getBytes());

                		}
    				}
            		
            		Thread.sleep(100);
            	}
            }
                
            catch (IOException e) {
            	Debug.printError("Error accepting client connection");
            }
        	catch (InterruptedException i) {
        		Debug.printError("Error with thread");
        	}
            
            try {
				clientSocket.close();
	            clientSocket = null;
			} catch (IOException e) {
				
			}
         }
	}
	
	synchronized void stop() throws Exception {

		shutDownServerSocket();
		
		Debug.printInfo("Shutdown UDPThread complete");
	}
	
	private boolean shutDownServerSocket() {
		try {
			tcpSocket.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
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
	
	private boolean openServerSocket() {
        try {
            this.tcpSocket = new ServerSocket(this.port);
            return true;
        } catch (IOException e) {
        	Debug.printError("Cannot open port " + this.port + "");
        }
        return false;
    }
}