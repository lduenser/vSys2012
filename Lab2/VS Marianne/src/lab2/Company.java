package lab2;

import java.util.ArrayList;

/**
 *
 * @author ternekma
 */
//Object for companies
public class Company {
  String username;
  String password;
  int credits;
  boolean admin; 
  boolean online;
  //number of executed tasks not of requested (lab1)
  int low,middle,high;
  //number of started executions (needed for discount calculation):
  int executionCnt;

  Company(String username, String password) {
     this.username=username;
     this.password=password;
     online=false;
     admin=false;
     credits=0;
     low=0;
     middle=0;
     high=0;
     executionCnt=0;
  }

  public synchronized void incStartExecutions() {
    executionCnt++;
  }

  public synchronized void incFinishedExecutions(int expectedload) {
    if (expectedload==1) { low++;}
    else if (expectedload==2) { middle++;}
    else if (expectedload==3) { high++;}
  }




  public synchronized int getExecutions() {
    return executionCnt;
  }

  public synchronized void incCredits (int incCredits, boolean bexecutionCntAlreadyAddedForThisExecution) {
      //decrease credits after preparing or executing
      int tempExecutionCnt=executionCnt;
      if (bexecutionCntAlreadyAddedForThisExecution) { tempExecutionCnt--;}
      if (incCredits<0) {
         //find discount
         double discount = PriceStepList.getDiscountForTaskCount(tempExecutionCnt);
         if (discount>0) {
           //if -1 and discount, than let it with -1 
           incCredits= (int) Math.floor((incCredits-((double)incCredits/100)*discount));
         }
         if (incCredits>0) {incCredits=0;}
      }
      this.credits=this.credits+incCredits;
  }

  public synchronized int getCredits () {
      return this.credits;
  }

  public synchronized void setCredits (int acredits) {
      this.credits=acredits;
  }

  public static Company findCompany( ArrayList<Company> companyList,String companyName) {
      Company foundcompany=null;
      for (Company company : companyList) {
          if (company.username.equals(companyName)) {
              foundcompany=company;
              break;
          }
      }
      return foundcompany;
  }
}
