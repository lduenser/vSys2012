package loadTest;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Date;

import loadTest.client.GenericClient;
import management.ManagementClient;

public class LoadTest {
	
	int clients = 0;
	int auctionsPerMin = 0;
	int auctionDuration = 0;
	int updateIntervalSec = 0;
	int bidsPerMin = 0;
	
	String server = null;
	int port = 0;
	
	public static Timestamp startTime = null;
	
	public static boolean active = true;
	
	public LoadTest(String server, int port, int clients, int auctionsPerMin, int auctionDuration, int updateIntervalSec, int bidsPerMin) {
		this.server = server;
		this.port = port;
		
		this.clients = clients;
		this.auctionsPerMin = auctionsPerMin;
		this.auctionDuration = auctionDuration;
		this.updateIntervalSec = updateIntervalSec;
		this.bidsPerMin = bidsPerMin;
		
		Date date = new java.util.Date();
		startTime = new Timestamp(date.getTime());
		
		for(int i=0; i<clients; i++) {
			GenericClient temp = new GenericClient(this.server, this.port, i, this.auctionsPerMin, this.auctionDuration, this.updateIntervalSec, this.bidsPerMin);
			new Thread(temp).start();
		}
	}
	
	public static void main(String args[]) {
		System.out.println("hallo");
		
		new LoadTest("localhost", 10290, 50, 1, 2*60, 20, 3); 
	}
}
