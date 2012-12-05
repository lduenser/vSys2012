package billing.model;

import java.io.Serializable;

public class BillLine implements Serializable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 5410686365350948562L;

	String user;
	
	long auctionId;
	double strike_price;
	double fee_fixed;
	double fee_variable;
	double fee_total;
	
	public BillLine(String user, long auctionId, double strike_price) {
		
		this.user = user;
		this.auctionId = auctionId;
		this.strike_price = strike_price;
	}
	
	boolean calculatePrice(PriceSteps list) {
		
		PriceStep step = list.getStepByPrice(this.strike_price);
		
		if(step!=null) {
			fee_fixed = step.getFixedPrice();
			fee_variable = strike_price * step.getVariablePricePercent();
			fee_total =	new Double(new Double(fee_variable).doubleValue() + new Double(fee_fixed).doubleValue());
			
			return true;
		}
		
		return false;
	}
	
	public String getUser() {
		return user;
	}
	
	public String toString() {
		String temp = "";
		
		temp+=this.auctionId + "\t\t" + this.strike_price + "\t\t" + this.fee_fixed + "\t\t" + this.fee_variable + "\t\t" + this.fee_total;
		
		return temp;
	}
}
