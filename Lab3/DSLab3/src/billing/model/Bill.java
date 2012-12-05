package billing.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Bill implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5927966445351852822L;
	ArrayList<BillLine> billLines;
	
	public Bill(){
		billLines = new ArrayList<BillLine> ();
	
	}
	
	public void addLineCalculate(BillLine line, PriceSteps priceSteps) {
		
		BillLine temp = line;
		line.calculatePrice(priceSteps);
		
		billLines.add(temp);
	}
	
	public void addLine(BillLine line) {
		billLines.add(line);
	}
	
	public Bill getUserBill(String user, PriceSteps priceSteps) {
		
		Bill temp = new Bill();
		
		for(BillLine line:billLines) {
			if(line.getUser().equals(user)) {
				temp.addLineCalculate(line, priceSteps);
			}
		}
		
		return temp;
	}
	
	public String toString() {
		String temp = ""+
		"auction_ID\tstrike_price\tfee_fixed\tfee_fariable\tfee_total";
		
		for(BillLine line:billLines) {
			temp+="\r\n"+line.toString();
		}
		
		return temp;
	}
}
