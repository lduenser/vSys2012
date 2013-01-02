package lab3;

/**
 *
 * @author Ternek Marianne 0825379
 * Jänner 2012
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ternekma
 */
public class ExecuteTaskThread implements Runnable {
       private final BufferedReader inFromTaskEngine;
       private final DataOutputStream outToTaskEngine;
       private final Task task;
       
       private INotifyClient notifyRemObj;
       private  byte [] mybytearray;
       private Company company;
       private int enginenumber;
       private int amount;

       public ExecuteTaskThread(Task task, Socket socket, byte [] mybytearray,INotifyClient notifyRemObj,Company company, int enginenumber, int amount)
           throws IOException {
           this.mybytearray=mybytearray;
           this.task=task;
           this.inFromTaskEngine = new BufferedReader(new InputStreamReader (socket.getInputStream()));
           this.outToTaskEngine= new DataOutputStream(socket.getOutputStream());
           this.company=company;
           this.enginenumber=enginenumber;
           this.amount=amount;

           this.notifyRemObj=notifyRemObj;
       }

        public void run()  {
          int costs=0;
          boolean bAddExeCnt=false;
          GregorianCalendar startDate =new GregorianCalendar();
          try {
            try {
                if (company.online) {
                  notifyRemObj.notifyMessage("Execution of task " + task.id + " started!" );
                }
                company.incStartExecutions(); bAddExeCnt=true;
                outToTaskEngine.write(mybytearray, 0, mybytearray.length);
                outToTaskEngine.flush();
                String line;
                boolean end = false;
                //falls er hier nicht mehr raus kommt
                while ((!end) && ((line = inFromTaskEngine.readLine()) != null)) {
                    line = line.trim();
                    if (line.equalsIgnoreCase("END_OF_OUTPUT")) {        
                        end = true;
                    } else {
                        task.AddOutput(enginenumber,line);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ExecuteTaskThread.class.getName()).log(Level.SEVERE, null, ex);
            } 
          } finally {
              boolean bfinished=false;
              if (task!=null) {
                task.AddFinishedTaskCount();
                bfinished=(task.GetFinishedTaskCount()==amount);
              }
              GregorianCalendar endDate = new GregorianCalendar();
              // Get the represented date in milliseconds
              long time1 = startDate.getTimeInMillis();
              long time2 = endDate.getTimeInMillis();
              // Calculate difference in milliseconds
              long diff = time2 - time1;
              // Difference in minutes
              double diffMin = ((double)diff) / (60 * 1000);
              costs=(int) Math.ceil(diffMin);
              costs=costs * 10;
              if ((company.online) && (bfinished)) {
                  try {
                     notifyRemObj.notifyMessage("Execution of task " + task.id + " finished!" );
                  } catch (RemoteException ex) {
                     System.out.println("Client, who has executed a task on taskengine(s) "+task.getEngineHosts()+" has gone during execution!");
                  }
              }
              if (task!=null) {
                if (bfinished) {
                   task.Status=UtilityClass.TaskStatus.stateFinished;
                }
                task.costs=task.costs+costs;
              }
              if (company!=null) {
                //if there went something wrong and the started executions are not beein incremented already
                if (!bAddExeCnt) {  company.incStartExecutions(); }
                //increment finished executions
                company.incFinishedExecutions(task.expectedload);
                //decrement costs
                company.incCredits(-costs,true);
              }

          }
        }

}
