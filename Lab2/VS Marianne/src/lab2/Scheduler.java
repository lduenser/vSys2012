package lab2;

/**
 *
 * @author Ternek Marianne 0825379
 * Oktober 2011
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.GregorianCalendar;

public class Scheduler {

    private static int tcpPort = 12720;
    private static int udpPort = 12721;
    private static int min = 2;
    private static int max = 4;
    private static int timeout = 3000;
    private static int checkPeriod = 1000;

    private static ServerSocket TCPSocket;
    private static DatagramSocket UDPSocket;

    //clientSocketList contains all sockets that needs to be closed
    private static ArrayList<Socket> clientSocketList = new ArrayList<Socket>();

    public static enum engineStatus {
        stateOnline, stateOffline, stateSuspended
    }

    //Object for TastEngineList
    private static class TaskEngine {
         int port;
         String hostAdress;
         engineStatus state;

         int minConsumption;
         int maxConsumption;
         int currentLoad;
         GregorianCalendar lastIsAlive =new GregorianCalendar();

         TaskEngine(int port,String hostAdress, int min, int max) {
          this.port=port;
          this.hostAdress=hostAdress;
          this.maxConsumption=max;
          this.minConsumption=min;
          this.currentLoad=0;
          this.state=engineStatus.stateOnline;
         }

         private synchronized void updateCurrLoad(int aLoad) {
             this.currentLoad=this.currentLoad+aLoad;
         }

         private synchronized  void updateState(engineStatus aState) {
             this.state=aState;
         }

         private synchronized  void suspendOrActivateTask(boolean suspend) {
            try {
                DatagramSocket socket = new DatagramSocket();
                byte[] sendData = new byte[1024];
                String sentence;
                if (suspend) {
                  sentence = "SUSPEND";
                   this.state=engineStatus.stateSuspended;
                } else {
                  sentence="ACTIVATE";
                  this.state=engineStatus.stateOffline; //description say so
                }
                sendData = sentence.getBytes();
                DatagramPacket pkt = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(this.hostAdress), this.port);
                socket.send(pkt);
            } catch (IOException ex) {
                Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
    }

    //TastEngineList contains all taskengines
    private static ArrayList<TaskEngine> taskEngineList = new ArrayList<TaskEngine>();
    
    /*****START NEED TO BE THREAD SAFE */
    //Number of active task engines
    private synchronized  static int getActiveTaskEngines() {
       int cnt=0;
       for (TaskEngine engine : taskEngineList) {
          if (engine.state==engineStatus.stateOnline) {
            cnt++;
          }
       }
       return cnt;
    }
    //get task with fewest energy which is suspended
    private synchronized  static TaskEngine GetFewestEnergyNotActiveTaskEngine() {
        TaskEngine bestEngine = null;
        double fewestEnergy=0;

        for (TaskEngine engine : taskEngineList) {
          if (engine.state==engineStatus.stateSuspended) {
           if (bestEngine==null) {
             fewestEnergy=engine.minConsumption;
             bestEngine=engine;
           } else if (engine.minConsumption<fewestEnergy) {
             fewestEnergy=engine.minConsumption;
             bestEngine=engine;
           }
          }
        }
        return bestEngine;
    }

    private synchronized  static boolean ActiveTaskEngineLessThan(int aLoad) {
        for (TaskEngine engine : taskEngineList) {
               if ((engine.state==engineStatus.stateOnline) && (engine.currentLoad<aLoad)) {
                   return true;
               }
            }
        return false;
    }
    private synchronized  static int getNoLoadEnginesCnt(int stopAt) {
        int Nullcnt =0;
        for (TaskEngine engine : taskEngineList) {
            if ((engine.currentLoad==0) && (engine.state==engineStatus.stateOnline)) {
                   Nullcnt++;
                   if ((stopAt>0) && (stopAt==Nullcnt)) {
                       break;
                   }
            }
        }
        return Nullcnt;
    }
    private synchronized  static void AddTaskEngine(int port,String hostAdress, int min, int max) {
        boolean bfound= false;
        for (TaskEngine engine : taskEngineList) {
            if ((engine.port==port) && (engine.hostAdress.equals(hostAdress))) {
               if (engine.state!=engineStatus.stateSuspended) {
                 engine.lastIsAlive= new GregorianCalendar();
                 engine.state=engineStatus.stateOnline;
               }
               bfound=true;
               break;
           }
        }

        if (!bfound) {
            TaskEngine engine = new TaskEngine(port,hostAdress,min,max);
            taskEngineList.add(engine);
        }
    }

    private synchronized  static void RequestLoad() {
        try {
            for (TaskEngine engine : taskEngineList) {
              if  (engine.state==engineStatus.stateOnline) {
                Socket engineSocket = new Socket(engine.hostAdress, engine.port);
                DataOutputStream outToEngine = new DataOutputStream(engineSocket.getOutputStream());
                BufferedReader inFromEngine = new BufferedReader(new InputStreamReader (engineSocket.getInputStream()));
                outToEngine.writeBytes("!load"+'\n');
                String sLoad = inFromEngine.readLine();
                int load =0;
                try {
                  load= Integer.parseInt(sLoad);
                } catch (NumberFormatException e) {
                  load= -1;
                }
                if (load>-1) {
                  engine.currentLoad=load;
                }
                engineSocket.close();
              }
            }
        } catch (UnknownHostException ex) {
                Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
                Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private synchronized  static TaskEngine GetBestTaskEngine(int expectedload) {
        TaskEngine bestEngine = null;
        double bestEnergy=0.0;
        double newEnergy=0.0;
        double diff;
        for (TaskEngine engine : taskEngineList) {
          if (engine.state==engineStatus.stateOnline) {
             int newLoad = engine.currentLoad+expectedload;
             if (newLoad<=100) {
               diff = engine.maxConsumption-engine.minConsumption;
               newEnergy = (diff/100)*newLoad;
               newEnergy = newEnergy+engine.minConsumption;
               if (bestEngine==null) {
                 bestEngine=engine;
                 bestEnergy=newEnergy;
               } else if (newEnergy<bestEnergy) {
                 bestEnergy=newEnergy;
                 bestEngine=engine;
               }
             }
          }
        }
        return bestEngine;
    }

    private synchronized  static TaskEngine GetMostEnergyActiveTaskEngine() {
        TaskEngine MostEnergyEngine = null;
        double mostEnergy=0.0;
        double newEnergy=0.0;
        double diff;
        for (TaskEngine engine : taskEngineList) {
          if (engine.state==engineStatus.stateOnline) {
           diff = engine.maxConsumption-engine.minConsumption;
           newEnergy = (diff/100)*engine.currentLoad;
           newEnergy = newEnergy+engine.minConsumption;
           if (MostEnergyEngine==null) {
             MostEnergyEngine=engine;
             mostEnergy=newEnergy;
           } else if (newEnergy>mostEnergy) {
             mostEnergy=newEnergy;
             MostEnergyEngine=engine;
           }
          }
        }
        return MostEnergyEngine;
    }

    private static synchronized  String getEnginesInfo() {
       int cnt=0;
       String sStatus;
       String feedback="";
       for (TaskEngine engine : taskEngineList) {
          cnt++;
          if (engine.state==engineStatus.stateSuspended) {
              sStatus="suspended";
          } else if (engine.state==engineStatus.stateOffline)  {
              sStatus="offline";
          } else  sStatus="online";
          feedback= feedback+ String.format("%d. IP: %s, TCP: %d, UDP: %d, %s, Energy Signatur: min %dW, max %dW, Load: %d%% "+ '\n', cnt, engine.hostAdress, engine.port, engine.port, sStatus, engine.minConsumption, engine.maxConsumption, engine.currentLoad);
        }

       return feedback;
    }
    /*****END NEED TO BE THREAD SAFE */

    //Timer to check if TaskEngines are alive and for dynamic cloud elasticity
    private static class TaskEngineTimer extends TimerTask
    {
      public void run()
      {
        //Check isAlive
        GregorianCalendar currDate = new GregorianCalendar();
        for (TaskEngine engine : taskEngineList) {
            if ((engine.state==engineStatus.stateOnline) && ((currDate.getTimeInMillis() - engine.lastIsAlive.getTimeInMillis())>timeout)) {
               engine.updateState(engineStatus.stateOffline);
           }
        }

        //start dynamic cloud elasticity:
        //ACTIVATE:
        //if active tasks greater or equal max  --> not acitvate a engine
        if (getActiveTaskEngines()<max) {
            boolean bfound=ActiveTaskEngineLessThan(66);
           
            if (!bfound) {
                TaskEngine bestEnergyEngine=GetFewestEnergyNotActiveTaskEngine();
                if (bestEnergyEngine!=null) { bestEnergyEngine.suspendOrActivateTask(false); }
            }
            if (getActiveTaskEngines()<min) {
               TaskEngine bestEnergyEngine=GetFewestEnergyNotActiveTaskEngine();
               if (bestEnergyEngine!=null) { bestEnergyEngine.suspendOrActivateTask(false); }
            }
        }

        //SUSPEND:
        //if active tasks is not greater than min  --> not suspend a engine
        if (getActiveTaskEngines()>min) {
            int Nullcnt=getNoLoadEnginesCnt(2);
            if (Nullcnt>=2) {
                TaskEngine mostEnergyEngine=GetMostEnergyActiveTaskEngine();
                if (mostEnergyEngine!=null) { 
                    mostEnergyEngine.suspendOrActivateTask(true);
                }
            }
        }
      }
    }

    //ServiceSocket to get all the Client Connections
    private static class TCPNetworkService implements Runnable {
           private static ExecutorService pool;

           public TCPNetworkService(int poolSize)
               throws IOException {
             pool = Executors.newFixedThreadPool(poolSize);
           }

           public void run() { 
             try {
               for (;;) {
                 pool.execute(new ClientConnection(TCPSocket.accept()));
               }
             } catch (SocketException e) {
                 //TCP Socket is closed
                 pool.shutdown();
             } catch (IOException ex) {
               pool.shutdown();
             }
           }
    }

    private static class ClientConnection implements Runnable {
       private final Socket clientSocket;
       ClientConnection(Socket clientSocket) { this.clientSocket = clientSocket; }
       public void run() {
            //read line from client
            BufferedReader inFromClient = null;
            try {
                inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream outToclient = new DataOutputStream(clientSocket.getOutputStream());

                String userInput="";
                String feedback="";
                StringTokenizer tokens;
                boolean transacting = true;
                while( transacting ) {
                    userInput =inFromClient.readLine();
                    //if userInput==null than Client has gone
                    if (userInput!=null) {
                        tokens = new java.util.StringTokenizer(userInput);
                        userInput = tokens.nextToken();
                        feedback="";
                        if  ( userInput.equalsIgnoreCase("!exit") ) {
                          transacting = false;
                        } else if  ( userInput.equalsIgnoreCase("!requestEngine") ) {
                          clientSocketList.add(clientSocket);
                          String type = tokens.nextToken();
                          int expectedload=0;
                          if (type.equals("1")) { expectedload=33;}
                          else if (type.equals("2")) { expectedload=66;}
                          else if (type.equals("3")) { expectedload=100;}
                          // request and save the current load of the available engines
                          RequestLoad();
                          TaskEngine bestengine = GetBestTaskEngine(expectedload);
                          if (bestengine==null) {
                             feedback="Error: No engine available for execution. Please try again later.";
                          } else {
                             //update the load infomration
                             bestengine.updateCurrLoad(expectedload);
                             feedback="Assigned engine: "+bestengine.hostAdress+" Port: "+bestengine.port;
                          }
                        } else {
                           feedback="can't understand you!"; 
                        }
                        if (!feedback.isEmpty()) {
                          outToclient.writeBytes(feedback+'\n');
                        }
                    } else {
                        transacting = false;
                        feedback="Client is not available!";
                        outToclient.writeBytes(feedback+'\n');
                    }
                }
            } catch (SocketException ex) {
                //"Client closed a connection!"; //raise no exception!
            } catch (IOException ex) {
                Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    inFromClient.close();
                    clientSocket.close();
                } catch (IOException ex) {
                    Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
       }
     }

     //DatagramSocket to check isAlive Messages
     private static class UDPNetworkService implements Runnable {
           private static ExecutorService pool;

           public UDPNetworkService(int poolSize)
               throws IOException {
             pool = Executors.newFixedThreadPool(poolSize);
           }

           public void run() { 
             try {
               for (;;) {
                 byte[] buf = new byte[1000];
                 DatagramPacket packet = new DatagramPacket(buf, buf.length);
                 UDPSocket.receive(packet);
                 pool.execute(new IsAliveMessage(packet));
               }
             } catch (SocketException e) {
                 //UDP Socket is closed
                 pool.shutdown();
             } catch (IOException ex) {
               pool.shutdown();
             }
           }
    }

    private static class IsAliveMessage implements Runnable {
       private final DatagramPacket packet;
       IsAliveMessage(DatagramPacket packet) { this.packet = packet; }
       public void run() {
         // isAlive Message verarbeiten
         int min=0, max=0, taskengineport=0;
         String dataStr = new String(packet.getData());
         StringTokenizer tokens = new java.util.StringTokenizer(dataStr);

         try { taskengineport= Integer.parseInt(tokens.nextToken()); } catch (NumberFormatException e) { taskengineport= 0; }
         try { min= Integer.parseInt(tokens.nextToken()); } catch (NumberFormatException e) { min= 0; }
         try { max= Integer.parseInt(tokens.nextToken()); } catch (NumberFormatException e) { max= 0; }
         
         AddTaskEngine(taskengineport,packet.getAddress().getHostAddress(), min, max);
       }
     }
     
    public static void main(String[] args) {

        //Check ob Werte stimmen
        String varialbeName="";       
        for (int i = 0; i < args.length; i++) {
            try {
                switch (i) {
                    case 0: varialbeName="TCP-Port";
                            tcpPort=Integer.parseInt(args[i]);
                            break;
                    case 1: varialbeName="UDP-Port";
                            udpPort=Integer.parseInt(args[i]);
                            break;
                    case 2: varialbeName="Min";
                            min=Integer.parseInt(args[i]);
                            break;
                    case 3: varialbeName="Max";
                            max=Integer.parseInt(args[i]);
                            break;
                    case 4: varialbeName="Timeout";
                            timeout=Integer.parseInt(args[i]);
                            break;
                     case 5: varialbeName="checkPeriod";
                            checkPeriod=Integer.parseInt(args[i]);
                            break;
                }
           } catch (Exception e) {
                System.out.println(varialbeName+" ist kein Integerwert!");
                System.exit(1); //Programm beenden
           }
        }

        //Sockets erzeugen
        try {
          TCPSocket = new ServerSocket(tcpPort);
          UDPSocket = new DatagramSocket(udpPort);

          Timer timer = new Timer();
          timer.schedule  ( new TaskEngineTimer(), 0,checkPeriod );

          Thread tcpthread = new Thread(new TCPNetworkService(100));
          tcpthread.start();
          Thread udpthread = new Thread(new UDPNetworkService(100));
          udpthread.start();

          BufferedReader inFromUser = new BufferedReader(new InputStreamReader (System.in));
          boolean transacting = true;
          String sentence, feedback;
          String sStatus;
          while( transacting ) {
                sentence = inFromUser.readLine();
                sentence = sentence.trim();
                feedback="";
                if  (sentence.equalsIgnoreCase("!engines") ) {
                   feedback=getEnginesInfo();            
                } else if  (sentence.equalsIgnoreCase("!exit") ){
                    //sockets von clients schliessen

                    for (Socket socket : clientSocketList) {
                       try {
                         if (!socket.isClosed()) {
                           socket.close();
                         }
                       } catch (Exception e) {}
                    }

                    //Stop Timer
                    timer.cancel();

                    TCPSocket.close();
                    UDPSocket.close();

                    transacting=false;
                } else {
                    feedback="can't understand you!";
                }

                System.out.println(feedback);
          }

        } catch (IOException ex) {
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}