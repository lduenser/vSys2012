package management;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import billing.Bill;

public class ManagementClient {
	
	private static int argCount = 2;
	static String bindingAnalytics;
	static String bindingBilling;
	
	public static void main(String args[]) {		
		
		checkArguments(args);
	}
	
	/*
	 * cmds with the billing server
	 * !login <username> <password> : Login
	 * !steps : List all existing price steps
	 * !addStep <startPrice> <endPrice> <fixedPrice> <variablePricePercent> : Add a new price step
	 * !removeStep <startPrice> <endPrice> : Remove an existing price step
	 * !bill <username> : shows the bill for a certain user name
	 * !logout : set the client into "logged out" state
	 * 
	 */
	
	/*
	 * cmds with the analytics server
	 * !subscribe <filterRegex>
	 * !unsubscribe <subscriptionID>
	 * !auto
	 * !hide
	 * !print
	 * 
	 */
	
	/*
	BufferedReader inFromUser = new BufferedReader(new InputStreamReader (System.in));
	
	line = inFromUser.readLine();
    tokens = new StringTokenizer(line);
	userInput = tokens.nextToken();
		
	if(userInput.startsWith("!bill ")){
	     try {
	       Bill b = bss.getBill(username);
	    }
	    
	 */
	
	
    private static void checkArguments(String[] args){
		if(args.length != argCount){
			System.out.println("Args Anzahl stimmt nicht");
		}
		
		for (int i = 0; i < args.length; i++) {
	            switch (i) {          	
		           case 0:  bindingAnalytics=args[i]; break;
		           case 1: 	bindingBilling=args[i]; break;		                
	            }
	     }
	}

}
