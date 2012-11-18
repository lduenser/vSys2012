package events;

public abstract class Event {

	String id; // um duplikate zu vermeiden: Prefix verwenden, zB: Auction123, Analytics123,...
	String type;
	long timestamp;
	
}
