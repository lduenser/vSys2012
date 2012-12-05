package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import debug.Debug;


public class UDPThread implements Runnable {
	
	DatagramSocket serverSocket;
    byte[] receiveData = new byte[1024];
    byte[] sendData = new byte[1024];
	
	public UDPThread(int port) {
		try {
			serverSocket = new DatagramSocket(port);
		} catch (IOException e) {
			Debug.printError(e.toString());
		}	
	}
 
	public void run() {
		
		
        while(Client.active) {
        	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	           try {
	        	   
	        	   serverSocket.receive(receivePacket);
        		   String sentence = new String( receivePacket.getData());
	               System.out.println(sentence);
	        	   
	               Thread.sleep(100);
	               
				} 
	           catch(Exception e) {
	        	   //Debug.printError(e.toString());
	        	   return;
	           }
           }
	}
	
	synchronized void stop() throws Exception {
		
		serverSocket.close();
		
		Debug.printInfo("Shutdown UDPThread complete");
	}
}