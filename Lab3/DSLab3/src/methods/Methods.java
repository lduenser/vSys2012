package methods;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import org.bouncycastle.util.encoders.Base64;

public class Methods {
	
	public static final String B64 = "a-zA-Z0-9/+";

	public static int setPort(int port) {
			
		if(port < 0 || port > 65536) {
			System.out.println("No valid port given. Using default port: 10000");
			return 10000;
		}
		else return port;
	}
	
	public static long getTimeStamp() {
		return System.currentTimeMillis();
	}
	
	public static int getRandomInt(int max) {
		SecureRandom secureRandom = new SecureRandom();
		
		return secureRandom.nextInt(max);

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
	
    public static boolean exists(String dir, String filename) {
        try {
            File myFile = new File(dir+"/"+ filename);
            return myFile.exists();
        } catch (Exception e) {
            return false;
        }
    }
	
	public static String bytes2String(byte[] bytes) {
	    StringBuilder string = new StringBuilder();
	    for (byte b : bytes) {
	        String hexString = Integer.toHexString(0x00FF & b);
	        string.append(hexString.length() == 1 ? "0" + hexString : hexString);
	    }
	    return string.toString();
	}
}
