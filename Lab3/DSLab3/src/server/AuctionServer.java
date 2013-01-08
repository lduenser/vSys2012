package server;

import server.threads.*;
import methods.Methods;
import methods.ReadKeys;
import methods.ReadProp;
import debug.Debug;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Properties;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

import analytics.IAnalyticsServer;
import billing.IBillingServer;
import billing.IBillingServerSecure;

public class AuctionServer {
	
	private static int port = 10290;
	private static String bindingAnalytics = "AnalyticsServer";
	private static String bindingBilling = "BillingServer";
	static int maxClients = 10000;
	static DataHandler data;
	private static int argCount = 3;
	public static boolean active = true;
	public static boolean stop = false;
	
	public static IBillingServerSecure billingServer;
	public static IAnalyticsServer analytics;
	
	public static ThreadPooledServer server = null;
	
	public static PublicKey publickey = null;
	public static PrivateKey privatekey = null;

	private static String pathToPublicKeyUser = "keys/alice.pub.pem";
	private static String pathToPrivateKeyServer ="keys/auction-server.pem";
	
	public AuctionServer() {
		Debug.info = true;
		Debug.error = true;
		Debug.debug = true;
	}
	
	public static void main(String[] args) throws Exception {
		
		data = new DataHandler();
		
	//	checkArguments(args);		
		
		UpdateThread updater = new UpdateThread();
		ScannerThread scanner = new ScannerThread();
		Properties regProp= ReadProp.readRegistry();
		
		if(getKeysServer()){
			Debug.printInfo("reading server keys success");
		}
		
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
            IBillingServer billing = (IBillingServer) registry.lookup(name);
          
            billingServer = billing.login("auctionClientUser", "management");            
            Debug.printInfo("Connected to secure Billing Server");
            
        } 
		catch(ConnectException ce){
			Debug.printError("could not connect to secure billing server");
		}
		catch(NotBoundException nbe){
			Debug.printError("could not bound to billing server");
		}
		catch (Exception e) {  
			e.printStackTrace();            
        }
		
		try {
            String name = bindingAnalytics;
            Registry registry = LocateRegistry.getRegistry(registryPort);            
     //       Debug.printInfo(registry.toString());
            
            analytics = (IAnalyticsServer) registry.lookup(name);           
            Debug.printInfo("Connected to Analytics Server");            
        }
		catch(ConnectException ce){
			Debug.printError("could not connect to analytics server");
		}
		catch(NotBoundException nbe){
			Debug.printError("could not bound to analytics server");
		}
		catch (Exception e) {        	           
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
	
	
	@SuppressWarnings("finally")
	public static boolean getKeysServer() {
        boolean result=false;
        PEMReader inPrivat=null,inPublic = null;
        try {
            //public key from user
            try {
              inPublic = new PEMReader(new FileReader(pathToPublicKeyUser));
            } catch (Exception e) {
                 System.out.println("Can't read file for public key!");
                 return false;
            }
            publickey= (PublicKey) inPublic.readObject();

            //private key from server   
            FileReader privateKeyFile=null;
            try {
               privateKeyFile=new FileReader(pathToPrivateKeyServer);
            } catch (Exception e) {
                 System.out.println("Can't read file for private key!");
                 return false;
            }
            
            inPrivat = new PEMReader(privateKeyFile, new PasswordFinder() {
                @Override
                 public char[] getPassword() {
                    // reads the password from standard input for decrypting the private key
                    System.out.println("Enter pass phrase:");
                    try {
                        return (new BufferedReader(new InputStreamReader(System.in))).readLine().toCharArray();
                    } catch (IOException ex) {
                        return "".toCharArray();
                    }
                 }
            });

           KeyPair keyPair = (KeyPair) inPrivat.readObject();
           privatekey = keyPair.getPrivate();
           result=true;
           System.out.println("Keys successfully initialized!");
        } catch (IOException ex) {
            System.out.println("Wrong password!");
            result=getKeysServer();
        } finally {
            try {
                if (inPublic!=null) {
                  inPublic.close();
                }
                if (inPrivat!=null) {
                  inPrivat.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return result;
        }
    }
	
	public static PrivateKey getPrivateKey() {
		return privatekey;
	}
	
	public static PublicKey getPublicKey(){
		return publickey;
	}
	
	private static void checkArguments(String[] args) throws Exception{
		if(args.length != argCount){
			throw new Exception("Anzahl der Argumente stimmen nicht");
		}
		
		for (int i = 0; i < args.length; i++) {
	            switch (i) {
		           case 0: 
		                try{	
		                	port=Integer.parseInt(args[i]);
		                } catch (Exception e) {
		                    throw new Exception("Port ist kein Integerwert!");
		                }
		                port = Methods.setPort(Integer.valueOf(args[0]));		                
		                break;	                	                	
		           case 1:  bindingAnalytics=args[i]; break;
		           case 2: 	bindingBilling=args[i]; break;		                
	            }
	     }
	}
}




