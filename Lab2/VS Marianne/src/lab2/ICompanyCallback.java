package lab2;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author ternekma
 */
public interface ICompanyCallback extends Remote{
   public int getCompanyCredits() throws RemoteException;
   public boolean incCompanyCredits  (int incCredits) throws RemoteException;
   public int prepareTask(String filename, int load,byte[] filecontent) throws RemoteException;
   public String executeTask (int id, String script,INotifyClient notifyRemObj) throws RemoteException ;
   public String taskInfo (int id) throws RemoteException;
   public void logout() throws RemoteException;
   public String getTaskOutput(int id) throws RemoteException ;
}
