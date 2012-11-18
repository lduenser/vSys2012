package lab2;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author ternekma
 */
public interface INotifyClient extends Remote{
  void notifyMessage (String message) throws RemoteException;
}

