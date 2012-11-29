package loadTest.client.threads;

import management.ManagementClient;

public class StartManagementClient implements Runnable {
	
	String bindingAnalytics = "";
	String bindingBilling = "";
	
	public StartManagementClient(String bindingAnalytics, String bindingBilling) {
		this.bindingAnalytics = bindingAnalytics;
		this.bindingBilling = bindingBilling;
	}
 
	public void run() {
		String args[] = new String[3];
		args[0] = bindingAnalytics;
		args[1] = bindingBilling;
		args[2] = "generic";
		
		try {
			ManagementClient.main(args);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}