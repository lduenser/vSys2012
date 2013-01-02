package security;

abstract public class Decorator implements Channel{
    protected final Channel decoratedChannel;

    public Decorator(Channel decoratedChannel) {
        this.decoratedChannel = decoratedChannel;
    }
    public byte[] receive() {
        return decoratedChannel.receive();
    }

    public void  send (byte[] sendStr) {
        decoratedChannel.send(sendStr);
    }

}
