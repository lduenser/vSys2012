package billing;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;
import java.util.StringTokenizer;
import java.security.*;

import debug.Debug;

import management.Admin;

public class BillingServer implements IBillingServer {

	private static int argCount = 1;
	private static String bindingBilling = "BillingServer";
	
	public BillingServer() {
		
	}
	
	public static void main(String[] args){
		
		//checkArguments(args);
		
		try {
			String name = bindingBilling;
            
            Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            
            Debug.printInfo(registry.toString());
            
            IBillingServer engine = new BillingServer();
            IBillingServer stub =
                (IBillingServer) UnicastRemoteObject.exportObject(engine, 0);
            
            registry.rebind(name, stub);
            Debug.printInfo("BillingServerEngine started");
        }
		catch (Exception e) {
            Debug.printInfo("Couldn't start BillingServerEngine");
            e.printStackTrace();
        }
		
						
	}
	
	@Override
    public IBillingServerSecure login(String username, String password) throws RemoteException {
		/*
		String pwd = "";
		try {
			MessageDigest md = MessageDigest.getInstance(pwd);
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		*/
		
		//pwd = readUserProp(username);
		//if(pwd != null) {
			//check
		//}
		
		
		IBillingServerSecure engine = new BillingServerSecure();
		IBillingServerSecure stub =
            (IBillingServerSecure) UnicastRemoteObject.exportObject(engine, 0);
		
		return stub;
		
		//return interface
    }
	
	public String readUserProp(String username){
		InputStream in = ClassLoader.getSystemResourceAsStream("user.properties");
		StringTokenizer token;
		if (in != null) {
			try{
				Admin admin;
				Properties userProps = new Properties();
                userProps.load(in);
                                
			}
			catch(IOException io){
				io.printStackTrace();
			}
		}
		else {
            // user.properties could not be found
            System.out.println("User Properties file could not be found!");
        }
		
		//return password
		return null;
	}
	
	public int readRegistry(){
		int port = 0;
		InputStream in = ClassLoader.getSystemResourceAsStream("registry.properties");
		StringTokenizer token;
		if (in != null) {
			try{
				Properties regProps = new Properties();
                regProps.load(in);
                                
			}
			catch(IOException io){
				io.printStackTrace();
			}
		}
		else {
            // user.properties could not be found
            System.out.println("Registry Properties file could not be found!");
        }
		
		return port;		
	}
	
	private static void checkArguments(String[] args){
		if(args.length != argCount){
			System.out.println("Args Anzahl stimmt nicht");
		}
		
		for (int i = 0; i < args.length; i++) {
	            switch (i) {
	                case 0: bindingBilling=args[i]; break;	                	                	
	            }
	     }
	}
	
	
}
