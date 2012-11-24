package billing;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import debug.Debug;
import billing.model.*;

public class BillingServerSecure implements IBillingServerSecure {

	private static PriceSteps steps;
	private static Bill bills;
	
	public BillingServerSecure()  throws RemoteException {
		steps = new PriceSteps();
		bills = new Bill();
	}
	
	@Override
	public PriceSteps getPriceSteps()  throws RemoteException {
		// TODO Auto-generated method stub
		return steps;
	}

	@Override
	public void createPriceStep(double startPrice, double endPrice,
			double fixedPrice, double variablePricePercent)
			throws RemoteException {
		
		PriceStep step = new PriceStep(startPrice, endPrice, fixedPrice, variablePricePercent);
		
		switch(steps.checkAddStep(step)) {
			case NONE:
				Debug.printDebug("No error");
				steps.addStep(step);
				break;

			case COLLISION:
				Debug.printDebug("Collision with existing step");
				
				break;
				
			case NEGATIVE:
				Debug.printDebug("Negative values");
				
				break;
		}
		
		Debug.printInfo("Price Steps\r\n" + steps.toString());

	}

	@Override
	public void deletePriceStep(double startPrice, double endPrice)
			throws RemoteException {
		
		PriceStep step = new PriceStep(startPrice, endPrice);
		
		switch(steps.checkRemoveStep(step)) {
			case NONE:
				Debug.printDebug("No error");
				steps.removeStep(step);
				
				break;
			
			case NOT_FOUND:
				Debug.printDebug("Step not found");
				break;
		}
		
		Debug.printInfo("Price Steps\r\n" + steps.toString());		
	}

	@Override
	public void billAuction(String user, long auctionID, double price) throws RemoteException {
		// TODO Auto-generated method stub
		
		BillLine temp = new BillLine(user, auctionID, price);
		
		bills.addLine(temp);
		
		Debug.printInfo("BillLine for "+user+ " created!\r\n" + temp.toString());	
	}

	@Override
	public Bill getBill(String user) throws RemoteException {
		// TODO Auto-generated method stub
		
		Bill temp = bills.getUserBill(user, steps);
		
		Debug.printInfo("Bill for "+user+ "\r\n" + temp.toString());
		
		return temp;
	}

}
