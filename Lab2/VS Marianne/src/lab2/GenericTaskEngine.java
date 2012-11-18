package lab2;

/**
 *
 * @author Ternek Marianne 0825379
 * Oktober 2011
 */

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenericTaskEngine {
    private static int tcpPort =12722;
    private static String schedulerHost ="localhost";
    private static int schedulerUDPPort = 12721;
    private static int alivePeriod = 1000;
    private static int minConsumption =90;
    private static int maxConsumption =240;
    private static String taskDir ="your/engine1Dir/";
    private static ServerSocket TCPSocket;
    private static DatagramSocket UDPSocket;
    private static int taskCnt=0;

    private static ArrayList<Socket> clientSocketList = new ArrayList<Socket>();

    //need to be thread-safe:
    private static int currLoad=0;
    private static boolean suspended=false;
    private static Timer timer = new Timer();

    private static boolean pleaseStopAllThreads = false;

    /*****START NEED TO BE THREAD SAFE */
    //declare a method synchronized, then other synchronized methods cannot be simultaneously called on the same object
    private synchronized static void setSuspend(boolean bsuspended) {
         if ((bsuspended) && (!suspended)) {
            suspended=true;
            timer.cancel();
            timer.purge(); //removes all canceled tasks
         } else  if ((!bsuspended) && (suspended)) {
            suspended=false;
            timer = new Timer();
            timer.schedule  ( new TaskEngineTimer(), 0,alivePeriod );
         }
    }
    private synchronized static boolean getSuspend() {
         return suspended;
    }
    private synchronized static int getCurrLoad() {
         return currLoad;
    }
    private synchronized static void AddToCurrLoad(int add) {
         currLoad=currLoad+add;
    }

    private synchronized static int IncrementTaskCnt() {
      taskCnt++;
      return taskCnt;
    }
    /*****END NEED TO BE THREAD SAFE */

    //Timer to send alive messages to scheduler
    private static class TaskEngineTimer extends TimerTask
    {
      public void run()
      {
         if (!getSuspend())  {
             try {
                DatagramSocket socket = new DatagramSocket();
                byte[] sendData = new byte[1024];
                String sentence;
                sentence = ""+tcpPort+" "+minConsumption+" "+maxConsumption;

                sendData = sentence.getBytes();
                DatagramPacket pkt = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(schedulerHost), schedulerUDPPort);
                socket.send(pkt);
            } catch (IOException ex) {
                Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            }
         }  
      }
    }

    //ServiceSocket to get all the Scheduler and Client Connections
    private static class TCPNetworkService implements Runnable {
           private static ExecutorService pool;

           public TCPNetworkService(int port, int poolSize)
               throws IOException {
             pool = Executors.newFixedThreadPool(poolSize);
           }

           public void run() { // run the service
             try {
               for (;;) {
                 pool.execute(new ClientOrSchedulerConnection(TCPSocket.accept()));
               }
             } catch (IOException ex) {
                pool.shutdown();
             }
           }
    }

    private static class ClientOrSchedulerConnection implements Runnable {
       private final Socket clientSocket;
       ClientOrSchedulerConnection(Socket clientSocket) { this.clientSocket = clientSocket; }
       public void run() {
            boolean bAddedExcpectedLoad= false;
            int expectedload=0;
            
            //read line from client
            BufferedReader inFromClient = null;
            try {
                inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream outToclient = new DataOutputStream(clientSocket.getOutputStream());

                String userInput="";
                String feedback="";
                StringTokenizer tokens;
  
                userInput =inFromClient.readLine();
                tokens = new java.util.StringTokenizer(userInput);
                userInput = tokens.nextToken();

                //from client
                if  ( userInput.equalsIgnoreCase("!executeTask") ) {
                    //"!executeTask "+ task.filename+ " "+ task.expectedload + " \"" + task.script+ "\" " ;
                    int taskID= IncrementTaskCnt();
                    String filename = tokens.nextToken();
                    String type = tokens.nextToken();
                    
                    if (type.equalsIgnoreCase("1")) { expectedload=33; }
                    else if (type.equalsIgnoreCase("2")) { expectedload=66; }
                    else if (type.equalsIgnoreCase("3")) { expectedload=100; }

                    //switch delimter to " --> for script-text
                    tokens.nextToken("\"");
                    String script=  tokens.nextToken();

                    //update the current load
                    AddToCurrLoad(expectedload);
                    bAddedExcpectedLoad=true;

                    int filesize=6022386;
                    byte [] mybytearray  = new byte [filesize];
                    InputStream is = clientSocket.getInputStream();

                    //create file and directory if necessary
                    File dir = new File(taskDir+"/"+taskID);
                    dir.mkdirs();
                    File myFile = new File(taskDir+"/"+taskID+"/"+filename); //sdjfkdj

                    try {
                        myFile.createNewFile();
                    } catch (IOException ex) {
                        Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    FileOutputStream fos = new FileOutputStream(myFile);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);

                    // tell client that i am ready for the file
                    outToclient.writeBytes("hey client, i am ready!"+'\n');
                    int bytesRead = is.read(mybytearray,0,mybytearray.length);         
                    int current = bytesRead;

                    bos.write(mybytearray, 0 , current);
                    bos.flush();
                    bos.close();

                    Runtime rt = Runtime.getRuntime();
                    final Process proc = rt.exec(script, null, dir); 
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        @Override
                        public void run() {
                            proc.destroy();
                        }
                    });

                    BufferedReader inFromProcess = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    String line;
                    
                    //falls er hier nicht mehr raus kommt
                    clientSocketList.add(clientSocket);
                    while (((line = inFromProcess.readLine()) != null) && (!pleaseStopAllThreads))
                      outToclient.writeBytes(line+'\n');
                    outToclient.writeBytes("END_OF_OUTPUT"+'\n');
      
                //command comes from scheduler:
                } else if ( userInput.equalsIgnoreCase("!load") ) {
                    feedback=""+getCurrLoad();
                    //Socket is than already close in scheduler
                }
                outToclient.writeBytes(feedback+'\n');    
            } catch (SocketException e) {
                 //if Socket is closed do nothing
            } catch (IOException ex) {
                Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                //if there are troubles during task execution, do this in final block:
                if (bAddedExcpectedLoad) {
                  AddToCurrLoad(-expectedload);
                }         
                try {
                    inFromClient.close();                    
                    //close the connection after each completed request
                    clientSocket.close();
                } catch (IOException ex) {
                    Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
       }
     }

    //DatagramSocket to check Suspend or Activate Messages
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
                 pool.execute(new SuspendOrActivateMessage(packet));
               }
             } catch (IOException ex) {
               pool.shutdown();
             }
           }
    }

    private static class SuspendOrActivateMessage implements Runnable {
       private final DatagramPacket packet;
       SuspendOrActivateMessage(DatagramPacket packet) { this.packet = packet; }
       public void run() {
         // Suspend Or Activate Message verarbeiten
         String dataStr = new String(packet.getData());
         dataStr=dataStr.trim();
         if (dataStr.equalsIgnoreCase("SUSPEND")){
            setSuspend (true);
         } else  if (dataStr.equalsIgnoreCase("ACTIVATE")) {
            setSuspend (false); 
         }

       }
     }

     public static void main(String[] args) {
        String varialbeName="";
        for (int i = 0; i < args.length; i++) {
            try {
                switch (i) {
                    case 0: varialbeName="TCP-Port";
                            tcpPort=Integer.parseInt(args[i]);
                            break;
                    case 2: varialbeName="SchedulerUDPPort";
                            schedulerUDPPort=Integer.parseInt(args[i]);
                            break;
                    case 3: varialbeName="AlivePeriod";
                            alivePeriod=Integer.parseInt(args[i]);
                            break;
                    case 4: varialbeName="minConsumption";
                            minConsumption=Integer.parseInt(args[i]);
                            break;
                     case 5: varialbeName="maxConsumption";
                            maxConsumption=Integer.parseInt(args[i]);
                            break;
                }
           } catch (Exception e) {
                System.out.println(varialbeName+" ist kein Integerwert!");
                System.exit(1); //Programm beenden
           }
           try {
                switch (i) {
                   case 1: varialbeName="SchedulerHost";
                            schedulerHost=args[i];
                            if (schedulerHost.isEmpty())  throw new Exception();
                            break;
                   case 6: varialbeName="TaskDir";
                            taskDir=args[i];
                            if (taskDir.isEmpty())  throw new Exception();
                            break;
                }
           } catch (Exception e) {
                System.out.println("Wert für "+varialbeName+" fehlt!");
                System.exit(1); //Programm beenden
           }
        }
        try {
          TCPSocket = new ServerSocket(tcpPort);
          UDPSocket = new DatagramSocket(tcpPort);

          //timer for alive messages
          timer.schedule  ( new TaskEngineTimer(),0, alivePeriod );

          Thread tcpthread = new Thread(new TCPNetworkService(tcpPort,100));
          tcpthread.start();

          Thread udpthread = new Thread(new UDPNetworkService(100));
          udpthread.start();

          BufferedReader inFromUser = new BufferedReader(new InputStreamReader (System.in));
          boolean transacting = true;
          String sentence, feedback;
          while( transacting ) {
               sentence = inFromUser.readLine();
               sentence = sentence.trim();
               feedback="";
               if  (sentence.equalsIgnoreCase("!load") ){
                   feedback="Current load: "+getCurrLoad()+"%";
               } else if  (sentence.equalsIgnoreCase("!exit") ){
                   //if tasks running --> means if sockets aren't closed
                   for (Socket socket : clientSocketList) {
                       if (!socket.isClosed()) {
                         socket.close();
                       }
                    }

                    //Stop Timer
                    timer.cancel();

                    pleaseStopAllThreads=true;

                    TCPSocket.close();
                    UDPSocket.close();
                   transacting=false;
               }  else {
                   feedback="Can't understand you!";
               }
               System.out.println(feedback);
          }

        } catch (IOException ex) {
            Logger.getLogger(GenericTaskEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
     }

}
