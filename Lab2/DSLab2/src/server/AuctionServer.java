package server;

import server.threads.*;
import methods.Methods;
import methods.ReadProp;
import debug.Debug;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;

import analytics.IAnalyticsServer;
import billing.BillingServer;
import billing.IBillingServer;
import billing.IBillingServerSecure;

public class AuctionServer {

	/**
	 * @param args
	 */
	private static int port = 10290;
	private static String bindingAnalytics = "AnalyticsServer";
	private static String bindingBilling = "BillingServer";
	static int maxClients = 10000;
	static DataHandler data;
	private static int argCount = 3;
	public static boolean active = true;
	
	public static IBillingServerSecure billingServer;
	public static IAnalyticsServer analytics;
	
	public AuctionServer() {
		Debug.info = true;
		Debug.error = true;
		Debug.debug = true;
	}
	
	public static void main(String[] args) throws InterruptedException {
		
		data = new DataHandler();
		
	//	checkArguments(args);		
		
		ThreadPooledServer server = null;
		UpdateThread updater = new UpdateThread();
		ScannerThread scanner = new ScannerThread();
		Properties regProp= ReadProp.readRegistry();
		
		while(server==null) {
			try {
				server = new ThreadPooledServer(port, maxClients);
			}
			catch(Exception e) {
				port--;
				Debug.printError("Port " + (port) + " is not available. Trying to use port " + (port+1));
			}
		}
		
		new Thread(server).start();
		new Thread(updater).start();
		new Thread(scanner).start();
		
		
		String registryHost = regProp.getProperty("registry.host");				
		Integer registryPort = Integer.parseInt(regProp.getProperty("registry.port"));
		
		try {
            String name = bindingBilling;
            Registry registry = LocateRegistry.getRegistry(registryPort);            
            Debug.printInfo(registry.toString());            
            IBillingServer billing = (IBillingServer) registry.lookup(name);
          
            billingServer = billing.login("auctionClientUser", "management");            
            Debug.printInfo("Connected to secure billing server");
            
        } catch (Exception e) {
        	Debug.printInfo("Couldn't connect to secure billing server");            
            e.printStackTrace();
        }
		
		try {
            String name = bindingAnalytics;
            Registry registry = LocateRegistry.getRegistry(registryPort);            
            Debug.printInfo(registry.toString());
            
            analytics = (IAnalyticsServer) registry.lookup(name);           
            Debug.printInfo("Connected to AnalyticsServer");
            
        } catch (Exception e) {
        	Debug.printInfo("Couldn't connect to AnalyticsServer");            
            e.printStackTrace();
        }
		
		Debug.printInfo("Server started on port: "+port);
		
		while(active) {
			Thread.sleep(100);
		}
		
		Debug.printInfo("Stopping server...");
		
		server.stop();
		updater.stop();
		scanner.stop();
		
		Debug.printInfo("Server shutdown complete!");
	}
	
	private static void checkArguments(String[] args){
		if(args.length != argCount){
			System.out.println("Args Anzahl stimmt nicht");
		}
		
		for (int i = 0; i < args.length; i++) {
	            switch (i) {
		           case 0: 
		                try{	
		                	port=Integer.parseInt(args[i]);
		                } catch (Exception e) {
		                    System.out.println("Port ist kein Integerwert!");
		                }
		                port = Methods.setPort(Integer.valueOf(args[0]));		                
		                break;	                	                	
		           case 1:  bindingAnalytics=args[i]; break;
		           case 2: 	bindingBilling=args[i]; break;	 
		                
	            }
	     }
	}
}




