package server;
import model.AuctionList;
import model.PendingNotifications;
import model.UserList;

public class DataHandler {
	public static PendingNotifications pendingNotifications;
	public static AuctionList auctions;
	public static UserList users;

	public DataHandler() {
		pendingNotifications = new PendingNotifications();
		auctions = new AuctionList();
		users = new UserList();
		
		/*
		
		users.login("hans", null, 12);
		users.login("peter", null, 13);
		users.login("fred", null, 14);
		
		Auction a1 = new Auction("tv", users.getUserById(1), 24);
		Auction a2 = new Auction("mac", users.getUserById(2), 20);
		
		auctions.addAuction(a1);
		auctions.addAuction(a2);
		
		auctions.getAuctionById(1).bid(new Bid(users.getUserById(1), 400.00));
		auctions.getAuctionById(1).bid(new Bid(users.getUserById(1), 600.00));
		auctions.getAuctionById(1).bid(new Bid(users.getUserById(1), 500.00));
		auctions.getAuctionById(2).bid(new Bid(users.getUserById(1), 700.00));
		
		users.logout("fred");
		users.logout("peter");
		users.logout("hans");
		
		*/
	}
}
