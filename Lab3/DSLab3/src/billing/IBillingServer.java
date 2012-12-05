package billing;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBillingServer extends Remote {

	public IBillingServerSecure login(String username, String password) throws RemoteException;
}
