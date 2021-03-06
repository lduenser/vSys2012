package management;

import java.io.IOException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
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
import billing.model.PriceSteps;

public class ManagementClient extends UnicastRemoteObject implements INotifyClient  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ManagementClient() throws RemoteException {
		super();
		this.subscription = new ArrayList<Subscription>();
		this.buffer = new ArrayList<Event>();
		this.status = Status.AUTO;
	}
	
	private enum Status {
		AUTO,
		HIDE
	}
	private ArrayList<Event> buffer = null;

	private static int argCount = 2;
	static String bindingAnalytics = "";
	static String bindingBilling = "";

	private static Scanner scanner;
	
	ArrayList<Subscription> subscription = null;
	private Status status;
	private int filterId = 0;
	
	public static void main(String args[]) throws Exception {		
		
		checkArguments(args);
		scanner = new Scanner(System.in);
		String line="";
		IBillingServer billRef = null;
		IAnalyticsServer analyticsRef = null;
		IBillingServerSecure ibss = null;
		
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
				billRef = (IBillingServer) registry.lookup(bindingBilling);
				analyticsRef = (IAnalyticsServer) registry.lookup(bindingAnalytics);
				
				boolean active = true;
				
				System.out.println("hello management client");
				
				//Falls generischer Zugriff - Subscription setzen
				if(args.length > 0) {
					if(args[2].equals("generic")) {
						if(self.subscription.isEmpty()) {
							analyticsRef.subscribe(self);
						}
						self.setSubscription("(.*)");
						
						while(active) {
							Thread.sleep(100);
						}
						
						active = false;
					}
				}
				
				
				String userInput="";
				StringTokenizer st;
				
				while(active){
					
						line = scanner.nextLine();
						st = new StringTokenizer(line);
						userInput = st.nextToken();
						
						if(userInput.startsWith("!login")){					
							if(ibss == null){
								String name=""; 
								String pwd="";
								if(st.countTokens() < 2){
									Debug.printError("username/password missing");
								}
								else{
									name = st.nextToken();
									pwd = st.nextToken(); 
									
									ibss=billRef.login(name,pwd);
					                  
				                    if (ibss==null) {
				                    	Debug.printError("username/password is not valid!");
				                    } else{ 
				                           Debug.printInfo(name+" successfully logged in");
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
							Double start, end, fixed, variable;
							if(st.countTokens() < 4){
								Debug.printError("start/end/fixed/variable missing");
							}
							else{
								try{
									start = Double.parseDouble(st.nextToken());
									end = Double.parseDouble(st.nextToken());									
									fixed = Double.parseDouble(st.nextToken());
									variable = Double.parseDouble(st.nextToken());
									
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
								catch(RemoteException re){
									Debug.printError("values are not valid");
								}
								catch(Exception e){
									Debug.printError("only numbers!");
								}						
							}					
						}
						
						else if(userInput.startsWith("!removeStep")){										
							Double start = null, end = null;
							if(st.countTokens() < 2){
								Debug.printError("start/end missing");
							}
							else{
								try{
									start = Double.parseDouble(st.nextToken());
									end = Double.parseDouble(st.nextToken());
									if(ibss!=null) {										
										ibss.deletePriceStep(start, end);									
										System.out.println("Price step ["+ start +" "+ end +"] successfully removed");
									}							
									else{
										System.out.println("ERROR: You are currently not logged in.");
									}
								}
								catch(RemoteException re){
									Debug.printError("PriceStep ["+ start +" "+ end +"] does not exist!");
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
								Debug.printError("username is missing");
							}
							else{							
								try{
									username = st.nextToken();
									if(ibss!=null){
										Bill bill = ibss.getBill(username);										
										if(bill != null){											
											System.out.println(bill.toString());
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
							if(ibss != null){
								ibss = null;
								System.out.println("Successfully logged out");
							}
							else{
								Debug.printInfo("you have to log in first!");
							}							
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
							self.status = Status.AUTO;
							Debug.printInfo("enabled the automatic printing of events");
							
						}
						else if(userInput.startsWith("!hide")){
							self.status = Status.HIDE;
							Debug.printInfo("disabled the automatic printing of events");
							
						}
						else if(userInput.startsWith("!print")){							
							Debug.printInfo("print events on buffer");
							self.printBuffer();							
						}
						
						else if(userInput.startsWith("!exit")){		
							Debug.printDebug("Shutdown M.Client");
							ibss = null;
							active = false;
							UnicastRemoteObject.unexportObject(self, true);							
						}
						
						else{
							Debug.printError("unknown command!");
						}						
					}
					
		       }	
	    	   catch(ConnectException ce){
		   			Debug.printError("could not connect to server");
	   	   		}
	    	   catch (IOException io) {
	   				io.printStackTrace();
		   		}
		   		catch (NotBoundException nbe) {
		   			Debug.printError("could not bind, sever is not available");
		   		}
		   		catch(Exception e){
		   			e.printStackTrace();
		   		}
			}
		
		scanner.close();
	}	
	
    private static void checkArguments(String[] args) throws Exception{
		if(args.length != argCount+1){
			throw new Exception("Anzahl der Argumente stimmen nicht");
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
		
		switch(this.status) {
		case AUTO:
			System.out.println(event.toString());
			break;
		case HIDE:
			this.buffer.add(event);
			break;
		}
	}
	
	public void printBuffer() {
		ArrayList<Event> copy = (ArrayList<Event>) this.buffer.clone();
		
		for(Event e:copy) {
				System.out.println(e.toString());
				this.buffer.remove(e);
		}
		
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
