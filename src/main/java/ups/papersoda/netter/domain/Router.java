package ups.papersoda.netter.domain;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Router implements IRouter {
    private long id;
    private Map<Long, Pair<Router, Connection>> neighbours;
    private RoutingTable routingTable;


    public Router(long id, Map<Long, Pair<Router, Connection>> neighbours) {
        this.id = id;
        this.neighbours = neighbours;
    }


    private void shareTableUpdateWithNeighbours() {
        neighbours.values()
                .forEach((neighbour) -> routingTable
                        .tryUpdateTable(neighbour.getKey()));
    }

    public void addNeighbour(final long neighbourId, final Router router, final Connection connection) {
        if (!isNeighbour(router)) {
            neighbours.put(neighbourId, Pair.of(router, connection));
            return;
        }
        neighbours.replace(neighbourId, Pair.of(router, connection));
    }

    public int getDistanceToPossibleNeighbour(final Router router) {
        if (router.isNeighbour(this))
            return router.getDistanceToPossibleNeighbour(this);
        else if (!neighbours.containsKey(router.id()))
            return RoutingTable.NO_CONNECTION;
        else
            return neighbours.get(router.id()).getValue().getWeight();
    }

    public void sendPacket(final long neighbour, final Packet packet) {
        neighbours.get(neighbour).getKey().receivePacket(packet);
    }

    public void receivePacket(final Packet packet) {
        packet.addToPath(this.id);

        if (this.isDestinationRouter.test(packet)) {
            System.out.println("packet reached destination" + packet);
            return;
        }

        final boolean tableWasUpdated = routingTable.tryUpdateTable(this);
        if (tableWasUpdated)
            this.shareTableUpdateWithNeighbours();

        final var nextNeighborToSend = routingTable.getNextHop(this, packet);
        this.sendPacket(nextNeighborToSend, packet);

    }


    private final Predicate<Packet> isDestinationRouter = p -> id == p.getDestId();
    public Pair<Router, Connection> getNeighbour(long neighbour) {
        return neighbours.get(neighbour);
    }
    public void setNeighbours(Map<Long, Pair<Router, Connection>> neighbours) {
        this.neighbours = neighbours;
    }
    public void setRoutingTable(final RoutingTable routingTable) {
        this.routingTable = routingTable;
    }
    public int neighbourCount() { return neighbours.keySet().size(); }
    public boolean isNeighbour(final Router router) {
        return neighbours.containsKey(router.id);
    }
    public long id() { return id; }
    public Set<Long> getNeighbours() {
        return neighbours.values().stream()
                .map(neighbour -> neighbour.getKey().id)
                    .collect(Collectors.toSet());
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Router))
            return false;
        Router r = (Router)obj;
        return r.id == id;
    }
    @Override
    public int hashCode() {
        return (int)id;
    }
    @Override
    public String toString() {
        return "Router{" +
                "id=" + id +
                ", neighbour keys=" + neighbours.keySet() +
                '}';
    }
}
