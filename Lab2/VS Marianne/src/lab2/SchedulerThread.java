/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lab2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ternekma
 */
public class SchedulerThread implements Runnable{

       private final Task task;
       private final BufferedReader inFromScheduler;
       private ArrayList<Socket> socketList = new ArrayList<Socket>();
       private INotifyClient notifyRemObj;
       private Company company;

       public SchedulerThread(Task task , Socket socket,ArrayList<Socket> socketList,INotifyClient notifyRemObj,Company company)
           throws IOException {
           this.task=task;
           this.inFromScheduler = new BufferedReader(new InputStreamReader (socket.getInputStream()));
           this.socketList=socketList;
           this.notifyRemObj=notifyRemObj;
           this.company=company;
       }

       private void SendToEngine() {
           try {
                Socket taskEngineSocket = new Socket (task.engine_host,task.engine_port );
                socketList.add(taskEngineSocket);
                DataOutputStream outToTaskEngine = new DataOutputStream(taskEngineSocket.getOutputStream());
                BufferedReader inFromTaskEngine = new BufferedReader(new InputStreamReader (taskEngineSocket.getInputStream()));

                String sentence ="!executeTask "+ task.filename+ " "+ task.expectedload + " \"" + task.script+ "\" " ;
                outToTaskEngine.writeBytes(sentence+'\n');

                // sendfile
                byte [] mybytearray  = task.filecontent;
                if ((company!=null) &&(company.online)) {
                  notifyRemObj.notifyMessage("Task "+task.id+": send file to task engine");
                }
                //wait on TaskEngine, if it's really ready
                if (inFromTaskEngine.readLine()!=null) {
                  
                   //now load is added to taskengine
                  Thread executeTaskThread = new Thread(new ExecuteTaskThread(task, taskEngineSocket, mybytearray, notifyRemObj,company));
                  executeTaskThread.start();
                }

                //taskEngineSocket.close();
            } catch (SocketException e) {
               //if Socket is closed do nothing
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }

       }

       public void run()  {
        try {
            String feedback = inFromScheduler.readLine();
            if (feedback != null) {
                 if (task!=null) {
                     StringTokenizer tokens = new java.util.StringTokenizer(feedback);
                     if (tokens.countTokens()==5) {
                       //"assigned engine:"
                       tokens.nextToken(); tokens.nextToken();
                       //z.B. 192.0.0.1
                       String sengine_host= tokens.nextToken();
                        //"Port:
                       tokens.nextToken();
                       //z.B. 17271
                       int engine_port= Integer.parseInt(tokens.nextToken());

                       UtilityClass.TaskStatus Status=UtilityClass.TaskStatus.stateAssigned;

                       TaskList.updateTask(task,sengine_host, engine_port,Status);

                       //execute
                       task.Status = UtilityClass.TaskStatus.stateExecuting;
                       SendToEngine();
                     } else { //Not enough capacity. Try again later.
                        //do nothing; messages accours in other method
                     }
                 }
            } else {
                feedback = "Scheduler has closed the connection!";
            }
            if ((company!=null) &&(company.online)) {
              notifyRemObj.notifyMessage(feedback);
            }
        } catch (SocketException ex) {
            if ((company!=null) &&(company.online)) {
              try { notifyRemObj.notifyMessage("Scheduler is down!"); } catch (RemoteException ex1) {}
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
       }
}
