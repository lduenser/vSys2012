package client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import methods.Methods;
import model.SignedBidList;
import model.User;
import model.UserList;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

import security.Base64Channel;
import security.Channel;
import security.CipherChannel;
import security.TCPChannel;
import debug.Debug;

public class Client {
	
	private static int argCount = 5;
	static int port = 5000;
	static boolean active = true;
	static int keepAliveTime = 5000;
	static String host = "localhost";
	static int serverPort = 10290;
	static String pathToPublicKeyServer = "keys/auction-server.pub.pem";
	static String clientskeydir = "keys/";
	
	private UserList users = null;
	
	User user = null;
	boolean serverDisconnect = false;
	Socket socket = null;
	int clientPort = 10291;
	
	CommandThread output = null;
	InputThread input = null;
	TCPThread tcp = null;
	
	SignedBidList signedBids = null;
	
	public Channel channel = null;
	public String random = null;
	
	public PublicKey publickey = null;
	public PublicKey publicSignedKey = null;
	public PrivateKey privatekey = null;

	
	public Client() {
		Debug.info = true;
		Debug.error = true;
		Debug.debug = true;
	}
	
	public static void main(String[] args) throws Exception {
		
		Client current = new Client();
		
		current.clientPort = 10290 + 1 + Methods.getRandomInt(200);
		
		current.socket = null;
		current.signedBids = new SignedBidList();
		
	//	checkArguments(args);
		
		Debug.printInfo("Client started on input port " + current.clientPort);
		
		try {
			current.socket = new Socket(host, serverPort);
			
			//Create unencrypted Channel
			current.createTCPChannel();
			
			current.input = new InputThread(current.socket, current);
			current.output = new CommandThread(current.socket, current.clientPort, current);
			current.tcp = new TCPThread(current.clientPort, current);
			
			new Thread(current.input).start();
			new Thread(current.output).start();
			new Thread(current.tcp).start();
						
			Debug.printInfo("Client connected to server");
		}
		catch(Exception e) {
			Debug.printError("Could not connect to server " + host + " on Port " + serverPort + "!");
			active = false;
		}		
		
		while(active) {
			checkAlive(current);
			Thread.sleep(Client.keepAliveTime);
		}
				 
		 try {
			 if(current.tcp!=null)current.tcp.stop();
			 if(current.input!=null)current.input.stop();
			 if(current.output!=null)current.output.stop();
			 if(current.socket!=null)current.socket.close();
			
		} catch (Exception e) {
			Debug.printError("Shutdown - " + e.toString());
		}
		 
		Debug.printInfo("Shutdown Client completed!");
	}
	
	private static void checkArguments(String[] args, Client current) throws Exception{
		if(args.length != argCount){
			throw new Exception("Anzahl der Argumente stimmen nicht");
		}
		
		for (int i = 0; i < args.length; i++) {
	            switch (i) {
	                case 0: host=args[i]; break;
	                
	                case 1: 
	                	try{
	                		serverPort = Integer.parseInt(args[i]);
	                	}
	                	catch(Exception e){
	                		Debug.printError("serverPort is no Integer");
	                	}
	                	serverPort = Methods.setPort(Integer.valueOf(args[1]));
	                	break;
	                	
	                case 2: 
	                	try{
	                		current.clientPort = Integer.parseInt(args[i]);
	                	}
	                	catch(Exception e){
	                		Debug.printError("clientPort is no Integer");
	                	}
	                	current.clientPort = Methods.setPort(Integer.valueOf(args[2]));
	                	break;
	                	
	                case 3: pathToPublicKeyServer = args[i]; break;
	                case 4: clientskeydir = args[i]; break;
	            }
	     }
	}	
	
	public UserList getUserList() {
		return this.users;
	}
	
	public void setUserList(UserList list) {
		this.users = list;
	}
	
	public String getUser(){
		return this.user.getName();
	}
	
	public String keydir(){
		return clientskeydir;
	}
	
