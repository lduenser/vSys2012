package lab3;

/**
 *
 * @author Ternek Marianne 0825379
 * Jänner 2012
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAdminCallback extends Remote{
   public void logout() throws RemoteException;
   public String getPricingCurve() throws RemoteException;

   public String setPriceStep(int taskCount, double percent) throws RemoteException;
}
