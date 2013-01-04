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
import java.util.Scanner;
import java.util.StringTokenizer;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

import security.Base64Channel;
import security.CipherChannel;
import security.TCPChannel;

import model.SignedBid;
import model.User;

import methods.Methods;

import debug.Debug;

public class CommandThread implements Runnable {
	
	Client parentClient;
	
	int clientPort;
	PrintWriter socketWriter = null;
	Scanner scanner;

	public static PublicKey publickey = null;
	public static PrivateKey privatekey = null;

	 private static String pathToPublicKeyServer = "keys/auction-server.pub.pem";
	 private static String pathToPrivateKeyUser ="keys/alice.pem";
	
	
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
						
						if(getKeysClient()){
								Debug.printInfo("reading client keys success");
								
								CipherChannel cipherChannel= new CipherChannel(new Base64Channel(new TCPChannel(parentClient.socket)));
			            		cipherChannel.setKey(publickey);
			            		cipherChannel.setalgorithm("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
	
			            		// login request send from the client to the auction server
			            		
			                    String sChallangeBase64 = Methods.getRandomNumber(32);
			                    // !login username tcpPort client-challange
			                    String firstMessage=("!login "+ parentClient.user.getName() + " "+ parentClient.user.getPort() + " " +sChallangeBase64);
			                    
			                    assert firstMessage.matches("!login [a-zA-Z0-9_\\-]+ [0-9]+ ["+Methods.B64+"]{43}=") : "1st message";
								
								cipherChannel.send(firstMessage.getBytes());							
						}		
						
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
	
	
	@SuppressWarnings("finally")
	public static boolean getKeysClient() {
        boolean result=false;
        PEMReader inPrivat=null,inPublic = null;
        try {
            //public key from client
            try {
              inPublic = new PEMReader(new FileReader(pathToPublicKeyServer));
            } catch (Exception e) {
                 System.out.println("Can't read file for public key!");
                 return false;
            }
            publickey= (PublicKey) inPublic.readObject();

            //private key from client    
            FileReader privateKeyFile=null;
            try {
               privateKeyFile=new FileReader(pathToPrivateKeyUser);
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