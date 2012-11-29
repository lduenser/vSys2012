package loadTest.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import loadTest.LoadTest;
import loadTest.client.threads.SetBid;
import loadTest.client.threads.StartAuction;

import debug.Debug;

public class GenericClient implements Runnable {
	
	String id;
	String shortId;
	int auctionsPerMinute;
	int auctionDuration;
	int updateInterval;
	int bidsPerMin;
	
	ArrayList<Integer> activeAuctions = null;
	
	private Socket socket;
	
	PrintWriter socketWriter = null;
	Scanner scanner;
	
	boolean isRunning = true;
	
	public GenericClient(String server, int port, int id, int auctionsPerMin, int auctionDuration, int updateIntervalSec, int bidsPerMin) {
		this.id = "Client - " + id;
		this.auctionsPerMinute = auctionsPerMin;
		this.auctionDuration = auctionDuration;
		this.updateInterval = updateIntervalSec;
		this.bidsPerMin = bidsPerMin;
		this.shortId = id + "";
		
		Debug.debug = false;
		
		activeAuctions = new ArrayList<Integer>();
		
		try {
			socket = new Socket(server, port);
			
			socketWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//System.out.println(this.id + " started");
	}
 
	public void run() {
		
		this.login();
		
		StartAuction auction = new StartAuction(this.shortId, this.auctionDuration, this.auctionsPerMinute, this.socketWriter);
		new Thread(auction).start();
		SetBid bid = new SetBid(this.bidsPerMin, this.socketWriter);
		new Thread(bid).start();
		
		while(LoadTest.active) {
			
			try {
				synchronized(activeAuctions) {
					activeAuctions = getActiveAuctions();
					bid.setAuctions(activeAuctions);
				}
				Thread.sleep(updateInterval * 1000);
				//LoadTest.active = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		LoadTest.active = false;
		 
	}
	
	
	private synchronized ArrayList<Integer> getActiveAuctions() {
		
		socketWriter.write("!list\r\n");
		socketWriter.flush();
		
		ArrayList<Integer> temp = new ArrayList<Integer>();
		
		try {
			BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			boolean reading = true;
			boolean skipped = false;
			
			while(socketReader.ready() && reading) {
				
				if(!skipped) {
					while(!socketReader.readLine().equals("-List of auctions")) {
						Debug.printDebug(this.id + " line skipped");
					}
					skipped = true;
				}
				
				String output = socketReader.readLine();
				
				Debug.printDebug(this.id + " line read: " + output);
				
				if(output.substring(0, 1).equals("-")) {
					reading = false;
				}
				else {
					temp.add(Integer.parseInt(output.substring(0, output.indexOf("."))));
				}
			}
		}
		catch(Exception e) {
			
		}
		
		//if(temp.size() > 0) Debug.printDebug("User " + this.id + " recieved list - " + temp.size() + " items");
		
		return temp;
	}

	private void login() {
		socketWriter.write("!login " + this.shortId + " 12345\r\n");
		socketWriter.flush();
	}
	
	synchronized void stop() {
				 		 
		 try {
			scanner.close();
			socketWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		 Debug.printInfo("Shutdown complete");
	}
}