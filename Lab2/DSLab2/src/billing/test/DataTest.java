package billing.test;

import java.rmi.RemoteException;

import debug.Debug;
import billing.BillingServerSecure;
import billing.model.PriceStep;
import billing.model.PriceSteps;

public class DataTest {
	
	public DataTest() {
		
		PriceStep step1 = new PriceStep(1.0, 10.0, 5.0, 2.3);
		PriceStep step2 = new PriceStep(11.0, 100.0, 5.0, 4.3);
		
		BillingServerSecure server;
		try {
			server = new BillingServerSecure();
			
			server.createPriceStep(1.0, 10.0, 5.0, 2.3);
			server.createPriceStep(11.0, 100.0, 5.0, 4.3);
			
			server.createPriceStep(10.0, 11.0, 5.0, 4.3);
			
			server.createPriceStep(100.0, 0, 5.0, 4.3);
			
			server.createPriceStep(0.0, 1.0, 5.0, 4.3);
			
			//server.deletePriceStep(100.0, 0);
			
			server.billAuction("hans", 01, 500.0);
			server.billAuction("hans", 02, 100.0);
			server.billAuction("hans", 03, 5.0);
			server.billAuction("peter", 04, 5.0);
			server.billAuction("peter", 05, 1.0);
			server.billAuction("fred", 06, 50.0);
			server.billAuction("hans", 07, 800.0);
			
			server.getBill("fred");
			
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
}
