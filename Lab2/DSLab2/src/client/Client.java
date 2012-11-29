package client;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import methods.Methods;
import debug.Debug;

public class Client {
	
	private static int argCount = 3;
	static int port = 5000;
	static boolean active = true;
	static Socket socket = null;
	static int keepAliveTime = 5000;
	static String host = "";
	static int serverPort = 10290;
	static int clientPort = 10291;
	
	public Client() {
		Debug.info = true;
		Debug.error = true;
		Debug.debug = true;
	}
	
	public static void main(String[] args) throws Exception {
		
		new Client();
		checkArguments(args);
		
		socket = null;
		Debug.printInfo("Client started");
		
		InputThread input = null;
		CommandThread output = null;
	//	UDPThread udp = null;
		 
		try {
			socket = new Socket(host, serverPort);
			
			input = new InputThread(socket);
			output = new CommandThread(socket, clientPort);
	//		udp = new UDPThread(clientPort);
			
			new Thread(input).start();
			new Thread(output).start();
			/* no UDP in Lab2
			 * new Thread(udp).start();
			 */
						
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
			 if(input!=null)input.stop();
			 if(output!=null)output.stop();
			 if(socket!=null)socket.close();
			
		} catch (Exception e) {
			Debug.printError("Shutdown - " + e.toString());
		}
		 
		Debug.printInfo("Shutdown Client completed!");
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
	 	    	Client.active = false;
	         	Debug.printDebug("Server Disconnected");
	         	socketWriter.close();
	            return false;
	 	    }

	 	    return true;
	    }
	}
}




