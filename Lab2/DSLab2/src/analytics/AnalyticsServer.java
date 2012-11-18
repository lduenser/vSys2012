package analytics;

import events.Event;

// receives events from the system and computes simple statistics/analytics
public class AnalyticsServer {

	private static int argCount = 1;
	private static String bindingAnalytics;
	
	public static void main(String[] args){
		
		checkArguments(args);
		
		
		
	}
	
	//public String subscribe()
	//processEvent(Event event)	
	//unsubscribe(String subID)
	
	
	private static void checkArguments(String[] args){
		if(args.length != argCount){
			System.out.println("Args Anzahl stimmt nicht");
		}
		
		for (int i = 0; i < args.length; i++) {
	            switch (i) {
	                case 0: bindingAnalytics=args[i]; break;	                	                	
	            }
	     }
	}
}
