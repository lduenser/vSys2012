package lab2;

/**
 *
 * @author Ternek Marianne 0825379
 * November 2011
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Remote;

public class ManagementComponent {

    private static String bindingName = "yourBindingName";
    public static String schedulerHost = "localhost";
    public static int schedulerTCPPort = 12720;
    public static int preparationCosts = 30;
    public static String taskDir= "your/managementTaskDir";

    private static ArrayList<Socket> socketList = new ArrayList<Socket>();
    public static ArrayList<Remote> exportedRemoteObjects = new ArrayList<Remote>();

    public static synchronized void RequestEngineExecuteTask(Task task,INotifyClient notifyRemObj, Company company ) {
        try {
            DataOutputStream outToServer=null;
            try {
                Socket clientSocket = new Socket(ManagementComponent.schedulerHost, ManagementComponent.schedulerTCPPort);
                socketList.add(clientSocket);

                outToServer = new DataOutputStream(clientSocket.getOutputStream());
                Thread serverthread = new Thread(new SchedulerThread(task,clientSocket,socketList,notifyRemObj,company));
                serverthread.start();
                outToServer.writeBytes("!requestEngine "+ task.expectedload +'\n');
                serverthread.join();
            } finally {
               outToServer.close();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(CompanyCallback.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(CompanyCallback.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CompanyCallback.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            //print out on ManagerComponent
            System.out.println("Scheduler is down!");
            //print out on Client
            if ((company!=null) &&(company.online)) {
                try {
                    notifyRemObj.notifyMessage("Scheduler is down!");
                } catch (RemoteException ex) {
                     //if client gone, you don't have to inform him about scheduler
                }
           }
        }

    }
    
    public static void main(String[] args)  {
        //Check ob Werte stimmen
        String varialbeName="";
        boolean bok=true;
        
        for (int i = 0; i < args.length; i++) {
            try {
                switch (i) {
                    case 0: varialbeName="bindingName";
                            bindingName=args[i];
                            break;
                    case 1: varialbeName="schedulerHost";
                            schedulerHost=args[i];
                            break;
                    case 2: varialbeName="schedulerTCPPort";
                            schedulerTCPPort=Integer.parseInt(args[i]);
                            break;
                    case 3: varialbeName="preparationCosts";
                            preparationCosts=Integer.parseInt(args[i]);
                            break;
                    case 4: varialbeName="TaskDir";
                            taskDir=args[i];
                            break;
                }
           } catch (Exception e) {
                System.out.println(varialbeName+" ist kein Integerwert!");
                bok=false;
           }
        }

        Properties regProp= UtilityClass.GetRegistryProps();

       if (regProp==null) {
            System.out.println("Reg.Properties file could not be found!");
       } else {
            try {
                String registryHost = regProp.getProperty("registry.host");
                Integer registryPort = Integer.parseInt(regProp.getProperty("registry.port"));
   
                RemoteLogin obj = new RemoteLogin();
                exportedRemoteObjects.add(obj);
                Registry reg= LocateRegistry.createRegistry(registryPort);
                reg.bind(bindingName, obj);

                try {
                  BufferedReader inFromUser = new BufferedReader(new InputStreamReader (System.in));
                  boolean transacting = true;
                  String sentence, feedback;
                  String sStatus;
                  while( transacting ) {
                        sentence = inFromUser.readLine();
                        sentence = sentence.trim();
                        feedback="";
                        if  (sentence.equalsIgnoreCase("!users") ) {
                            int cnt=0;
                            for (Company company : RemoteLogin.companyList) {
                                cnt++;
                                if (company.online) {
                                  sStatus="online";
                                } else { sStatus="offline"; }
                                feedback= feedback+ String.format("%d. %s (%s): LOW %d, MIDDLE %d, HIGH %d" +'\n', cnt, company.username, sStatus,company.low,company.middle,company.high);
                            }
                        } else if  (sentence.equalsIgnoreCase("!exit") ){
                            for (Socket socket : socketList) {
                               try {
                                  if (!socket.isClosed()) {
                                   socket.close();
                                  }
                               } catch (Exception e) {}
                            }
                            for (Remote remObject : exportedRemoteObjects) {
                               try {
                                  UnicastRemoteObject.unexportObject(remObject, true);
                               } catch (Exception e) {}
                            }


                            transacting=false;
                        } else {
                            feedback="can't understand you!";
                        }

                        System.out.println(feedback);
                  }

                } catch (IOException ex) {
                    Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
                }
                

            } catch (AlreadyBoundException ex) {
                Logger.getLogger(ManagementComponent.class.getName()).log(Level.SEVERE, null, ex);
            } catch (AccessException ex) {
                Logger.getLogger(ManagementComponent.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                Logger.getLogger(ManagementComponent.class.getName()).log(Level.SEVERE, null, ex);
            }

           


        }
       
        
    }

}
