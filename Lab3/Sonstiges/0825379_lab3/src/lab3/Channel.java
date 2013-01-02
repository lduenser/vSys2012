package lab3;

/**
 *
 * @author Ternek Marianne 0825379
 * Jänner 2012
 */
public interface Channel {
   public void send(byte[] sendStr);
   public byte[] receive();
}
