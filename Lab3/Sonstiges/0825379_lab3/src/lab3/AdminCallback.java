package lab3;

/**
 *
 * @author Ternek Marianne 0825379
 * Jänner 2012
 */

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class AdminCallback extends UnicastRemoteObject implements IAdminCallback{
   private Company company;

   // Defaultkonstruktor muß RemoteException werfen
   public AdminCallback(Company company) throws RemoteException
   {
       this.company=company;
   }
   
   public void logout() throws RemoteException{
       company.online=false;
   }

   public String getPricingCurve() throws RemoteException{
     return PriceStepList.getAllPriceSteps();
   }

   public String setPriceStep(int taskCount, double percent) throws RemoteException {
       if (taskCount<0) {
           throw new RemoteException("Error: Invalid task count!");
       } else if ((percent<0) || (percent>100)) {
           throw new RemoteException("Error: Invalid percentage!");
       } else {
           PriceStepList.UpdateOrAddPriceStep(taskCount, percent);
           return "Successfully inserted price step.";
       }
   }

}
