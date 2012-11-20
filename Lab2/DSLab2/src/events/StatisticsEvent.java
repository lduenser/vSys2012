package events;

import events.BidEvent.types;

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
	
	/*
	 * type is either of:
	 * - USER_SESSIONTIME_MIN
	 * - USER_SESSIONTIME_MAX
	 * - USER_SESSIONTIME_AVG
	 * - BID_PRICE_MAX
	 * - BID_COUNT_PER_MINUTE
	 * - AUCTION_TIME_AVG
	 * - AUCTION_SUCCESS_RATIO
	 */

}
