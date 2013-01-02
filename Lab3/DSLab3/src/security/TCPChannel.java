package security;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

public class TCPChannel implements Channel {

	private Socket socket;
    private final BufferedReader inFromScheduler;
    private final DataOutputStream outToServer;
    
    public TCPChannel(Socket socket) throws IOException  {
        this.socket = socket;
        this.inFromScheduler = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.outToServer = new DataOutputStream(socket.getOutputStream());
    }
    
    public void send(byte[] sendBytes) {
        try {
            String s="";
            try {
                 s = new String(sendBytes, "UTF8");
            } catch (UnsupportedEncodingException ex) {
                 System.out.println("encoding failure");
            }
            outToServer.writeBytes(s+"\n");
        } catch (IOException ex) {
        	System.out.println("io exc");
        }

    }

    public byte[] receive()   {
        try {
           // BufferedReader inFromScheduler = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String s = null;
            while ((s==null) && ((s = inFromScheduler.readLine()) == null)) {
            }
            return s.getBytes();
        } catch (Exception e) {            
        	System.out.println("Scheduler is down!");
        }

        return null;
    }

}
