package model;

public class Bid {
	private User user;
	private Double money;
	
	public Bid(User user, Double money) {
		this.user = user;
		this.money = money;
	}
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Double getMoney() {
		return money;
	}
	public void setMoney(Double money) {
		this.money = money;
	}
}
