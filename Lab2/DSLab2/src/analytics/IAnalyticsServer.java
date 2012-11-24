package analytics;

import java.rmi.Remote;
import java.rmi.RemoteException;

import management.INotifyClient;

import events.Event;

public interface IAnalyticsServer extends Remote {
	public void subscribe(INotifyClient client) throws RemoteException;
	
	public void processEvent(Event event) throws RemoteException;
	
	public void unsubscribe(INotifyClient client) throws RemoteException;
}
