package methods;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ReadProp {

	public static Properties readRegistry(){
		InputStream in = ClassLoader.getSystemResourceAsStream("registry.properties");		
		if (in != null) {
			Properties regProps = new Properties();
			try{				
               regProps.load(in);
               return regProps;  
               /* beim auslesen dann:
               String registryHost = regProp.getProperty("registry.host");
               Integer registryPort = Integer.parseInt(regProp.getProperty("registry.port"));             
               */
			}
			catch(IOException io){
				io.printStackTrace();
			}
			finally{
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else {
           System.out.println("Registry Properties file could not be found!");
       }
		
		return null;		
	}	
	
	public static Properties readUserProp(){
		InputStream in = ClassLoader.getSystemResourceAsStream("user.properties");
		if (in != null) {
			Properties userProps = new Properties();
			try{
			//	Admin admin;				
                userProps.load(in);
                return userProps;                            
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
	
}
