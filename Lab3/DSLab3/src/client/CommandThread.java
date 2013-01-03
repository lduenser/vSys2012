package client;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import methods.Methods;

import debug.Debug;

public class CommandThread implements Runnable {
	
	private Socket s;
	int clientPort;
	PrintWriter socketWriter = null;
	Scanner scanner;
	
	boolean isRunning = true;
	
	public CommandThread(Socket s, int clientPort) {
		this.s = s;
		this.clientPort = clientPort;
		
		try {
			socketWriter = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
			scanner = new Scanner(System.in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
 
	public void run() {
		
		 try {
			 
			do {
				if(System.in.available()>0) {
					String output = scanner.nextLine();
					
					if(output.contains("!login")) {
						output += (" " + clientPort);
						
						
					}
					if(output.contains("!end")) {
						Client.active = false;
					}
					
					output+= "\r\n";
					
					socketWriter.write(output);
					socketWriter.flush();
				 }
				Thread.sleep(100);
			} while(Client.active);
				 
		} catch (Exception e) {
			// Debug.printError(e.toString());
		} 
		 
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