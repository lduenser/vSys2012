package server.threads;

import java.util.Scanner;

import server.AuctionServer;

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
					
					if(output.length() == 0) {
						AuctionServer.active = false;
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