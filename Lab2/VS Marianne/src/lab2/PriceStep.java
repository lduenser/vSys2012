package lab2;

/**
 *
 * @author ternekma
 */

public class PriceStep implements Comparable<PriceStep>{
     double discount ;
     int executionCnt;

     PriceStep(int executionCnt, double discount) {
      this.executionCnt=executionCnt;
      this.discount=discount;
     }

    public int compareTo(PriceStep argument) {
        if( executionCnt < argument.executionCnt )
            return -1;
        if( executionCnt > argument.executionCnt )
            return 1;

        return 0;
    }
}