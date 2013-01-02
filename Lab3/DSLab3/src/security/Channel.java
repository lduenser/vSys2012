package security;

public interface Channel {
	
	   public void send(byte[] string);
	   
	   public byte[] receive();
}
