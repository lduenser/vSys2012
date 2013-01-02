package lab3;

/**
 *
 * @author Ternek Marianne 0825379
 * Jänner 2012
 */

import java.rmi.RemoteException;

public class NotifyClient implements INotifyClient {
    public NotifyClient() throws RemoteException
    {

    }

    public void notifyMessage (String message) throws RemoteException {
        System.out.println(message);
    
    }

}
