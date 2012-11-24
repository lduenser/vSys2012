package billing;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;
import java.util.Scanner;
import java.security.*;

import debug.Debug;
import methods.ReadProp;

public class BillingServer implements IBillingServer {

	private static int argCount = 1;
	private static String bindingBilling = "BillingServer";
	private static Scanner scanner;
	
	public BillingServer() {		
	}
	
	public static void main(String[] args) throws RemoteException{
		
		//checkArguments(args);
		boolean active = true;
		scanner = new Scanner(System.in);
		String line;
		Properties regProp= ReadProp.readRegistry();
		String name = bindingBilling;
		Registry registry = null;
		
		if(regProp == null){
			System.out.println("Reg.Properties file could not be found!");
		}		
		else{						
			String registryHost = regProp.getProperty("registry.host");	
			Integer registryPort = Integer.parseInt(regProp.getProperty("registry.port"));
          			
			try {			
				registry = LocateRegistry.createRegistry(registryPort);	
			}
			catch(ExportException ee){
				Debug.printDebug("registry already created");
				registry = LocateRegistry.getRegistry(registryHost, registryPort);
			}
			
			try {        
	            	            
	            IBillingServer engine = new BillingServer();
	            IBillingServer stub =
	                (IBillingServer) UnicastRemoteObject.exportObject(engine, 0);
	            
	            registry.rebind(name, stub);
	            Debug.printInfo("BillingServerEngine started");
	            
	            while(active){
	            	line = scanner.nextLine();					
					if(line.startsWith("!exit")){
						Debug.printInfo("Shutdown BillingServer");
						active = false;
						// registry.unbind() ??
						UnicastRemoteObject.unexportObject(engine, true);
					}
					else{
						Debug.printInfo("unknown command");
					}
	            }
	        }
			catch (Exception e) {
	            Debug.printInfo("Couldn't start BillingServerEngine");
	            e.printStackTrace();
	        }
			
		}
		scanner.close();		
	}
	
	@Override
    public IBillingServerSecure login(String username, String password) throws RemoteException {		
		String hashword = null;
		Properties prop = null;
		String secret = null;
		try {
			prop = ReadProp.readUserProp();
			secret = prop.getProperty(username);
			
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(password.getBytes());
			BigInteger hash = new BigInteger(1, md.digest());
			hashword = hash.toString(16);
		
			if(hashword.length() == 31){
				hashword = "0"+hashword;
			}
			
		} catch (NoSuchAlgorithmException nse) {
			nse.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		Debug.printDebug("secret: "+secret);
		Debug.printDebug("hash: "+hashword);
		
		if(hashword.equals(secret)){
			Debug.printInfo("richtiges pwd");
			// hier stub etc .. dann einfuegen
			// return stub;
		}
		else{
			Debug.printDebug("falsches Passwort");
			// return null;
		}
				
		IBillingServerSecure engine = new BillingServerSecure();
		IBillingServerSecure stub =
            (IBillingServerSecure) UnicastRemoteObject.exportObject(engine, 0);
		
		return stub;
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
