package lab3;

/**
 *
 * @author Ternek Marianne 0825379
 * Jänner 2012
 */

import java.util.ArrayList;

public class PriceStepList {
   //PriceStepList contains all PriceSteps
    public static ArrayList<PriceStep> priceStepList = new ArrayList<PriceStep>(); // Liste nur für PriceStep-Objekte


    /*****START NEED TO BE THREAD SAFE */
    //function and procedures for PriceSteps and the PriceSteplist:
    //update PriceStep in list
    public synchronized static void updatePriceStep(PriceStep step, int executionCnt, double discount) {
       step.discount=discount;
       step.executionCnt=executionCnt;
    }
    //Update or add a new Task to List
    public synchronized static void UpdateOrAddPriceStep(int executionCnt, double discount) {
        boolean bfound=false;
        for (PriceStep step : priceStepList) {
           if (step.executionCnt==executionCnt) {
              bfound=true;
              updatePriceStep(step, executionCnt,discount);
              break;
           }
        }
        if (!bfound) {
          PriceStep step = new PriceStep(executionCnt, discount);
          priceStepList.add(step);
          java.util.Collections.sort( priceStepList );
        }
    }

    //Get discount for count of executed tasks
    public synchronized static double getDiscountForTaskCount(int count) {
        double discount= 0;
        int currCnt=0;

        for (PriceStep step : priceStepList) {
           if ((step.executionCnt<=count) && (step.executionCnt>currCnt)) {
                currCnt=step.executionCnt;
                discount=step.discount;
           }
        }
        return discount;
    }
    
    //Get text for !getPricingCurve command
    public synchronized static String getAllPriceSteps() {
        String returntext= "Task count | Discount";

        for (PriceStep step : priceStepList) {
           returntext=returntext+"\n"+step.executionCnt+" | "+step.discount+" %";
        }
        return returntext;
    }
    /*****END NEED TO BE THREAD SAFE */

}
