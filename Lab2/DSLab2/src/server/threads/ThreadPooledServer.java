package server.threads;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import server.AuctionServer;

import debug.Debug;


public class ThreadPooledServer implements Runnable{

	protected int maxClients = 10;
    protected int port;
    protected ServerSocket serverSocket;
    
    protected boolean isStopped = false;
    protected Thread runningThread = null;
    protected ExecutorService threadPool = Executors.newFixedThreadPool(maxClients);
    protected ArrayList<Socket> clients = null;

    public ThreadPooledServer(int port) throws IOException{
        this.port = port;
        openServerSocket();
        clients = new ArrayList<Socket>();
    }
    
    public ThreadPooledServer(int port, int maxClients) throws IOException{
        this.port = port;
        this.maxClients = maxClients;
        openServerSocket();
        clients = new ArrayList<Socket>();
    }

    public void run(){
        
    	while(AuctionServer.active){
        	
        	for(Socket s:clients) {
            	if(s.isClosed()) {
            		clients.remove(s);
            	}
            }
        	
            Socket clientSocket = null;
            try {
            	clientSocket = this.serverSocket.accept();
                System.out.println("New client connected:");
                System.out.println("IP: " + clientSocket.getInetAddress().getHostAddress());
                System.out.println("Port: " + clientSocket.getPort());
                System.out.println("Open clientSpots: " + (maxClients-clients.size()));
                
                if(maxClients-clients.size() == 0) {
                	PrintWriter printer = new PrintWriter(clientSocket.getOutputStream(), true);
                	printer.println("Couldn't connect: Too many clients connected!");
                	printer.flush();
                	
                	clientSocket.close();
                    clientSocket = null;
                }
                
            } catch (IOException e) {
            	if(!AuctionServer.active) break;
            	
            	Debug.printError("Error accepting client connection");
            }
            if(clientSocket!=null) {
            	this.threadPool.execute(
                new AuctionServerThread(clientSocket));
                clients.add(clientSocket);
            } 
        }
    }


   
    public synchronized void stop(){
    	
    	this.threadPool.shutdownNow();
        
        for(Socket s:clients) {
        	if(!s.isClosed()) {
        		try {
					s.close();
				} catch (IOException e) {
					Debug.printError(e.toString());
				}
        	}
        }
        
        try {
            this.serverSocket.close();
        } catch (IOException e) {
        	Debug.printError("Error closing client connection");
        }
        
		Debug.printInfo("ThreadPooledServer stopped.") ;
    }

    private void openServerSocket() throws IOException {
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
        	Debug.printError("Cannot open port " + this.port + "");
        }
    }
}