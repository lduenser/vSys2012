package lab2;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author ternekma
 */
public interface IRemoteLogin extends Remote{

    Remote login(String companyname, String password) throws RemoteException ;
    
}
