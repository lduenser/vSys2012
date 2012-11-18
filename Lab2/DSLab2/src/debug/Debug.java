package debug;

public class Debug {
	
	public static boolean info = true;
	public static boolean error = true;
	public static boolean debug = true;
	
	public static void printInfo(String str) {
		if(info)print("info", str);
	}
	public static void printError(String str) {
		if(error)print("error", str);
	}
	public static void printDebug(String str) {
		if(debug)print("debug", str);
	}
	
	private static void print(String type, String string) {
		String output = "";
		
		if(type.equals("info")) {
			output+= "[INFO] - ";
		}
		else if(type.equals("error")) {
			output+= "[ERROR] - ";
		}
		else if(type.equals("debug")) {
			output+= "[DEBUG] - ";
		}
		else {
			output+= "[ ] - ";
		}
		output+=string;
		
		System.out.println(output);
	}
}
