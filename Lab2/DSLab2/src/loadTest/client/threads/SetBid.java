package loadTest.client.threads;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import loadTest.LoadTest;

import debug.Debug;

public class SetBid implements Runnable {
	
	double bid = 0;
	int delay = 0;
	int auctionId = 0;
	
	ArrayList<Integer> auctions = null;
	
	PrintWriter socketWriter = null;
	
	public SetBid(int delay, PrintWriter socketWriter) {
		this.delay = delay;
		auctions = new ArrayList<Integer>();
		this.socketWriter = socketWriter;
	}
 
	public void setAuctions(ArrayList<Integer> auctions) {
		this.auctions = auctions;
	}
	
	public void run() {
		Random random = new Random();
		
		while(LoadTest.active) {
			if(auctions.size() > 0) {
				try {
					auctionId = auctions.get(random.nextInt(auctions.size()));
					long now = (new Timestamp(new java.util.Date().getTime())).getTime()/1000;
					
					bid = now-(LoadTest.startTime.getTime())/1000;
					
					this.setBid(auctionId, bid);
					
					Thread.sleep((60*1000)/delay);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		}
		 
	}
	
	private void setBid(int auctionId, double bid) {
		String temp = "!bid " + this.auctionId + " " + this.bid;
		
		socketWriter.write(temp + "\r\n");
		socketWriter.flush();
	}
}