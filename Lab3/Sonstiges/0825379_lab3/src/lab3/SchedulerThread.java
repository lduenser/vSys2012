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
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.security.Key;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.util.encoders.Base64;

/**
 *
 * @author ternekma
 */
public class SchedulerThread implements Runnable{

       private final Task task;
      // private final BufferedReader inFromScheduler;
       private ArrayList<Socket> socketList = new ArrayList<Socket>();
       private INotifyClient notifyRemObj;
       private Company company;
       private int amount;
       private CryptChannel cryptChannel;
       private String sManagerChallangeBase64;

       public SchedulerThread(Task task , Socket socket,ArrayList<Socket> socketList,INotifyClient notifyRemObj,Company company, int amount,Key key, String sManagerChallangeBase64)
           throws IOException {
           this.task=task;
           this.cryptChannel= new CryptChannel(new Base64Channel(new TCPChannel(socket)));
           cryptChannel.setKey(key);
           cryptChannel.setalgorithm("RSA/NONE/OAEPWithSHA256AndMGF1Padding");

          // this.inFromScheduler = new BufferedReader(new InputStreamReader (socket.getInputStream()));
           this.socketList=socketList;
           this.notifyRemObj=notifyRemObj;
           this.company=company;
           this.amount=amount;
           this.sManagerChallangeBase64=sManagerChallangeBase64;
       }

       private void SendToEngine(String engine_host, int engine_port, int enginenumber) {
           try {
                Socket taskEngineSocket = new Socket (engine_host,engine_port );
                socketList.add(taskEngineSocket);
                DataOutputStream outToTaskEngine = new DataOutputStream(taskEngineSocket.getOutputStream());
                BufferedReader inFromTaskEngine = new BufferedReader(new InputStreamReader (taskEngineSocket.getInputStream()));
                String sentence ="";
                String script= task.script;
                if ((amount>1) && (enginenumber>0)) {
                   script= script.replace("$a", ""+amount);
                   script= script.replace("$p", ""+enginenumber);
                }

                sentence ="!executeTask "+ task.filename+ " "+ task.expectedload + " \"" + script+ "\" " ;
                
                outToTaskEngine.writeBytes(sentence+'\n');

                // sendfile
                byte [] mybytearray  = task.filecontent;
                if ((company!=null) &&(company.online)) {
                  notifyRemObj.notifyMessage("Task "+task.id+": send file to task engine: "+engine_host);
                }
                //wait on TaskEngine, if it's really ready
                if (inFromTaskEngine.readLine()!=null) {
                  
                   //now load is added to taskengine
                  Thread executeTaskThread = new Thread(new ExecuteTaskThread(task, taskEngineSocket, mybytearray, notifyRemObj,company,enginenumber,amount));
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
        //authentication
        boolean bread=true;
        String feedback="";
        while (bread) {
          byte[] temp= cryptChannel.receive();
          if (temp!=null) {
              try {
                     feedback = new String(temp, "UTF8");
              } catch (UnsupportedEncodingException ex) {
                     Logger.getLogger(ManagementComponent.class.getName()).log(Level.SEVERE, null, ex);
              }
              bread=false;
          }
        }
        if (feedback.startsWith("Error:")) {
            if ((company!=null) &&(company.online)) {
                try {
                    notifyRemObj.notifyMessage(feedback);
                } catch (RemoteException ex) {
                    Logger.getLogger(SchedulerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            StringTokenizer tokens = new java.util.StringTokenizer(feedback);
            //!ok
            tokens.nextToken();
            //manager-challange
            String sManagerChallangeBase64FromScheduler=tokens.nextToken();
            //scheduler-challange
            String schedulerCallangeBase64= tokens.nextToken();
            if (!sManagerChallangeBase64.equals(sManagerChallangeBase64FromScheduler)) {
                feedback = "Manager-Challange from scheduler is not the same!";
            } else {
                //secretKey
                byte[] secretKey = Base64.decode(tokens.nextToken().getBytes());

                Key skey = new javax.crypto.spec.SecretKeySpec(secretKey, "AES");
                //Init Vector
                byte[] iv=  Base64.decode(tokens.nextToken().getBytes());

                cryptChannel.setalgorithm("AES/CTR/NoPadding");
                cryptChannel.setInitVector(iv);
                cryptChannel.setKey(skey);
                try {
                    String thirdMessage= schedulerCallangeBase64;
                    assert thirdMessage.matches("["+UtilityClass.B64+"]{43}=") : "3rd message is not well-formed";
                    cryptChannel.send(thirdMessage.getBytes());

                    cryptChannel.send(("!requestEngine "+ task.expectedload +" "+amount).getBytes()); //TODO + \n?
                    try {
                        int engineNumber=0;
                        boolean berror=false;
                        while ((engineNumber<amount) && (!berror)) {
                            berror=true;
                            byte [] temp=cryptChannel.receive();
                            if (temp!=null) {
                                try {
                                   feedback = new String(temp, "UTF8");
                                } catch (UnsupportedEncodingException ex) {
                                   Logger.getLogger(SchedulerThread.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            } else {feedback=null;}
                            if (feedback != null) {
                                 if (task!=null) {
                                     tokens = new java.util.StringTokenizer(feedback);
                                     if (tokens.countTokens()==5) {
                                       engineNumber++;
                                       //"assigned engine:"
                                       tokens.nextToken(); tokens.nextToken();
                                       //z.B. 192.0.0.1
                                       String sengine_host= tokens.nextToken();
                                        //"Port:
                                       tokens.nextToken();
                                       //z.B. 17271
                                       int engine_port= Integer.parseInt(tokens.nextToken());

                                       UtilityClass.TaskStatus Status=UtilityClass.TaskStatus.stateAssigned;

                                       TaskList.updateTask(task,Status);
                                       TaskList.AddRequestedEngine(task, engine_port, sengine_host);

                                       //execute
                                       task.Status = UtilityClass.TaskStatus.stateExecuting;
                                       SendToEngine(sengine_host,engine_port,engineNumber);
                                       berror=false;
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
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (AssertionError e) {
                    System.out.println(e.getMessage());
                    if (cryptChannel!=null) {
                      cryptChannel.send(("Error: "+e.getMessage()).getBytes());
                    }
                    try {
                        notifyRemObj.notifyMessage("Error: "+e.getMessage());
                    } catch (RemoteException ex) {
                    }
                }
            }
          }
       }
}
