/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lab2;

import java.rmi.RemoteException;

/**
 *
 * @author ternekma
 */
public class NotifyClient implements INotifyClient {
    public NotifyClient() throws RemoteException
    {

    }

    public void notifyMessage (String message) throws RemoteException {
        System.out.println(message);
    
    }


}
