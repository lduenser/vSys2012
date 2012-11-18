package lab2;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoteLogin extends UnicastRemoteObject implements IRemoteLogin{
    //CompanyList contains all companies and their stati
    public static ArrayList<Company> companyList = new ArrayList<Company>();
    
    private static boolean ReadCompanies () {
        InputStream in = ClassLoader.getSystemResourceAsStream("user.properties");
        StringTokenizer tokens;
        if (in != null) {
            try {
                Properties companies = new java.util.Properties();
                companies.load(in);
                String company, setting,value;
                Set<String> lines = companies.stringPropertyNames(); // get all company names
                for (String line : lines) {
                    tokens = new java.util.StringTokenizer(line,".");
                    Company companyObj;
                    //if one token than username and password
                    if (tokens.countTokens()==1) {
                       companyObj = Company.findCompany(companyList, line);
                       if (companyObj==null) {
                          companyList.add(new Company(line,companies.getProperty(line)));
                       } else {
                          companyObj.password= companies.getProperty(line);
                       }
                    } else if (tokens.countTokens()==2) {
                       value= companies.getProperty(line);
                       company=tokens.nextToken();
                       setting=tokens.nextToken();
                       companyObj = Company.findCompany(companyList, company);
                       if (companyObj==null) {
                          companyObj= new Company(company,"");
                          companyList.add(companyObj);
                       }
                       if (companyObj!=null) {
                         if (setting.equalsIgnoreCase("admin")) {
                            if (value.equalsIgnoreCase("true")) {
                                companyObj.admin=true;
                            } else if (value.equalsIgnoreCase("false")) {
                                companyObj.admin=false;
                            }
                         } else if (setting.equalsIgnoreCase("credits")) {
                            try {
                              companyObj.setCredits(Integer.parseInt(value));
                            } catch (NumberFormatException e) {
                              companyObj.setCredits(0);
                            }
                         }
                       }
                    }
                }
                return true;
            } catch (IOException ex) {
                Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            // company.properties could not be found
            System.out.println("Properties file could not be found!");
        }
        return false;
    }

    private static Company CheckLogin(String username,String password) {
        Company returnCompany=null;
        for (Company company : companyList) {
            if (company.username.equalsIgnoreCase(username)) {
                if (company.password.equals(password)) {
                    returnCompany=company;
                }
                break;
            }
        }
        return returnCompany;
     } 


    // Defaultkonstruktor muß RemoteException werfen
   public RemoteLogin() throws RemoteException
   {
       ReadCompanies();   
   }


    public Remote login( String companyname, String password) throws RemoteException {
       Company company = null;
       company=CheckLogin(companyname, password);

       if (company!=null) {
         if (!company.online) {
             company.online=true;
             Remote remObj=null;
             if (!company.admin) {
                 remObj= new CompanyCallback(company);
              }  else {
                 remObj= new AdminCallback(company); 
              }
              ManagementComponent.exportedRemoteObjects.add(remObj);
              return remObj;
         } else  {
           throw new RemoteException("An other client is already logged in with this username. Try again later.");
         } 
       } else {
           throw new RemoteException("Wrong name or password.");
       }
    }

}
