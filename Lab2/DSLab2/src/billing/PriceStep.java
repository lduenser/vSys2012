package billing;

public class PriceStep {

	Double startPrice;
	Double endPrice;
	Double fixedPrice;
	Double variablePricePercent;
	
	public PriceStep(){
		
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
	
	
}
