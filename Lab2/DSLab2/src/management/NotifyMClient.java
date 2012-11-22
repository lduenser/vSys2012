package management;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class NotifyMClient extends UnicastRemoteObject implements INotifyMClient {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected NotifyMClient() throws RemoteException {
		super();
	}

	@Override
	public String notify(String message) throws RemoteException {
		
		String returnMessage = "Callback received: " + message;
	    System.out.println(returnMessage);
	    return returnMessage;
	}

}