	private static boolean checkAlive(Client client){
		
		boolean error = false;

		//If Server is offline try to open new socket
		if(client.serverDisconnect) {
			try {
				client.socket = new Socket(host, serverPort);
			} catch (UnknownHostException e) {
				Debug.printDebug("Server still offline - UnknownHostException");
				return false;
			} catch (IOException e) {
				Debug.printDebug("Server still offline - IOException");
				return false;
			}
			
			Debug.printDebug("Server back online");
			
			client.reconnect();
			client.serverDisconnect = false;
			return true;
		}
		
		else {
			synchronized (client.socket) {
		    	
		    	if(client.channel.getError()) {
					error = true;
				}
				
		    	//client.channel.send("!alive".getBytes());
	    		
		    	if(client.channel.getError()) {
					error = true;
				}
				
				//Server Offline
				if(error) {
		    		client.setOffline(); 
		    		return false;
		 	    }
		 	    return true;
		    }
		}
	}
	
	void setUser(String name, int port) {
		this.user = new User(name, null, port);
	}
	
	void reconnect() {
		Debug.printDebug("Reconnect Client");
		
		try {
			channel = new Base64Channel(new TCPChannel(this.socket));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		output.updateStreams();
		input.updateStreams();
		
		if(user!=null) {
			output.login(user);
		}
		
		if(signedBids!=null) {
			if(!signedBids.isEmpty())
			output.sendSignedBids();
		}		
	}
	
	void setOffline() {
		serverDisconnect = true;
	    Debug.printDebug("Server Offline");
	}
	
	void createCipherChannel() {
		CipherChannel cipherChannel;
		try {
			cipherChannel = new CipherChannel(new Base64Channel(new TCPChannel(socket)));
			cipherChannel.setKey(publickey);
			cipherChannel.setalgorithm("RSA/NONE/OAEPWithSHA256AndMGF1Padding");

			channel = cipherChannel;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void createTCPChannel() {
		try {
			channel = new Base64Channel(new TCPChannel(socket));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void createAESChannel(byte[] iv, Key key){
		CipherChannel cipher = (CipherChannel) channel;
		try{
			cipher.setalgorithm("AES/CTR/NoPadding");
			cipher.setInitVector(iv);
			cipher.setKey(key);
			
			channel = cipher;
		}
		catch(Exception e){
			e.printStackTrace();
		}		
	}
	
	String createRandom(int size){
		String randomNumber = null;
		randomNumber = Methods.getRandomNumber(size);		
		random = randomNumber;
		return random;
	}
	
	@SuppressWarnings("finally")
	public boolean getKeysClient() {
        boolean result=false;
        PEMReader inPrivat=null,inPublic = null;
        try {
            //public key from server
            try {
              inPublic = new PEMReader(new FileReader(pathToPublicKeyServer));
            } catch (Exception e) {
                 System.out.println("Can't read file for public key!");
                 return false;
            }
            publickey = (PublicKey) inPublic.readObject();

            //private key from client    
            FileReader privateKeyFile=null;
            try {
            	String path = (clientskeydir + getUser() + ".pem");
               privateKeyFile=new FileReader(path);
           	
            } catch (Exception e) {
                 System.out.println("Can't read file for private key!");
                 return false;
            }
            
            inPrivat = new PEMReader(privateKeyFile, new PasswordFinder() {
                @Override
                 public char[] getPassword() {
                    // reads the password from standard input for decrypting the private key
                    System.out.println("Enter pass phrase:");
                    try {
                        return (new BufferedReader(new InputStreamReader(System.in))).readLine().toCharArray();
                    } catch (IOException ex) {
                        return "".toCharArray();
                    }
                 }
            });

           KeyPair keyPair = (KeyPair) inPrivat.readObject();
           privatekey = keyPair.getPrivate();
           publicSignedKey = keyPair.getPublic();
           result=true;
           System.out.println("Keys successfully initialized!");
        } catch (IOException ex) {
            System.out.println("Wrong password!");
            result=getKeysClient();
        } finally {
            try {
                if (inPublic!=null) {
                  inPublic.close();
                }
                if (inPrivat!=null) {
                  inPrivat.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return result;
        }
    }
	
	public void sendSignedBids() {
		this.output.sendSignedBids();
	}
}




