package threads;
import server.AuctionServer;
import server.DataHandler;
import model.Notification;
import debug.Debug;


public class UpdateThread implements Runnable{
	
    public void run(){
        
        while(AuctionServer.active){
        	DataHandler.auctions.updateList();
        	
        	while(null != DataHandler.pendingNotifications.getNotificationFromOnlineUser(DataHandler.users)) {
        		Notification temp = DataHandler.pendingNotifications.getNotificationFromOnlineUser(DataHandler.users);
        		boolean sent = false;
        		
        		Debug.printDebug("Try to send notification: " + temp.toString() + " to " + temp.getUser().getName());
        		
        		sent = Notification.sendNotification(temp);
        		if(sent) DataHandler.pendingNotifications.removeNotification(temp);
        	}
        	
        	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Debug.printError(e.toString());
			}
      
        }
    }
    
    public synchronized void stop() {
    	Debug.printInfo("UpdateThread stopped.") ;
    }
}