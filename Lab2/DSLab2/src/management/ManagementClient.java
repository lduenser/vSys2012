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
import billing.model.PriceStep;
import billing.model.PriceSteps;

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
	                           System.out.println(name+" successfully logged in");
	                      }                      
					}                    
				}
				// List all existing price steps
				else if(userInput.startsWith("!steps")){
					if(ibss!=null){
						PriceSteps steps;
						try{
							steps = ibss.getPriceSteps();							
							System.out.println("Price Steps\r\n" + steps.toString());
						}
						catch(Exception e){
							System.out.println("could not print PriceSteps");
						}						
					}
					else{
						System.out.println("You have to log in first!");
					}					
				}
				
				else if(userInput.startsWith("!addStep")){
					Long start, end, fixed, variable;
					if(st.countTokens() < 4){
						System.out.println("start/end/fixed/variable missing");
					}
					else{
						try{
							start = Long.parseLong(st.nextToken());
							end = Long.parseLong(st.nextToken());
							
							fixed = Long.parseLong(st.nextToken());
							variable = Long.parseLong(st.nextToken());
							if(ibss!=null){
								ibss.createPriceStep(start, end, fixed, variable);
								if(end == 0){
									String inf = "INFINITY";
									System.out.println("Step ["+ start +" "+ inf +"] successfully added");
								}
								else{
									System.out.println("Step ["+ start +" "+ end +"] successfully added");
								}								
							}
							else{
								System.out.println("You have to log in first!");
							}
						}
						catch(Exception e){
							System.out.println("only numbers!");
						}						
					}					
				}
				
				else if(userInput.startsWith("!removeStep")){										
					Long start, end;
					if(st.countTokens() < 2){
						System.out.println("start/end missing");
					}
					else{
						try{
							start = Long.parseLong(st.nextToken());
							end = Long.parseLong(st.nextToken());
							if(ibss!=null){
								ibss.deletePriceStep(start, end);
							}
							else{
								System.out.println("You have to log in first!");
							}
						}
						catch(Exception e){
							System.out.println("only numbers!");
						}
					}
				}
				
				else if(userInput.startsWith("!bill")){
					String username;
					if(st.countTokens() < 1){
						System.out.println("username missing");
					}
					else{							
						try{
							username = st.nextToken();
							if(ibss!=null){
								Bill bill = ibss.getBill(username);
								System.out.println("bill: ");
								bill.toString();
							}
							else{
								System.out.println("You have to log in first!");
							}
						}
						catch(Exception e){
						    e.printStackTrace();	 
						}
					}
				}   
				else if(userInput.startsWith("!logout")){
					// admin.setOnline(false); ??
					ibss = null;
					// active = false;
					System.out.println("Successfully logged out");
				}
				else{
					System.out.println("unknown command");
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
