package billing;

import billing.model.*;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBillingServerSecure extends Remote {

	public PriceSteps getPriceSteps();
    
    public void createPriceStep(double startPrice, double endPrice, double fixedPrice, double variablePricePercent) throws RemoteException;
    
    public void deletePriceStep(double startPrice, double endPrice) throws RemoteException;
        
    public void billAuction(String user, long auctionID, double price);
        
    public Bill getBill(String user) throws RemoteException;
	
}
