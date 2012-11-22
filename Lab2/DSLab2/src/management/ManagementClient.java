package management;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.StringTokenizer;

import debug.Debug;
import billing.IBillingServer;
import billing.IBillingServerSecure;
import billing.model.Bill;

public class ManagementClient {
	
	private static int argCount = 2;
	static String bindingAnalytics = "AnalyticsServer";
	static String bindingBilling = "BillingServer";
	private static INotifyMClient  notifyStub =null;
	private static IBillingServerSecure ibss;
	static Scanner scanner;
	
	public static void main(String args[]) {		
		
		//checkArguments(args);
		
		scanner = new Scanner(System.in);
		String line="";
		
		try {
			/*
			NotifyMClient notifyRemObj= new NotifyMClient();
            notifyStub= (INotifyMClient)UnicastRemoteObject.exportObject(notifyRemObj, 0);
			*/
            Registry registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
            IBillingServer billRef = null;
			try {
				billRef = (IBillingServer) registry.lookup(bindingBilling);
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
			String userInput="";
			StringTokenizer st;
			boolean active = true;
			
			while(active){
			
				line = scanner.nextLine();
				st = new StringTokenizer(line);
				userInput = st.nextToken();
				
				if(userInput.startsWith("!login")){
					
					String name=""; 
					String pwd="";
					if(st.countTokens() < 2){
						System.out.println("username/pwd missing");
					}
					else{
						name = st.nextToken();
						pwd = st.nextToken(); 
						
						ibss=billRef.login(name,pwd);
		                  
	                    if (ibss==null) {
	                          throw new RemoteException("Something went wrong!");
	                    } else{ 
	                           System.out.println("Successfully logged in as '" +name+ "'.");
	                      }                      
					}                    
				}
				
				if(userInput.startsWith("!steps")){
					
				}
				if(userInput.startsWith("!addStep")){
					
				}
				if(userInput.startsWith("!removeStep")){
					
				}
				
				if(userInput.startsWith("!bill")){
				     try {
				      // Bill b = ibss.getBill(username);
				    }
				    catch(Exception e){
				    	 
				    }
				}   
				if(userInput.startsWith("!logout") && (ibss != null)){
					// admin.setOnline(false); ??
					ibss = null;
					active = false;
					System.out.println("Successfully logged out.");
				}
				
			} 
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/*
	 * cmds with the billing server
	 * !login <username> <password> : Login
	 * !steps : List all existing price steps
	 * !addStep <startPrice> <endPrice> <fixedPrice> <variablePricePercent> : Add a new price step
	 * !removeStep <startPrice> <endPrice> : Remove an existing price step
	 * !bill <username> : shows the bill for a certain user name
	 * !logout : set the client into "logged out" state
	 */
	
	/*
	 * cmds with the analytics server
	 * !subscribe <filterRegex>
	 * !unsubscribe <subscriptionID>
	 * !auto
	 * !hide
	 * !print
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
