package lab3;

/**
 *
 * @author Ternek Marianne 0825379
 * Jänner 2012
 */

import java.io.BufferedReader;
import java.io.FileReader;
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
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

public class ManagementComponent {

    private static String bindingName;// = "yourBindingName";
    public static String schedulerHost;// = "localhost";
    public static int schedulerTCPPort;
    public static String enKeyFile, deKeyFile,keyDirectory;
    public static int preparationCosts;// = 30;
    public static String taskDir;//= "your/managementTaskDir";

    private static ArrayList<Socket> socketList = new ArrayList<Socket>();
    public static ArrayList<Remote> exportedRemoteObjects = new ArrayList<Remote>();

    public static PublicKey publickey = null;
    public static PrivateKey privatekey = null;

    public static synchronized void RequestEngineExecuteTask(Task task,INotifyClient notifyRemObj, Company company, int amount ) {
        Socket clientSocket=null;
        try {
            //create socket between management component and scheduler
            clientSocket = new Socket(ManagementComponent.schedulerHost, ManagementComponent.schedulerTCPPort);
            socketList.add(clientSocket);

            CryptChannel cryptChannel= new CryptChannel(new Base64Channel(new TCPChannel(clientSocket)));
            cryptChannel.setKey(publickey);
            cryptChannel.setalgorithm("RSA/NONE/OAEPWithSHA256AndMGF1Padding");

            String sManagerChallangeBase64 = UtilityClass.getRandomNumber(32);
            String firstMessage=("!login "+sManagerChallangeBase64);
            assert firstMessage.matches("!login ["+UtilityClass.B64+"]{43}=") : "1st message is not well-formed";

            Thread serverthread = new Thread(new SchedulerThread(task,clientSocket,socketList,notifyRemObj,company,amount,publickey,sManagerChallangeBase64));
            serverthread.start();

            cryptChannel.send(firstMessage.getBytes());
            serverthread.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(CompanyCallback.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(CompanyCallback.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CompanyCallback.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AssertionError e) {
                try {
                  clientSocket.close();
                } catch (IOException ex) {
                }
                System.out.println(e.getMessage());
                if ((company!=null) &&(company.online)) {
                try {
                    notifyRemObj.notifyMessage("Error: "+e.getMessage());
                } catch (RemoteException ex) {
                }
           }
        } catch (Exception e) {
            //print out on ManagerComponent
            System.out.println("Scheduler is down!");
             Logger.getLogger(ManagementComponent.class.getName()).log(Level.SEVERE, null, e);
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
                    case 2: varialbeName="preparationCosts";
                            preparationCosts=Integer.parseInt(args[i]);
                            break;
                    case 3: varialbeName="TaskDir";
                            taskDir=args[i];
                            break;
                }
           } catch (Exception e) {
                System.out.println(varialbeName+" ist kein Integerwert!");
                bok=false;
           }
        }

       //read manager.properties file
       Config config = new Config("manager");
       schedulerTCPPort=config.getInt("scheduler.tcp.port");
       enKeyFile=config.getString("key.en");
       deKeyFile=config.getString("key.de");
       keyDirectory=config.getString("keys.dir");
       /*InputStream in = ClassLoader.getSystemResourceAsStream("manager.properties");
       if (in != null) {
            try {
                Properties managerProps = new java.util.Properties();
                managerProps.load(in);
                String setting;
                Set<String> lines = managerProps.stringPropertyNames(); 
                for (String name : lines) {
                    setting=managerProps.getProperty(name);
                    if (name.equalsIgnoreCase("scheduler.tcp.port") ) {
                         schedulerTCPPort=Integer.parseInt(setting);
                    } else if (name.equalsIgnoreCase("key.en") ) {
                         enKeyFile=setting;
                    } else if (name.equalsIgnoreCase("key.de") ) {
                         deKeyFile=setting;
                    } else if (name.equalsIgnoreCase("keys.dir") ) {
                         keyDirectory=setting;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ManagementComponent.class.getName()).log(Level.SEVERE, null, ex);
            }
       }*/
       if (ReadKeys()) {
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
                        Logger.getLogger(ManagementComponent.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(ManagementComponent.class.getName()).log(Level.SEVERE, null, ex);
            }
            return result;
        }
    }

}
