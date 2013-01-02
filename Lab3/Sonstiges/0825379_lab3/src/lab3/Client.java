package lab3;
/**
 *
 * @author Ternek Marianne 0825379
 * Jänner 2012
 */
import java.io.*;
import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.util.Set;
import org.bouncycastle.util.encoders.Base64;


public class Client {
    private static String managementComponent;// = "yourBindingName";
    private static String taskDir;// ="your/taskDir";
    private static String keysDirectory;

    private static Remote remCallObj = null;

    private static ArrayList<Socket> taskEngineSocketList = new ArrayList<Socket>();

    private static INotifyClient  notifyStub =null;

    
    //get Filenames of TaskDir
    private static String ReadFileNames() {
        String feedback = "";
        File dir = new File(taskDir);
        if (dir.exists()) {
            String[] files = dir.list();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    // Get filename of file or directory
                    String filename = files[i];
                    feedback = feedback + filename + '\n';
                }
            }
        } else {
            feedback="folder does not exist!";
        }
        return feedback;
    }

     private static boolean isLoggedInAsAdmin() {
         boolean bAdmin=false;

         if (remCallObj!=null) {
            bAdmin=(remCallObj instanceof IAdminCallback);
         }

         return bAdmin;
     }
  
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       for (int i = 0; i < args.length; i++) {
            switch (i) {
                case 0: managementComponent=args[i]; break;
                case 1: taskDir=args[i]; break;
            }
       }

       //read client.properties file
       InputStream in = ClassLoader.getSystemResourceAsStream("client.properties");
       if (in != null) {
            try {
                Properties clientProps = new java.util.Properties();
                clientProps.load(in);
                String setting;
                Set<String> lines = clientProps.stringPropertyNames();
                for (String name : lines) {
                    setting=clientProps.getProperty(name);
                     if (name.equalsIgnoreCase("keys.dir") ) {
                         keysDirectory=setting;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
       }
       
       Properties regProp= UtilityClass.GetRegistryProps();

       if (regProp==null) {
            System.out.println("Reg.Properties file could not be found!");
       } else {
            try {
               NotifyClient notifyRemObj= new NotifyClient();
               notifyStub= (INotifyClient)UnicastRemoteObject.exportObject(notifyRemObj, 0);
              
               String registryHost = regProp.getProperty("registry.host");
               Integer registryPort = Integer.parseInt(regProp.getProperty("registry.port"));
               Registry registry = LocateRegistry.getRegistry(registryHost, registryPort);
               IRemoteLogin remRef = (IRemoteLogin) registry.lookup(managementComponent);

               String sentence;
               String feedback;
               int load=0;
               int id;
               BufferedReader inFromUser = new BufferedReader(new InputStreamReader (System.in));
               try {
                    StringTokenizer tokens;
                    String loggedUsername="";
                    boolean transacting = true;
                    String userInput="";
                    while( transacting ) {
                        sentence = inFromUser.readLine();
                        if (sentence.equals("")) {continue; }
                        tokens = new java.util.StringTokenizer(sentence);
                        userInput = tokens.nextToken();
                        
                        feedback="";
                        if  (userInput.equalsIgnoreCase("!exit") ) {
                           if (isLoggedInAsAdmin()) {
                             IAdminCallback adminCallback =  (IAdminCallback)remCallObj;
                             adminCallback.logout();
                             remCallObj=null;
                             loggedUsername="";
                           } else if (remCallObj!=null) {
                              ICompanyCallback companyCallback =  (ICompanyCallback)remCallObj;
                              companyCallback.logout();
                              remCallObj=null;
                              loggedUsername="";
                           }
                           transacting=false;
                           if (notifyStub!=null) {
                             UnicastRemoteObject.unexportObject(notifyRemObj, true);
                           }
                        } else if ((!userInput.equalsIgnoreCase("!login") ) &&
                            (remCallObj==null)) {
                           feedback="You have to log in first!";
                        } else {
                             if  (( userInput.equalsIgnoreCase("!login") ) && (remCallObj!=null)){
                                  feedback="You are already logged in. Please logout to login as an other user.";
                             } else if  ( userInput.equalsIgnoreCase("!login") ) {
                                 try {
                                   String companyname=""; String password="";
                                   if (tokens.hasMoreTokens()) {   companyname = tokens.nextToken();}
                                   if (tokens.hasMoreTokens()){   password = tokens.nextToken(); }

                                   remCallObj=remRef.login(companyname,password);
                                   if (remCallObj==null) {
                                        throw new RemoteException("Something went wrong!");
                                   } else {
                                           loggedUsername=companyname;
                                           feedback="Successfully logged in. Using ";
                                           if (isLoggedInAsAdmin()) {
                                               feedback=feedback+"admin";
                                           } else {
                                               feedback=feedback+"company";
                                           }
                                           feedback=feedback+" mode.";
                                   }
                                 } catch (RemoteException e) {
                                    feedback=e.detail.getMessage();
                                 }
                             //commands for admin
                             } else if ( isLoggedInAsAdmin() ) {
                               IAdminCallback adminCallback =  (IAdminCallback)remCallObj;
                               if ( userInput.equalsIgnoreCase("!logout") ) {
                                 try {
                                   adminCallback.logout();
                                   remCallObj=null;
                                   loggedUsername="";
                                   feedback="Successfully logged out.";
                                 } catch (RemoteException e) {
                                    feedback=e.detail.getMessage();
                                 }
                               } else if ( userInput.equalsIgnoreCase("!getPricingCurve") ) {
                                    feedback=adminCallback.getPricingCurve();
                               } else if ( userInput.equalsIgnoreCase("!setPriceStep") ) {
                                    if (tokens.countTokens()==2) {
                                       int taskCount=-1;
                                       double percent=-1;
                                       try { taskCount  = Integer.parseInt(tokens.nextToken()); } catch (Exception ex) {taskCount = -1;}
                                       try { percent  = Double.parseDouble(tokens.nextToken()); } catch (Exception ex) {percent = -1;}
                                       try {
                                         feedback=adminCallback.setPriceStep(taskCount,percent);
                                       } catch (RemoteException e) {
                                         feedback=e.detail.getMessage();
                                       }
                                    } else { feedback = "wrong parameters"; }

                               //all commands for companys are not allowed
                               } else if ((userInput.equalsIgnoreCase("!list") ) ||
                                          (userInput.equalsIgnoreCase("!prepare") ) ||
                                          (userInput.equalsIgnoreCase("!executeTask") ) ||
                                          (userInput.equalsIgnoreCase("!executeDistributed") ) ||
                                          (userInput.equalsIgnoreCase("!info")) ||
                                          (userInput.equalsIgnoreCase("!credits")) ||
                                          (userInput.equalsIgnoreCase("!buy"))  ||
                                          (userInput.equalsIgnoreCase("!getOutput"))){
                                   feedback="Command not allowed. You are not a company!";
                               } else {
                                   feedback="can't understand you.";
                               }
                             //commands for companies
                             } else {
                               ICompanyCallback companyCallback =  (ICompanyCallback)remCallObj;
                               if ( userInput.equalsIgnoreCase("!list") ) {
                                 feedback=ReadFileNames();
                               } else if ( userInput.equalsIgnoreCase("!logout") ) {
                                 try {
                                   companyCallback.logout();
                                   remCallObj=null;
                                   loggedUsername="";
                                   feedback="Successfully logged out.";
                                 } catch (RemoteException e) {
                                    feedback=e.detail.getMessage();
                                 }
                               } else  if ( userInput.equalsIgnoreCase("!prepare") ) {
                                     if (tokens.countTokens()==2) {
                                         String filename=tokens.nextToken();
                                         if ( UtilityClass.FileExists(taskDir,filename)) {
                                             File myFile = new File(taskDir+"/"+filename);
                                             byte[] filecontent= UtilityClass.getBytesFromFile(myFile);
                                             userInput= tokens.nextToken();
                                             load=0;
                                             if  ( userInput.equalsIgnoreCase("LOW") ) { load=1; }
                                             else if  ( userInput.equalsIgnoreCase("MIDDLE") ) { load=2; }
                                             else if  ( userInput.equalsIgnoreCase("HIGH") ) { load=3; }
                                             if (load>0) {
                                                try {
                                                    int taskid=companyCallback.prepareTask(filename, load,filecontent);
                                                    feedback="Task with id "+taskid+" prepared.";
                                                } catch (RemoteException ex) {
                                                    feedback=ex.detail.getMessage();
                                                }
                                             } else {
                                               feedback="Please choose between: LOW, MIDDLE or HIGH";
                                             }
                                         } else {
                                            feedback="Task not found.";
                                         }
                                     } else { feedback = "wrong parameters"; }
                               } else if  (userInput.equalsIgnoreCase("!executeTask") ) {
                                   try {
                                     id  = Integer.parseInt(tokens.nextToken());
                                   } catch (Exception ex) {
                                     id = 0;
                                   }
                                   //switch delimter to " --> for script-text
                                   try {
                                       tokens.nextToken("\"");
                                       String script=  tokens.nextToken();

                                       feedback=companyCallback.executeTask(id, script,notifyStub);
                                   } catch (NoSuchElementException e) {
                                        feedback="wrong parameters";
                                   } catch (RemoteException ex) {
                                        feedback=ex.detail.getMessage();
                                   }
                                } else if  (userInput.equalsIgnoreCase("!executeDistributed") ) {
                                   try {
                                     id  = Integer.parseInt(tokens.nextToken());
                                   } catch (Exception ex) {
                                     id = 0;
                                   }
                                   int amount;
                                   try {
                                     amount  = Integer.parseInt(tokens.nextToken());
                                   } catch (Exception ex) {
                                     amount = 0;
                                   }
                                   //switch delimter to " --> for script-text
                                   try {
                                       tokens.nextToken("\"");
                                       String script=  tokens.nextToken();

                                       feedback=companyCallback.executeDistributedTask(id,amount, script,notifyStub);
                                   } catch (NoSuchElementException e) {
                                        feedback="wrong parameters";
                                   } catch (RemoteException ex) {
                                        feedback=ex.detail.getMessage();
                                   }

                               }  else if ( userInput.equalsIgnoreCase("!info") ) {
                                   try {
                                     id  = Integer.parseInt(tokens.nextToken());
                                   } catch (Exception ex) {
                                     id = 0;
                                   }
                                    try {
                                       feedback=companyCallback.taskInfo(id);
                                   } catch (RemoteException ex) {
                                        feedback=ex.detail.getMessage();
                                   }
                               }  else if ( userInput.equalsIgnoreCase("!credits") ) {
                                   feedback="You have " +companyCallback.getCompanyCredits()+" credits left.";
                               }  else if ( userInput.equalsIgnoreCase("!buy") ) {
                                   int credits=0;
                                   if (tokens.hasMoreTokens()) {
                                       try {
                                         credits  = Integer.parseInt(tokens.nextToken());
                                       } catch (Exception ex) {
                                         credits = 0;
                                       }
                                       if (credits>0) {
                                         companyCallback.incCompanyCredits(credits);
                                         feedback="Successfully bought credits. You have "+companyCallback.getCompanyCredits()+" credits left.";
                                       }  else {
                                         feedback="Error: invalid amount of credits";
                                       }
                                   } else {
                                       feedback="wrong parameters";
                                   }
                               }  else if ( userInput.equalsIgnoreCase("!getOutput") ) {
                                   try {
                                     id  = Integer.parseInt(tokens.nextToken());
                                   } catch (Exception ex) {
                                     id = 0;
                                   }
                                   try {
                                        feedback="";
                                        //do it twice
                                        for (int i = 0; i < 2; i++) {
                                          
                                          String outputMacStr=companyCallback.getTaskOutput(id);
                                          StringTokenizer outputtoken = new java.util.StringTokenizer(outputMacStr);
                                          feedback=outputtoken.nextToken("###");
                                          System.out.println(feedback);
                                          if (feedback.startsWith("Error: ")) {
                                             feedback="";
                                             break;
                                          } else {
                                              OutputHMac outputMacClient= new OutputHMac(keysDirectory,loggedUsername);
                                              outputMacClient.output=feedback;
                                              feedback="";
                                              outputMacClient.updateHMac();
                                              String recHash=outputtoken.nextToken();
                                              boolean validHash= MessageDigest.isEqual(outputMacClient.hash, Base64.decode(recHash.getBytes()));
                                              if (validHash) {
                                                  break;
                                              } else {
                                                  if (i==0) {
                                                    System.out.println("Integrity check failed. Try again...");
                                                  }
                                              }
                                          }
                                       }    
                                   } catch (RemoteException ex) {
                                        feedback=ex.detail.getMessage();
                                   }
                               } else if ((userInput.equalsIgnoreCase("!getPricingCurve") ) ||
                                          (userInput.equalsIgnoreCase("!setPriceStep") )){
                                   feedback="Command not allowed. You are not a admin!";
                               } else {
                                   feedback="can't understand you.";
                               }
                             }
                        }
                        if (!feedback.isEmpty()) {
                          System.out.println(feedback);
                        }
                    }
               } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
               }
            } catch (NotBoundException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
       }
    }

}
