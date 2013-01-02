package lab3;

/**
 *
 * @author Ternek Marianne 0825379
 * Jänner 2012
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ternekma
 */
public class TCPChannel implements Channel {
    private Socket socket;
    private final BufferedReader inFromScheduler;
    private final DataOutputStream outToServer;

    TCPChannel(Socket socket) throws IOException  {
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
                 Logger.getLogger(ManagementComponent.class.getName()).log(Level.SEVERE, null, ex);
            }
            outToServer.writeBytes(s+"\n");
        } catch (IOException ex) {
            Logger.getLogger(TCPChannel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public byte[] receive()   {
        try {
           // BufferedReader inFromScheduler = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String s = null;
            while ((s==null) && ((s = inFromScheduler.readLine()) == null)) {
            }
            return s.getBytes();
        } catch (Exception ex) {
            //Scheduler is down!
            // Logger.getLogger(TCPChannel.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

}
