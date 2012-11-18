package billing;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.StringTokenizer;

import management.Admin;

public class BillingServer implements IBillingServer {

	private static int argCount = 1;
	private static String bindingBilling;
	
	public static void main(String[] args){
		
		checkArguments(args);				
	}
	
	@Override
    public IBillingServerSecure login(String username, String password) throws RemoteException {
		String pwd = readUserProp(username);
		if(pwd != null){
			//check
		}
		
        return null;
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
