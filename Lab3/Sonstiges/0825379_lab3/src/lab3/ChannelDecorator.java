package lab3;

/**
 *
 * @author Ternek Marianne 0825379
 * Jänner 2012
 */
abstract public class ChannelDecorator implements Channel{
    protected final Channel decoratedChannel;

    public ChannelDecorator(Channel decoratedChannel) {
        this.decoratedChannel = decoratedChannel;
    }
    public byte[] receive() {
        return decoratedChannel.receive();
    }

    public void  send (byte[] sendStr) {
        decoratedChannel.send(sendStr);
    }

}
