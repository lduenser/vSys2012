package security;

import java.security.Key;


public class Cipher extends Decorator{

	 public Cipher(Channel decoratedChannel) {
		super(decoratedChannel);
	}

	private Key key = null;
	private String algorithm = "";
	byte[] iv=null;
	    

	    public void setKey(Key key) {
	        this.key=key;
	    }

	    public void setalgorithm(String algorithm) {
	        this.algorithm=algorithm;
	    }

	    public void setInitVector(byte[] iv) {
	        this.iv=iv;
	    }

	    @Override
	    public byte[] receive() {
			return null;
	    	
	    }
	    
	    @Override
	    public void send (byte[] sendBytes) {
	    	
	    }
}
