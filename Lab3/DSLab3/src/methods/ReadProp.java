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
			}
			catch(IOException io){
				io.printStackTrace();
			}
			finally{
				try {
					in.close();
				} catch (IOException e) {
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
                userProps.load(in);
                return userProps;                            
			}
			catch(IOException io){
				io.printStackTrace();
			}
		}
		else {
            System.out.println("User Properties file could not be found!");
        }
		
		return null;
	}
	
	public static Properties readLoadTest(){
		InputStream in = ClassLoader.getSystemResourceAsStream("loadtest.properties");
		if (in != null) {
			Properties loadProps = new Properties();
			try{				
                loadProps.load(in);
                return loadProps;                           
			}
			catch(IOException io){
				io.printStackTrace();
			}
		}
		else {
            System.out.println("LoadTest Properties file could not be found!");
        }
			
		return null;
	}
	
}
