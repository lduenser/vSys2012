package security;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class TCPChannel  implements Channel {

	private Socket socket;
    private final BufferedReader inFromScheduler;
    private final DataOutputStream outToServer;
    
    TCPChannel(Socket socket) throws IOException  {
        this.socket = socket;
        this.inFromScheduler = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.outToServer = new DataOutputStream(socket.getOutputStream());
    }
    
	@Override
	public void send(byte[] string) {
		
		
	}

	@Override
	public byte[] receive() {
		
		return null;
	}

}
