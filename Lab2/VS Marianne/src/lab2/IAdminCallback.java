package lab2;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author ternekma
 */
public interface IAdminCallback extends Remote{
   public void logout() throws RemoteException;
   public String getPricingCurve() throws RemoteException;

   public String setPriceStep(int taskCount, double percent) throws RemoteException;
}
