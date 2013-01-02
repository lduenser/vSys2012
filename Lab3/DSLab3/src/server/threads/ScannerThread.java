package server.threads;

import java.util.Scanner;

import server.AuctionServer;
import server.DataHandler;

import debug.Debug;



public class ScannerThread implements Runnable {
	
	Scanner scanner;
	
	public ScannerThread() {
		scanner = new Scanner(System.in);		
	}
 
	public void run() {
		
		while(AuctionServer.active) {
			
			try {
				if(System.in.available()>0) {
					String output = scanner.nextLine();
					
				
					if(output.startsWith("!exit")){
						AuctionServer.active = false;
					}
					else if(output.startsWith("!stop")) {
						if(AuctionServer.stop) {
							Debug.printInfo("Server Restarted");
							AuctionServer.stop = false;
						}
						else {
							AuctionServer.stop = true;
							Debug.printInfo("Server Stopped");
						}
					}
					else if(output.startsWith("!getClientList")) {
						Debug.printInfo("\r\nActive Clients:" + DataHandler.users.toString());
					}
					else {
						Debug.printInfo("Unknown command");
					}
				 }
				
				Thread.sleep(100);
			} catch (Exception e) {
				Debug.printError(e.toString());
			}
		}
	}
	
	public synchronized void stop() {
		 try {
			 scanner.close();
			 Debug.printInfo("ScannerThread stopped");
		} catch (Exception e) {
			Debug.printError(e.toString());
		} 
	}
}