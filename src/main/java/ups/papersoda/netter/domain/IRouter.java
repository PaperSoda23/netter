package ups.papersoda.netter.domain;

public interface IRouter {
    void receivePacket(final Packet packet);
    void sendPacket(final long neighbour, final Packet packet);
}
