package management;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface INotifyMClient extends Remote {

	public String notify(String message) throws RemoteException;
	
}
