package management;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;

import management.model.Subscription;
import methods.ReadProp;
import debug.Debug;
import events.Event;
import analytics.IAnalyticsServer;
import billing.IBillingServer;
import billing.IBillingServerSecure;
import billing.model.Bill;
import billing.model.PriceStep;
import billing.model.PriceSteps;

public class ManagementClient extends UnicastRemoteObject implements INotifyClient  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected ManagementClient() throws RemoteException {
		super();
		this.subscription = new ArrayList<Subscription>();
		// TODO Auto-generated constructor stub
	}

	private static int argCount = 2;
	static String bindingAnalytics = "AnalyticsServer";
	static String bindingBilling = "BillingServer";
	
	private static IBillingServerSecure ibss;
	private static Scanner scanner;
	
	ArrayList<Subscription> subscription = null;
	private int filterId = 0;
	
	public static void main(String args[]) {		
		
		//checkArguments(args);
		
		scanner = new Scanner(System.in);
		String line="";
		IBillingServer billRef = null;
		IAnalyticsServer analyticsRef = null;
		
		ManagementClient self = null;
				
		Properties regProp= ReadProp.readRegistry();
		
		if (regProp==null) {
            System.out.println("Reg.Properties file could not be found!");
        } 
		else {		
    	   try {
    		   self = new ManagementClient();
    		   
    		   	String registryHost = regProp.getProperty("registry.host");
				Integer registryPort = Integer.parseInt(regProp.getProperty("registry.port"));
			    Registry registry = LocateRegistry.getRegistry(registryHost, registryPort);				       
			    //  Registry registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);				            
				billRef = (IBillingServer) registry.lookup(bindingBilling);
				analyticsRef = (IAnalyticsServer) registry.lookup(bindingAnalytics);
				
				Debug.printInfo("hello ...");
				
				String userInput="";
				StringTokenizer st;
				boolean active = true;
					
					while(active){
					
						line = scanner.nextLine();
						st = new StringTokenizer(line);
						userInput = st.nextToken();
						
						if(userInput.startsWith("!login")){					
							if(ibss == null){
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
				                          throw new RemoteException("ERROR: Something went wrong!");
				                    } else{ 
				                           System.out.println(name+" successfully logged in");
				                      }                      
								}
							}
							else{
								Debug.printError("you are already logged in");
							}							                    
						}
						
						/*
						 * ! BILLING:
						 */
						
						// List all existing price steps
						else if(userInput.startsWith("!steps")){
							if(ibss!=null){
								PriceSteps steps;
								try{
									steps = ibss.getPriceSteps();							
									System.out.println("Price Steps\r\n" + steps.toString());
								}
								catch(Exception e){
									Debug.printError("could not print PriceSteps");
								}						
							}
							else{
								System.out.println("ERROR: You are currently not logged in.");
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
										System.out.println("ERROR: You are currently not logged in.");
									}
								}
								catch(Exception e){
									Debug.printError("only numbers!");
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
									//	System.out.println("Price step ["+ start +" "+ end +"] successfully removed");
									
									}							
									else{
										System.out.println("ERROR: You are currently not logged in.");
									}
								}
								catch(Exception e){
									Debug.printError("only numbers!");
								}
							}
						}
						// shows the bill for a certain user name
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
										if(bill != null){
											bill.toString();
										}
										else{
											Debug.printError("could not print bill");
										}
									}
									else{
										System.out.println("ERROR: You are currently not logged in.");
									}
								}
								catch(Exception e){
								    e.printStackTrace();	 
								}
							}
						}  
						// set the client into "logged out" state
						else if(userInput.startsWith("!logout")){					
							// admin.setOnline(false); ??
							ibss = null;
							// active = false;
							System.out.println("Successfully logged out");
						}
						
						/*
						 *  ! ANALYTICS:
						 */
						
						// !subscribe <filterRegex>
						else if(userInput.startsWith("!subscribe")){
							if(st.hasMoreTokens()){
								String filter = st.nextToken();
								int id = 0;
								filter = filter.replace("\'","");
								
								if(self.subscription.isEmpty()) {
									analyticsRef.subscribe(self);
								}
								
								id = self.setSubscription(filter);
								
								Debug.printInfo("Created subscription with ID " + id + " for events " +
										"using filter " +filter+ " .");
							}
							else{
								Debug.printError("Filter is missing");
							}							
						}
						// !unsubscribe <subscriptionID>
						else if(userInput.startsWith("!unsubscribe")){
							int id;
							if(st.countTokens() < 1){
								System.out.println("ID missing");
							}
							else{
								try{
									id = Integer.parseInt(st.nextToken());

									self.removeSubscription(id);
									
									if(self.subscription.isEmpty()) {
										analyticsRef.unsubscribe(self);
									}
									
									Debug.printInfo("subscription " +id+ " terminated");
								}
								catch(Exception e){
									Debug.printError("ID has to be a number");
								}																
							}							
						}
						else if(userInput.startsWith("!auto")){
							Debug.printInfo("enabled the automatic printing of events");
							
						}
						else if(userInput.startsWith("!hide")){
							Debug.printInfo("disabled the automatic printing of events");
							
						}
						else if(userInput.startsWith("!print")){
							
							
						}
						
						else{
							Debug.printError("unknown command!");
						}						
					}
					
		       }	
	    	   catch (IOException io) {
	   			io.printStackTrace();
		   		}
		   		catch (NotBoundException nbe) {
		   			// TODO Auto-generated catch block
		   			nbe.printStackTrace();
		   		}
		   		catch(Exception e){
		   			e.printStackTrace();
		   		}
			}
		scanner.close();
	}	
	
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

	@Override
	public void eventRecieved(Event event) throws RemoteException {
		// TODO Event ausgeben
		
		System.out.println(event.toString());
	}
	
	public int setSubscription(String newSub) {
		filterId++;
		this.subscription.add(new Subscription(this.filterId, newSub));
		return filterId;
	}
	
	private boolean removeSubscription(int id) {
		
		for(Subscription s:subscription) {
			if(s.getId() == id) {
				subscription.remove(s);
				return true;
			}
		}
		return false;
	}

	@Override
	public String getSubscription() throws RemoteException {
		
		String temp = "";
		
		for(Subscription s:subscription) {
			temp = temp + s.getFilterString() + "|";
		}
		
		return temp;
	}

}
