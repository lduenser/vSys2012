package loadTest;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;

import debug.Debug;

import loadTest.client.GenericClient;
import management.ManagementClient;
import methods.Methods;
import methods.ReadProp;

public class LoadTest {
	
	int clients = 0;
	int auctionsPerMin = 0;
	int auctionDuration = 0;
	int updateIntervalSec = 0;
	int bidsPerMin = 0;

	private static int argCount = 4;
	static String host = "localhost";
	static int serverPort = 10290;
	static String bindingAnalytics = "AnalyticsServer";
	static String bindingBilling = "BillingServer";
	
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
	
	public static void main(String args[]) throws Exception {
		System.out.println("hallo");
		
	//	checkArguments(args);
		
		Properties loadProp = ReadProp.readLoadTest();
		
		if(loadProp != null){
			try{
				Integer clients = Integer.parseInt(loadProp.getProperty("clients"));
				Integer auctionsPerMin = Integer.parseInt(loadProp.getProperty("auctionsPerMin"));
				Integer auctionDuration = Integer.parseInt(loadProp.getProperty("auctionDuration"));
				Integer updateIntervalSec = Integer.parseInt(loadProp.getProperty("updateIntervalSec"));
				Integer bidsPerMin = Integer.parseInt(loadProp.getProperty("bidsPerMin"));
	
				new LoadTest(host, serverPort, clients, auctionsPerMin, auctionDuration, updateIntervalSec, bidsPerMin);
				
			}
			catch(Exception e){
				e.printStackTrace();
			}
				
		}else{
			Debug.printError("could not find loadTest property - starting default LoadTest");
			 new LoadTest(host, serverPort, 50, 1, 120, 20, 3); 
		}
		
	}

	private static void checkArguments(String[] args) throws Exception{
		if(args.length != argCount){
			throw new Exception("Anzahl der Argumente stimmen nicht");
		}
		
		for (int i = 0; i < args.length; i++) {
	            switch (i) {
	                case 0: host=args[i]; break;
	                
	                case 1: 
	                	try{
	                		serverPort = Integer.parseInt(args[i]);
	                	}
	                	catch(Exception e){
	                		Debug.printError("serverPort is no Integer");
	                	}
	                	serverPort = Methods.setPort(Integer.valueOf(args[1]));
	                	break;
	                	
	                case 2: bindingAnalytics=args[i]; break;
	      //          case 3: bindingBilling = args[i]; break;
	            }
	     }
	}
	
}
