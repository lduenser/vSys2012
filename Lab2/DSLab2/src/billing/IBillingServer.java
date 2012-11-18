package billing;

import java.rmi.RemoteException;

public interface IBillingServer {

	public IBillingServerSecure login(String username, String password) throws RemoteException;
}
