package lab3;

/**
 *
 * @author Ternek Marianne 0825379
 * Jänner 2012
 */

import java.io.*;
import java.net.*;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.GregorianCalendar;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.util.encoders.Base64;

public class Scheduler {

    private static int tcpPort;
    private static int udpPort;// = 12721;
    private static int min;// = 2;
    private static int max;// = 4;
    private static int timeout;// = 3000;
    private static int checkPeriod;// = 1000;
    private static String enKeyFile,deKeyFile;

    private static ServerSocket TCPSocket;
    private static DatagramSocket UDPSocket;

    public static PublicKey publickey = null;
    public static PrivateKey privatekey = null;

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

    private synchronized  static TaskEngine GetBestTaskEngine(int expectedload,ArrayList<TaskEngine> requestedEngines) {
        TaskEngine bestEngine = null;
        double bestEnergy=0.0;
        double newEnergy=0.0;
        double diff;
        for (TaskEngine engine : taskEngineList) {
          if ((engine.state==engineStatus.stateOnline) && (!requestedEngines.contains(engine))) {
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
            clientSocketList.add(clientSocket);
            //read line from client
            CryptChannel cryptChannel = null;
            try {
                String userInput="";
                String feedback="";
                StringTokenizer tokens;
                boolean transacting = false;

                cryptChannel= new CryptChannel(new Base64Channel(new TCPChannel(clientSocket)));
                cryptChannel.setKey(privatekey);
                cryptChannel.setalgorithm("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
                byte [] decryptedText =  cryptChannel.receive();
                if (decryptedText!=null) {
                    try {
                            userInput = new String(decryptedText, "UTF8");
                    } catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(ManagementComponent.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {userInput=null;}
                if (userInput!=null) {
                  tokens = new java.util.StringTokenizer(userInput);
                  userInput = tokens.nextToken();
                  if  ( userInput.equalsIgnoreCase("!login") ) {
                      String managerChallange = tokens.nextToken();
                      String newChallange= UtilityClass.getRandomNumber(32);
                      SecretKey secretKey=null;
                      try {
                        KeyGenerator generator;
                        generator = KeyGenerator.getInstance("AES");
                        generator.init(256);// KEYSIZE is in bits
                        secretKey = generator.generateKey();
                      } catch (NoSuchAlgorithmException ex) {
                        Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
                      }

                      String ivParam= UtilityClass.getRandomNumber(16);

                      String secondMessage="!ok "+managerChallange+" "+newChallange+" "+new String(Base64.encode(secretKey.getEncoded()),"UTF8")+" "+ivParam;
                      assert secondMessage.matches("!ok ["+UtilityClass.B64+"]{43}= ["+UtilityClass.B64+"]{43}= ["+UtilityClass.B64+"]{43}= ["+UtilityClass.B64+"]{22}==") : "2nd message is not well-formed";
                      cryptChannel.send(secondMessage.getBytes());

                      byte[] iv=  Base64.decode(ivParam.getBytes());
                      cryptChannel.setInitVector(iv);
                      cryptChannel.setKey(secretKey);
                      cryptChannel.setalgorithm("AES/CTR/NoPadding");
                      decryptedText =  cryptChannel.receive();
                      if (decryptedText!=null) {
                          try {
                              userInput = new String(decryptedText, "UTF8");
                          } catch (UnsupportedEncodingException ex) {
                                    Logger.getLogger(ManagementComponent.class.getName()).log(Level.SEVERE, null, ex);
                          }
                      } else {userInput=null;}
                      if (userInput!=null) {
                        String sManagerChallangeBase64FromScheduler=userInput;
                        if (newChallange.equals(sManagerChallangeBase64FromScheduler)) {
                          transacting=true;
                        } else {
                           feedback="Scheduler-Challange from manager is not the same!";
                        }
                      }
                  }
                }
               
                
                while( transacting ) {
                    decryptedText =  cryptChannel.receive();
                    if (decryptedText!=null) {
                        try {
                           userInput = new String(decryptedText, "UTF8");
                        } catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(ManagementComponent.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {userInput=null;}
                    //if userInput==null than Client has gone
                    if (userInput!=null) {
                        if (userInput.startsWith("Error:")) {
                           System.out.println(userInput);
                        } else {
                            tokens = new java.util.StringTokenizer(userInput);
                            userInput = tokens.nextToken();
                            feedback="";
                            ArrayList<TaskEngine> requestedEngines = new ArrayList<TaskEngine>();
                            if  ( userInput.equalsIgnoreCase("!exit") ) {
                              transacting = false;
                            } else if  ( userInput.equalsIgnoreCase("!requestEngine") ) {
                              String type = tokens.nextToken();
                              int expectedload=0;
                              if (type.equals("1")) { expectedload=33;}
                              else if (type.equals("2")) { expectedload=66;}
                              else if (type.equals("3")) { expectedload=100;}

                              int amount=Integer.parseInt( tokens.nextToken());
                              // request and save the current load of the available engines
                              for (int i = 0; i < amount; i++) {
                                RequestLoad();
                                TaskEngine bestengine = GetBestTaskEngine(expectedload,requestedEngines);
                                if (bestengine==null) {
                                     feedback="Error: No engine available for execution. Please try again later.";
                                     requestedEngines.clear();
                                     break;
                                } else {
                                     //update the load infomration
                                     bestengine.updateCurrLoad(expectedload);
                                     requestedEngines.add(bestengine);
                                }
                              }
                            } else {
                               feedback="can't understand you!";
                            }
                            if (requestedEngines.size()>0) {
                              for (TaskEngine bestengine : requestedEngines) {
                                feedback="Assigned engine: "+bestengine.hostAdress+" Port: "+bestengine.port;
                                cryptChannel.send((feedback+'\n').getBytes());
                              }
                            } else if (!feedback.isEmpty()) {
                              cryptChannel.send((feedback+'\n').getBytes());
                            }
                        }
                    } else {
                        transacting = false;
                        //"Client is not available!"
                    }
                }
            } catch (SocketException ex) {
                //"Client closed a connection!"; //raise no exception!
            } catch (IOException ex) {
                Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (AssertionError e) {
                System.out.println(e.getMessage());
                if (cryptChannel!=null) {
                  cryptChannel.send(("Error: "+e.getMessage()).getBytes());
                }
            } finally {
                try {
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
                    case 0: varialbeName="UDP-Port";
                            udpPort=Integer.parseInt(args[i]);
                            break;
                    case 1: varialbeName="Min";
                            min=Integer.parseInt(args[i]);
                            break;
                    case 2: varialbeName="Max";
                            max=Integer.parseInt(args[i]);
                            break;
                    case 3: varialbeName="Timeout";
                            timeout=Integer.parseInt(args[i]);
                            break;
                    case 4: varialbeName="checkPeriod";
                            checkPeriod=Integer.parseInt(args[i]);
                            break;
                }
           } catch (Exception e) {
                System.out.println(varialbeName+" ist kein Integerwert!");
                System.exit(1); //Programm beenden
           }
        }

        //read scheduler.properties file
       Config config = new Config("scheduler");
       tcpPort=config.getInt("tcp.port");
       enKeyFile=config.getString("key.en");
       deKeyFile=config.getString("key.de");

       /*
        InputStream in = ClassLoader.getSystemResourceAsStream("scheduler.properties");
        if (in != null) {
            try {
                Properties schedulerProps = new java.util.Properties();
                schedulerProps.load(in);
                String setting;
                Set<String> lines = schedulerProps.stringPropertyNames();
                for (String name : lines) {
                    setting=schedulerProps.getProperty(name);
                    if (name.equalsIgnoreCase("tcp.port") ) {
                         tcpPort=Integer.parseInt(setting);
                    } else if (name.equalsIgnoreCase("key.en") ) {
                         enKeyFile=setting;
                    } else if (name.equalsIgnoreCase("key.de") ) {
                         deKeyFile=setting;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }*/

        if (ReadKeys()) {
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

    //Read private und public key for secure channel
    private static boolean ReadKeys() {
        boolean result=false;
        PEMReader inPrivat=null,inPublic = null;
        try {
            //public key from scheduler
            try {
              inPublic = new PEMReader(new FileReader(enKeyFile));
            } catch (Exception e) {
                 System.out.println("Can't read file for public key!");
                 return false;
            }
            publickey= (PublicKey) inPublic.readObject();

            //private key       
            FileReader privateKeyFile=null;
            try {
               privateKeyFile=new FileReader(deKeyFile);
            } catch (Exception e) {
                 System.out.println("Can't read file for private key!");
                 return false;
            }
            
            inPrivat = new PEMReader(privateKeyFile, new PasswordFinder() {
                @Override
                 public char[] getPassword() {
                    // reads the password from standard input for decrypting the private key
                    System.out.println("Enter pass phrase:");
                    try {
                        return (new BufferedReader(new InputStreamReader(System.in))).readLine().toCharArray();
                    } catch (IOException ex) {
                        return "".toCharArray();
                    }
                 }
            });

           KeyPair keyPair = (KeyPair) inPrivat.readObject();
           privatekey = keyPair.getPrivate();
           result=true;
           System.out.println("Keys successfully initialized!");
        } catch (IOException ex) {
            System.out.println("Wrong password!");
            result=ReadKeys();
        } finally {
            try {
                if (inPublic!=null) {
                  inPublic.close();
                }
                if (inPrivat!=null) {
                  inPrivat.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            }
            return result;
        }
    }
}