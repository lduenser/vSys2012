package loadTest.client.threads;


import java.io.PrintWriter;

import loadTest.LoadTest;
import debug.Debug;

public class StartAuction implements Runnable {
	
	String name = "";
	int duration = 0;
	int delay = 0;
	
	PrintWriter socketWriter = null;
	
	public StartAuction(String name, int duration, int delay, PrintWriter socketWriter) {
		this.name = name;
		this.duration = duration;
		this.delay = delay;
		this.socketWriter = socketWriter;
	}
 
	public void run() {
		
		while(LoadTest.active) {
			
			try {
				
				this.createAuction();
				Thread.sleep((60*1000)/delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		 
	}
	
	private void createAuction() {
		socketWriter.write("!create " + this.duration + " " + this.name + "\r\n");
		socketWriter.flush();
		
		Debug.printDebug("!create " + this.duration + " " + this.name);
	}
}