package billing;

import java.rmi.RemoteException;

public class BillingServerSecure implements IBillingServerSecure {

	@Override
	public PriceStep getPriceSteps() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createPriceStep(double startPrice, double endPrice,
			double fixedPrice, double variablePricePercent)
			throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deletePriceStep(double startPrice, double endPrice)
			throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void billAuction(String user, long auctionID, double price) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Bill getBill(String user) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}