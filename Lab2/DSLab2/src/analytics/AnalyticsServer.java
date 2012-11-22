package analytics;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Properties;

import methods.ReadProp;
import analytics.model.StatisticsEventData;
import debug.Debug;
import events.AuctionEvent;
import events.BidEvent;
import events.Event;
import events.StatisticsEvent;
import events.UserEvent;

// receives events from the system and computes simple statistics/analytics
public class AnalyticsServer implements IAnalyticsServer {
	
	public StatisticsEventData statistics;
	
	public AnalyticsServer() {
		statistics = new StatisticsEventData();
	}

	public static void main(String[] args){
		
		String name = "AnalyticsServer";
		Properties regProp= ReadProp.readRegistry();
		
		if (regProp==null) {
            System.out.println("Reg.Properties file could not be found!");
       } else {	    	        
			try {				     
				String registryHost = regProp.getProperty("registry.host");				
				Integer registryPort = Integer.parseInt(regProp.getProperty("registry.port"));
				Registry registry = LocateRegistry.getRegistry(registryPort);
				   
	         //   Registry registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
	            
	            Debug.printInfo(registry.toString());
	            
	            IAnalyticsServer engine = new AnalyticsServer();
	            IAnalyticsServer stub =
	                (IAnalyticsServer) UnicastRemoteObject.exportObject(engine, 0);
	            
	            registry.rebind(name, stub);
	            Debug.printInfo("AnalyticsServer started");
	        }
			catch (Exception e) {
	            Debug.printInfo("Couldn't start AnalyticsServer");
	            e.printStackTrace();
	        }
       }
	}
	
	@Override
	public String subscribe(String client, String filter)
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void processEvent(Event event) throws RemoteException {
		// TODO Auto-generated method stub
		
		ArrayList<Event> send = new ArrayList<Event>();
		send.add(event);
		
		if(event.getType().equals(AuctionEvent.types.AUCTION_STARTED)) {
			
		}
		if(event.getType().equals(AuctionEvent.types.AUCTION_ENDED)) {
			statistics.endAuction();
			
			StatisticsEvent temp = new StatisticsEvent(StatisticsEvent.types.AUCTION_TIME_AVG, statistics.getAUCTION_TIME_AVG());
			send.add(temp);
		}
		
		if(event.getType().equals(UserEvent.types.USER_LOGIN)) {
			statistics.addUserToList((UserEvent)event);
		}
		if(event.getType().equals(UserEvent.types.USER_LOGOUT)) {
			statistics.logoutUser((UserEvent)event);
			
			StatisticsEvent temp = new StatisticsEvent(StatisticsEvent.types.USER_SESSIONTIME_AVG, statistics.getUSER_SESSIONTIME_AVG());
			send.add(temp);
		}
		
		if(event.getType().equals(BidEvent.types.BID_PLACED)) {
			
		}		
		if(event.getType().equals(BidEvent.types.BID_OVERBID)) {
			
		}		
		if(event.getType().equals(BidEvent.types.BID_WON)) {
			
		}
		
		Debug.printInfo(event.getType().toString());
		
	}

	@Override
	public boolean unsubscribe(String id) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}
}
