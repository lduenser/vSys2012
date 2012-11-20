package analytics;

import java.rmi.Remote;
import java.rmi.RemoteException;

import events.Event;

public interface IAnalyticsServer extends Remote {
	public String subscribe(String client, String filter) throws RemoteException;
	
	public void processEvent(Event event) throws RemoteException;
	
	public boolean unsubscribe(String id) throws RemoteException;
}
