package management.model;

public class Subscription {
	String filterString = null;
	int id = 0;
	
	public Subscription(int id, String filterString) {
		this.id = id;
		this.filterString = filterString;
	}
	
	public int getId() {
		return id;
	}
	
	public String getFilterString() {
		return filterString;
	}
}
