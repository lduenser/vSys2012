package loadTest.client.threads;

import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Random;

import loadTest.LoadTest;
import management.ManagementClient;


public class StartManagementClient implements Runnable {
	
	public StartManagementClient() {
		
	}
 
	public void run() {
		String args[] = new String[1];
		args[0] = "generic";
		try {
			ManagementClient.main(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}