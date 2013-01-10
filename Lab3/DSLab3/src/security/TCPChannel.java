package security;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;

import debug.Debug;

public class TCPChannel implements Channel {

	private Socket socket;
    private final BufferedReader in;
    private final DataOutputStream out;
    
    private boolean error = false;
    
    public TCPChannel(Socket socket) throws IOException  {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new DataOutputStream(socket.getOutputStream());
    }
    
    public void send(byte[] sendBytes) {
        try {
            String s="";
            s = new String(sendBytes);
            out.writeBytes(s+"\r\n");
            out.flush();
            
        } catch (IOException io) {
        	error = true;
        }

    }

    public byte[] receive()   {
        try {
           if(in.ready()){
        	   String s = null;
               
        	   while ((s = in.readLine()) != null) {
        		    return s.getBytes();
        		}
           }
        	
            
        } catch (Exception e) {            
        	e.printStackTrace();        	
        }

        return null;
    }

	@Override
	public boolean getError() {
		// TODO Auto-generated method stub
		return error;
	}
}
