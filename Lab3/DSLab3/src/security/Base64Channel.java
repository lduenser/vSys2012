package security;

import org.bouncycastle.util.encoders.Base64;

public class Base64Channel extends Decorator{

	public Base64Channel(Channel decoratedChannel) {
		super(decoratedChannel);
		
	}

	public byte[] receive() {
        byte[] receivedStr= super.receive();
        if (receivedStr!=null) {
          byte[] encryptedMessage = Base64.decode(receivedStr);
          return encryptedMessage;
        } else {
            return null;
        }
    }

    public void send (byte[] sendBytes) {
       
        byte[] base64Message = Base64.encode(sendBytes);
        super.send(base64Message);
    }

}
