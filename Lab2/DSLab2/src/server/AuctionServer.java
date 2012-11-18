package server;

import server.threads.*;
import methods.Methods;
import debug.Debug;


public class AuctionServer {

	/**
	 * @param args
	 */
	static int port = 5000;
	static String bindingAnalytics;
	static String bindingBilling;
	static int maxClients = 10;
	static DataHandler data;
	private static int argCount = 3;
	public static boolean active = true;
	
	public AuctionServer() {
		Debug.info = true;
		Debug.error = true;
		Debug.debug = true;
	}
	
	public static void main(String[] args) throws InterruptedException {
		
		new AuctionServer();		
		data = new DataHandler();
		
		checkArguments(args);		
				
		//port = 10290;
		
		ThreadPooledServer server = null;
		UpdateThread updater = new UpdateThread();
		ScannerThread scanner = new ScannerThread();
		
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
		/* no UDP in Lab2
		new Thread(updater).start();
		*/
		new Thread(scanner).start();
		
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




