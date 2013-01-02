package lab3;

/**
 *
 * @author Ternek Marianne 0825379
 * Jänner 2012
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ICompanyCallback extends Remote{
   public int getCompanyCredits() throws RemoteException;
   public boolean incCompanyCredits  (int incCredits) throws RemoteException;
   public int prepareTask(String filename, int load,byte[] filecontent) throws RemoteException;
   public String executeTask (int id, String script,INotifyClient notifyRemObj) throws RemoteException ;
   public String executeDistributedTask (int id, int amount, String script,INotifyClient notifyRemObj) throws RemoteException ;
   public String taskInfo (int id) throws RemoteException;
   public void logout() throws RemoteException;
   public String getTaskOutput(int id) throws RemoteException ;
}
