package billing.model;

import java.io.Serializable;
import java.util.ArrayList;

public class PriceSteps implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 13086410831029628L;
	ArrayList<PriceStep> priceStepList;
	public enum addErrorTypes {
		NEGATIVE,
		COLLISION,
		NONE
	}
	
	public enum removeErrorTypes {
		NOT_FOUND,
		NONE
	}
	
	public PriceSteps() {
		priceStepList = new ArrayList<PriceStep>();
	}
	
	public void addStep(PriceStep step) {
		priceStepList.add(getPosition(step), step);
	}
	
	public void removeStep(PriceStep step) {

		priceStepList.remove(getStep(step));
	}
	
	public int getPosition(PriceStep step) {
		for(PriceStep priceStep:priceStepList) {
			if(step.startPrice < priceStep.getStartPrice()) return priceStepList.indexOf(priceStep);
		}
		return priceStepList.size();
	}
	
	public PriceStep getStep(PriceStep step) {
		
		for(PriceStep priceStep:priceStepList) {
			
			if(priceStep.comparePriceStep(step)) return priceStep;
		}
		
		return null;
	}
	
	public removeErrorTypes checkRemoveStep(PriceStep step) {
		
		for(PriceStep priceStep:priceStepList) {
			
			if(priceStep.comparePriceStep(step)) return removeErrorTypes.NONE;
		}
		
		return removeErrorTypes.NOT_FOUND;
	}
	
	public addErrorTypes checkAddStep(PriceStep step) {
		
		if(step.hasNegativeValues()) return addErrorTypes.NEGATIVE;
		
		for(PriceStep priceStep:priceStepList) {
			if(priceStep.checkOverlap(step)) return addErrorTypes.COLLISION;
		}
		
		return addErrorTypes.NONE;
	}
	
	public PriceStep getStepByPrice(double price) {
		
		for(PriceStep priceStep:priceStepList) {
			if(priceStep.checkInside(price)) return priceStep;
		}
		
		return null;
	}
	
	public String toString() {
		String temp = "" +
		"Min_Price\tMax_Price\tFee_Fixed\tFee_Variable";
		
		for(PriceStep step:priceStepList) {
			temp+="\r\n"+step.toString();
		}
		
		return temp;
	}

}