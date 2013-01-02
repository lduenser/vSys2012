package methods;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import org.bouncycastle.util.encoders.Base64;

public class Methods {

	public static int setPort(int port) {
			
		if(port < 0 || port > 65536) {
			System.out.println("No valid port given. Using default port: 10000");
			return 10000;
		}
		else return port;
	}
	
	public static String getRandomNumber(int numbersize) {
	      
	      SecureRandom secureRandom = new SecureRandom();
	      final byte[] number = new byte[numbersize];
	      secureRandom.nextBytes(number);
	      byte[] base64Message = Base64.encode(number);
	      String feedback="";
	          try {
	                 feedback = new String(base64Message, "UTF8");
	          } catch (UnsupportedEncodingException ex) {
	                 System.out.println("random number failed");
	          }
	      return feedback;

	    }
}
