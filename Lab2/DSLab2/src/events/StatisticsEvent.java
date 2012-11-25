package events;

public class StatisticsEvent extends Event{
	
	Double value;
	
	public enum types {
		USER_SESSIONTIME_MIN,
		USER_SESSIONTIME_MAX,
		USER_SESSIONTIME_AVG,
		BID_PRICE_MAX,
		BID_COUNT_PER_MINUTE,
		AUCTION_TIME_AVG,
		AUCTION_SUCCESS_RATIO
	}
	
	public StatisticsEvent(types type, Double value) {
		super();
		
		this.type = type.toString();
		this.value = value;
	}
	
	public String toString() {
		String info = "";
		
		if(this.type.equals(types.USER_SESSIONTIME_MIN.toString())) {
			info = "minimum session time is " + this.value.toString() + " seconds";
		}
		if(this.type.equals(types.USER_SESSIONTIME_MAX.toString())) {
			info = "maximum session time is " + this.value.toString() + " seconds";
		}
		if(this.type.equals(types.USER_SESSIONTIME_AVG.toString())) {
			info = "average session time is " + this.value.toString() + " seconds";
		}
		
		if(this.type.equals(types.BID_PRICE_MAX.toString())) {
			info = "maximum bid price seen so far is " + this.value.toString();
		}
		if(this.type.equals(types.BID_COUNT_PER_MINUTE.toString())) {
			info = "current bids per minute is " + this.value.toString();
		}
		
		if(this.type.equals(types.AUCTION_TIME_AVG.toString())) {
			info = "average auction time is " + this.value.toString() + " seconds";
		}
		if(this.type.equals(types.AUCTION_SUCCESS_RATIO.toString())) {
			info = "current auction success ratio is " + this.value.toString();
		}
		
		return this.getHead() + info;
	}
}
