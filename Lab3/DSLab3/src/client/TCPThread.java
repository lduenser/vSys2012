package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

import methods.Methods;

import server.AuctionServer;
import server.threads.AuctionServerThread;

import debug.Debug;


public class TCPThread implements Runnable {
	
	ServerSocket tcpSocket = null;
	private int port;
	
	
	
	public TCPThread(int port) {
		this.port = port;
		openServerSocket();
	}
 
	public void run() {
		Debug.printDebug("TCPClient started on Port " + port);
	
		if(tcpSocket!=null) {
			
		}
		
        while(Client.active) {
          
        	Socket clientSocket = null;
            try {
            	clientSocket = this.tcpSocket.accept();
                
            	PrintWriter printer = new PrintWriter(clientSocket.getOutputStream(), true);
            	BufferedReader socketReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            	
            	while(!socketReader.ready()) {
            		try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            	if(socketReader.ready()) {
            		String input = socketReader.readLine();
            		
            		StringTokenizer st = new StringTokenizer(input);
            		String token = st.nextToken();
            		
            		if(token.equals("!getTimestamp")) {
            			String auction = st.nextToken();
            			String price = st.nextToken();
            			String timestamp = Long.toString(Methods.getTimeStamp());
            			
            			String response = "!timestamp " + auction +  " " + price + " " + timestamp;
            			printer.println(response);
            			printer.flush();
            		}
            	}
            	
            	
            }
                
            catch (IOException e) {
            	Debug.printError("Error accepting client connection");
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