package lab3;

/**
 *
 * @author Ternek Marianne 0825379
 * Jänner 2012
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface INotifyClient extends Remote{
  void notifyMessage (String message) throws RemoteException;
}

