package client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

import methods.Methods;
import debug.Debug;

public class Client {
	
	private static int argCount = 3;
	static int port = 5000;
	static boolean active = true;
	static Socket socket = null;
	static int keepAliveTime = 5000;
	static String host = "localhost";
	static int serverPort = 10290;
	static int clientPort = 10291;
	
	static boolean serverDisconnect = false;
	
	public static PublicKey publickey = null;
	public static PrivateKey privatekey = null;

	 private static String pathToPublicKey = "keys/alice.pub.pem";
	 private static String pathToPrivateKey ="keys/alice.pem";
	
	public Client() {
		Debug.info = true;
		Debug.error = true;
		Debug.debug = true;
	}
	
	public static void main(String[] args) throws Exception {
		
		new Client();
	//	checkArguments(args);
		
		socket = null;
		Debug.printInfo("Client started");
		
		if(getKeysClient()){
			Debug.printInfo("reading client keys success");
		}
		
		InputThread input = null;
		CommandThread output = null;
	//	UDPThread udp = null;
		TCPThread tcp = null;
		 
		try {
			socket = new Socket(host, serverPort);
			
			input = new InputThread(socket);
			output = new CommandThread(socket, clientPort);
	//		udp = new UDPThread(clientPort);
			
			Debug.printInfo("Client TCP Port: " + clientPort);
			
			tcp = new TCPThread(clientPort);
			
			new Thread(input).start();
			new Thread(output).start();
			/* no UDP in Lab2
			 * new Thread(udp).start();
			 */
			new Thread(tcp).start();
						
			Debug.printInfo("Client connected to server");
		}
		catch(Exception e) {
			Debug.printError("Could not connect to server " + host + " on Port " + serverPort + "!");
			active = false;
		}
		
		
		while(active) {
			checkAlive(socket);
			Thread.sleep(Client.keepAliveTime);
		}
				 
		 try {
	//		 if(udp!=null)udp.stop();
			 if(tcp!=null)tcp.stop();
			 if(input!=null)input.stop();
			 if(output!=null)output.stop();
			 if(socket!=null)socket.close();
			
		} catch (Exception e) {
			Debug.printError("Shutdown - " + e.toString());
		}
		 
		Debug.printInfo("Shutdown Client completed!");
	}
	
	@SuppressWarnings("finally")
	public static boolean getKeysClient() {
        boolean result=false;
        PEMReader inPrivat=null,inPublic = null;
        try {
            //public key from client
            try {
              inPublic = new PEMReader(new FileReader(pathToPublicKey));
            } catch (Exception e) {
                 System.out.println("Can't read file for public key!");
                 return false;
            }
            publickey= (PublicKey) inPublic.readObject();

            //private key from client    
            FileReader privateKeyFile=null;
            try {
               privateKeyFile=new FileReader(pathToPrivateKey);
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
	
	
	
	private static void checkArguments(String[] args) throws Exception{
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
	                		clientPort = Integer.parseInt(args[i]);
	                	}
	                	catch(Exception e){
	                		Debug.printError("clientPort is no Integer");
	                	}
	                	clientPort = Methods.setPort(Integer.valueOf(args[2]));
	                	break;
	            }
	     }
	}	
	
	private static boolean checkAlive(Socket socket){
		
		Socket s = socket;
		PrintWriter socketWriter = null;
		try {
			socketWriter = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
       synchronized (socket) {
	    	
	    	boolean error = false;

			if(socketWriter.checkError()) {
				error = true;
			}
			try {
	        	socketWriter.write("!alive\r\n");
				socketWriter.flush();
				
				if(socketWriter.checkError()) {
					error = true;
				}
				
	        } catch (Exception e) {
	        	
	        }
	    	 
	    	 if(error) {
	    		 Client.serverDisconnect = true;
	 	    	//Client.active = false;
	         	Debug.printDebug("Server Disconnected");
	         	//socketWriter.close();
	            return false;
	 	    }
	    	 else {
	    		 if(Client.serverDisconnect == true)
	    			 Debug.printDebug("Server online again");
	    		 Client.serverDisconnect = false;
	    	 }

	 	    return true;
	    }
	}
}




