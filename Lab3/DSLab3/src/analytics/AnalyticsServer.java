package analytics;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import management.INotifyClient;
import methods.ReadProp;
import analytics.model.StatisticsEventData;
import debug.Debug;
import events.AuctionEvent;
import events.BidEvent;
import events.Event;
import events.StatisticsEvent;
import events.UserEvent;

public class AnalyticsServer implements IAnalyticsServer {
	
	private static int argCount = 1;
	public StatisticsEventData statistics;
	private static String bindingAnalytics = "AnalyticsServer";
	private static Scanner scanner;
	
	private ArrayList<INotifyClient> clients;
	
	public AnalyticsServer() {
		statistics = new StatisticsEventData();
		clients = new ArrayList<INotifyClient>();
	}

	public static void main(String[] args) throws Exception{
		
	//	checkArguments(args);
		boolean active = true;
		scanner = new Scanner(System.in);
		String line;
		
		String name = bindingAnalytics;
		Properties regProp= ReadProp.readRegistry();
		Registry registry = null;
				
		if (regProp==null) {
            System.out.println("Reg.Properties file could not be found!");
       } else {	  
    	    String registryHost = regProp.getProperty("registry.host");				
			Integer registryPort = Integer.parseInt(regProp.getProperty("registry.port"));
    	   
			try {			
				registry = LocateRegistry.createRegistry(registryPort);				
			}
			catch(ExportException ee){
				Debug.printDebug("registry already created");
				registry = LocateRegistry.getRegistry(registryHost, registryPort);
			}
			
			try{    
	            
	            IAnalyticsServer engine = new AnalyticsServer();
	            IAnalyticsServer stub =
	                (IAnalyticsServer) UnicastRemoteObject.exportObject(engine, 0);
	          
	            registry.rebind(name, stub);
	            Debug.printInfo("AnalyticsServer started");
	            
	            while(active){
	            	line = scanner.nextLine();					
					if(line.startsWith("!exit")){
						Debug.printInfo("Shutdown AnalyticsServer");
						active = false;							
						UnicastRemoteObject.unexportObject(engine, true);
						registry.unbind(name);
					}
					else{
						Debug.printInfo("unknown command");
					}
	            }	            
	        }
			catch(ConnectException ce){
				Debug.printError("connection to server failed");
			}
			catch (Exception e) {
	            Debug.printError("Could not start AnalyticsServer");
	            e.printStackTrace();
	        }
       }		
		scanner.close();		
	}
	
	@Override
	public void processEvent(Event event) throws RemoteException {
		
		ArrayList<Event> send = new ArrayList<Event>();
		send.add(event);
		
		
		if(event.getType().equals(AuctionEvent.types.AUCTION_STARTED.toString())) {
			
			AuctionEvent e = (AuctionEvent)event;
			statistics.addAuction(e.getDuration());
			StatisticsEvent temp = new StatisticsEvent(StatisticsEvent.types.AUCTION_TIME_AVG, statistics.getAUCTION_TIME_AVG());
			send.add(temp);
		}
		if(event.getType().equals(AuctionEvent.types.AUCTION_ENDED.toString())) {
			statistics.endAuction();
			
			StatisticsEvent temp = new StatisticsEvent(StatisticsEvent.types.AUCTION_SUCCESS_RATIO, statistics.getAUCTION_SUCCESS_RATIO());
			send.add(temp);
		}
		
		if(event.getType().equals(UserEvent.types.USER_LOGIN.toString())) {
			statistics.addUserToList((UserEvent)event);
		}
		if(event.getType().equals(UserEvent.types.USER_LOGOUT.toString()) || event.getType().equals(UserEvent.types.USER_DISCONNECTED.toString())) {
			statistics.logoutUser((UserEvent)event);
			
			StatisticsEvent temp = new StatisticsEvent(StatisticsEvent.types.USER_SESSIONTIME_MIN, statistics.getUSER_SESSIONTIME_MIN());
			send.add(temp);
			temp = new StatisticsEvent(StatisticsEvent.types.USER_SESSIONTIME_MAX, statistics.getUSER_SESSIONTIME_MAX());
			send.add(temp);
			temp = new StatisticsEvent(StatisticsEvent.types.USER_SESSIONTIME_AVG, statistics.getUSER_SESSIONTIME_AVG());
			send.add(temp);
			
		}
		
		if(event.getType().equals(BidEvent.types.BID_PLACED.toString())) {
			BidEvent bid = (BidEvent)event;
			statistics.addBid();
			
			StatisticsEvent temp = new StatisticsEvent(StatisticsEvent.types.BID_COUNT_PER_MINUTE, statistics.getBID_COUNT_PER_MINUTE());
			send.add(temp);
			
			if(statistics.checkPriceMax(bid.getPrice())) {
				temp = new StatisticsEvent(StatisticsEvent.types.BID_PRICE_MAX, statistics.getBID_PRICE_MAX());
				send.add(temp);
			}
			
		}		
		if(event.getType().equals(BidEvent.types.BID_OVERBID.toString())) {
			
		}		
		if(event.getType().equals(BidEvent.types.BID_WON.toString())) {
			statistics.bidWon();
		}
		
		Debug.printDebug("Send size: " + send.size());
		
		//Sende Events an alle Clients
		for(Event e:send) {
			Debug.printDebug(e.toString());
			notifyClients(e);
		}
	}
	
	private void notifyClients(Event event) {
		
		try {
			for(INotifyClient client:clients) {
				try {
					Debug.printDebug("Compare String: " + event.getType() + " with " + client.getSubscription());
					if(event.getType().matches(client.getSubscription())) {
						client.eventRecieved(event);
					}
				}
				catch(Exception e) {
					clients.remove(client);
				}
			}
		}
		catch(Exception e) {
			
		}
		
	}

	private static void checkArguments(String[] args) throws Exception{
		if(args.length != argCount){
			throw new Exception("Anzahl der Argumente stimmen nicht");
		}
		
		for (int i = 0; i < args.length; i++) {
	            switch (i) {
	                case 0: bindingAnalytics=args[i]; break;	                	                	
	            }
	     }
	}

	@Override
	public void subscribe(INotifyClient client) throws RemoteException {		
		Debug.printDebug("New Subscription: " + client.getSubscription());
		clients.add(client);
	}

	@Override
	public void unsubscribe(INotifyClient client) throws RemoteException {		
		Debug.printDebug("Remove Subscription: " + client.getSubscription());
		clients.remove(client);
	}
}
