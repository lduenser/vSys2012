package management;

import java.rmi.Remote;
import java.rmi.RemoteException;

import events.Event;

public interface INotifyClient extends Remote {

	public void eventRecieved(Event event) throws RemoteException;
	
	public String getSubscription() throws RemoteException;
}
