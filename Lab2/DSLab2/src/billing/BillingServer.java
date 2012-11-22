package billing;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;
import java.security.*;

import debug.Debug;
import methods.ReadProp;

public class BillingServer implements IBillingServer {

	private static int argCount = 1;
	private static String bindingBilling = "BillingServer";
	
	public BillingServer() {
		
	}
	
	public static void main(String[] args){
		
		//checkArguments(args);
		
		try {
			String name = bindingBilling;
				Properties regProp= ReadProp.readRegistry();
				Integer registryPort = Integer.parseInt(regProp.getProperty("registry.port"));
            Debug.printInfo(registryPort.toString());
            Registry registry = LocateRegistry.createRegistry(registryPort);
         //   Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            
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
		}
		else{
			Debug.printDebug("falsches Passwort");
		}
				
		IBillingServerSecure engine = new BillingServerSecure();
		IBillingServerSecure stub =
            (IBillingServerSecure) UnicastRemoteObject.exportObject(engine, 0);
		
		return stub;		
		//return interface
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
