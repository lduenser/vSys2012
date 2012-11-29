package billing.model;

import java.io.Serializable;

public class PriceStep implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1197915023576412121L;
	Double startPrice;
	Double endPrice;
	Double fixedPrice;
	Double variablePricePercent;
	
	public PriceStep(){
		
	}
	
	public PriceStep(Double startPrice, Double endPrice, Double fixedPrice,
			Double variablePricePercent) {
		this.startPrice = startPrice;
		this.endPrice = endPrice;
		this.fixedPrice = fixedPrice;
		this.variablePricePercent = variablePricePercent;
	}
	
	public PriceStep(Double startPrice, Double endPrice) {
		this.startPrice = startPrice;
		this.endPrice = endPrice;
	}

	
	public Double getStartPrice() {
		return startPrice;
	}
	public void setStartPrice(Double startPrice) {
		this.startPrice = startPrice;
	}
	public Double getEndPrice() {
		return endPrice;
	}
	public void setEndPrice(Double endPrice) {
		this.endPrice = endPrice;
	}
	public Double getFixedPrice() {
		return fixedPrice;
	}
	public void setFixedPrice(Double fixedPrice) {
		this.fixedPrice = fixedPrice;
	}
	public Double getVariablePricePercent() {
		return variablePricePercent;
	}
	public void setVariablePricePercent(Double variablePricePercent) {
		this.variablePricePercent = variablePricePercent;
	}
	
	public boolean hasNegativeValues() {
		
		if(startPrice==endPrice) return true;
		if(startPrice < 0 || endPrice < 0 || fixedPrice < 0 || variablePricePercent < 0) return true;
		
		return false;
	}
	
	public boolean checkOverlap(PriceStep step) {
		
		if(this.endPrice <= step.startPrice && this.endPrice > 0) return false;
		if(step.endPrice <= this.startPrice && step.endPrice > 0) return false;
		
		return true;
	}
	
	public boolean comparePriceStep(PriceStep step) {
		
		if(this.startPrice.equals(step.startPrice) && this.endPrice.equals(step.endPrice)) return true;
		
		return false;
	}
	
	public boolean checkInside(double price) {
		
		if(price >= startPrice && (price <= endPrice || endPrice.equals(0.0))) return true;
		
		return false;
	}
	
	public String toString() {
		
		String temp = "";
		
		String end = this.endPrice.toString();
		if(this.endPrice==0) {
			end = "INFINITY";
		}
	
		temp+=this.startPrice + "\t\t" + end + "\t\t" + this.fixedPrice + "\t\t" + this.variablePricePercent + "%";
		
		return temp;
	}
	
	
}
