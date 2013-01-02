package methods;

public class Methods {

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
}
