/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lab2;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ternekma
 */
public class CompanyCallback extends UnicastRemoteObject implements ICompanyCallback{
   private Company company;

   // Defaultkonstruktor muß RemoteException werfen
   public CompanyCallback(Company company) throws RemoteException
   {
       this.company=company;
   }

   public int getCompanyCredits  () throws RemoteException {
       return company.getCredits();
   }
   
   public boolean incCompanyCredits  (int incCredits) throws RemoteException {
       //if incCredits greater 0 than --> command !buy was executed, otherwise
       //first check if there are enough credits --> comman !prepare was executed
       if ((incCredits>0) ||((company.getCredits()+incCredits)>=0)) {
         company.incCredits(incCredits,false);
         return true;
       } else {
         return false;
       }
   }

  public void logout() throws RemoteException {
       company.online=false;
   }

   public int prepareTask(String filename, int load,byte[] filecontent) throws RemoteException {
       if (incCompanyCredits(-ManagementComponent.preparationCosts)) {
         Task task = new Task(TaskList.getNewTaskID(),load,filename,company,filecontent);
         task.costs=ManagementComponent.preparationCosts;
         TaskList.AddTask(task);
         return task.id;
       } else {
           throw new RemoteException("Not enough credits to prepare a task.");
       }
   }

   public synchronized String executeTask (int id, String script,INotifyClient notifyRemObj) throws RemoteException {
       Task task= TaskList.getTaskByID(id);
       if (task==null) {
           return "Error: Task "+id+" does not exist.";
       } else if (task.company!=company)  {
            throw new RemoteException("Error: Task "+id+" does not belong to your company.");
       } else if (task.Status!=UtilityClass.TaskStatus.statePrepared) {
          return "Error: Execution has already been started.";
       } else {
           task.script=script;
           
           ManagementComponent.RequestEngineExecuteTask(task, notifyRemObj,company);
           return "";
       }
   }

   public String getTaskOutput(int id) throws RemoteException {
       String output="";
       Task task= TaskList.getTaskByID(id);

       if (task==null) {
           output= "Error: Task "+id+" does not exist.";
       } else if (task.company!=company)  {
            throw new RemoteException("Error: Task "+id+" does not belong to your company.");
       } else if (task.Status!=UtilityClass.TaskStatus.stateFinished) {
          output= "Error: Task "+task.id+" has not been finished yet.";
       } else {
          if (company.getCredits()>=0) {
            output=task.output;
          } else {
            output= "Error: You do not have enough credits to pay this execution. (Costs: "+Math.abs(company.getCredits())+ " credits) Buy new credits for retrieving the output.";
          }
       }

       return output;
   }

   public String taskInfo (int id) throws RemoteException {
       String feedback;
       Task task= TaskList.getTaskByID(id);

       if (task==null) {
           throw new RemoteException ("Error: Task "+id+" does not exist.");
       } else if (task.company!=company)  {
            throw new RemoteException("Error: Task "+id+" does not belong to your company.");
       } else  {
         feedback="Task "+id +" ("+task.filename+")\n";
         String sLoad="";
         if (task.expectedload==1) {
           sLoad="LOW";
         } else if  (task.expectedload==2) {
             sLoad="MIDDLE";
         } else if (task.expectedload==3) {
            sLoad="HIGH";
         }
         feedback=feedback+"Type: "+sLoad+"\n";
         String sEngine=task.engine_host;
         if (sEngine.isEmpty()) { sEngine="none"; }
         feedback=feedback+"Assigned engine: "+sEngine+"\n";
         String sStatus="";
         switch (task.Status) {
             case statePrepared: sStatus="prepared"; break;
             case stateAssigned: sStatus="assigned"; break;
             case stateExecuting: sStatus="executing"; break;
             case stateFinished: sStatus="finished"; break;
         }
         feedback=feedback+"Status: "+sStatus+"\n";
         if (task.costs>0) {
           feedback=feedback+"Costs: "+task.costs;
         } else {
           feedback=feedback+"Costs: unknown";
         }
       }
       return feedback;
   }
}

