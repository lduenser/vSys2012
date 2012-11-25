package loadTest;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;

import debug.Debug;

import loadTest.client.GenericClient;
import management.ManagementClient;
import methods.ReadProp;

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
		
		Properties loadProp = ReadProp.readLoadTest();
		
		if(loadProp != null){
			try{
				Integer clients = Integer.parseInt(loadProp.getProperty("clients"));
				Integer auctionsPerMin = Integer.parseInt(loadProp.getProperty("auctionsPerMin"));
				Integer auctionDuration = Integer.parseInt(loadProp.getProperty("auctionDuration"));
				Integer updateIntervalSec = Integer.parseInt(loadProp.getProperty("updateIntervalSec"));
				Integer bidsPerMin = Integer.parseInt(loadProp.getProperty("bidsPerMin"));
	
				new LoadTest("localhost", 10290, clients, auctionsPerMin, auctionDuration, updateIntervalSec, bidsPerMin);
				
			}
			catch(Exception e){
				e.printStackTrace();
			}
				
		}else{
			Debug.printError("could not find loadTest property - starting default LoadTest");
			 new LoadTest("localhost", 10290, 50, 1, 120, 20, 3); 
		}
		
	}
}
