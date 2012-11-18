package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import debug.Debug;



public class InputThread implements Runnable {
	
	private Socket s;
	BufferedReader socketReader = null;
	
	boolean isRunning = true;
	
	public InputThread(Socket s) {
		this.s = s;
		
		try {
			socketReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			 Debug.printError(e.toString());
		}
		
	}
 
	public void run() {
		
		 try {
			while(Client.active) {
				
				if(socketReader.ready()) {
					String output = socketReader.readLine();
					if(output!=null) {
						System.out.println(output);
					}
				 }
				Thread.sleep(100);
			}
				 
		} catch (Exception e) {
			Debug.printError(e.getMessage());
			
		} 
	}
	
	synchronized void stop() {
		
		 Debug.printInfo("Shutdown InputThread");
		
		 try {
			 socketReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 Debug.printInfo("Shutdown InputThread complete");
		 
	}
}