package ups.papersoda.netter.domain;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class RoutingTable {
    public static final int NO_CONNECTION = -1;
    /**
     * Map<RouterIDs, Map<AssociateRouterIDs, Pair<NextHopRouterId, CurrentShortestDistance>>>
     */
    private Map<Long, Map<Long, Pair<Long, Number>>> routingTable;


    private RoutingTable(Map<Long, Map<Long, Pair<Long, Number>>> routingTable) {
        this.routingTable = routingTable;
    }


    public static RoutingTable createRoutingTable(final Collection<? extends Router> routers) {
        if (routers.isEmpty())
            throw new IllegalArgumentException("routing table: no routers provided");

        Map<Long, Map<Long, Pair<Long, Number>>> routingTable = new HashMap<>();

        routers.forEach(currentRouter -> routingTable.putIfAbsent(
                    currentRouter.id(),
                    routers
                        .stream()
                        .filter(everyRouter -> currentRouter.id() != everyRouter.id())
                        .collect(Collectors.toMap(
                                        Router::id,
                                        (someOtherRouter) -> Pair.of(
                                                someOtherRouter.id(),
                                                currentRouter.getDistanceToPossibleNeighbour(someOtherRouter)
                                        )
                        )
        )));

        return new RoutingTable(routingTable);
    }

    public boolean tryUpdateTable(final long currentRouter) {
        AtomicBoolean tableWasUpdated = new AtomicBoolean(false);

        Set<Long> neighbours = this
                .getNeighbourRoutes(currentRouter);


        Map<Long, Pair<Long, Number>> currentRouterRoutes = this
                .getRouterRoutes(currentRouter);

        neighbours.forEach(neighbour -> {
            Number costToNeighbour = this
                    .getRouterDistance(currentRouter, neighbour);

            currentRouterRoutes
                    .forEach((associateRouterId, routerPair) -> {
                        if (associateRouterId.equals(neighbour))
                            return;

                        final Number currentShortestPath = routerPair.getValue().intValue();
                        final Number possibleShortestPath = this
                                .getRouterDistance(associateRouterId, neighbour).intValue() + costToNeighbour.intValue();

                        if (
                                RoutingTable.NO_CONNECTION == currentShortestPath.intValue() ||
                                currentShortestPath.intValue() <= possibleShortestPath.intValue()
                        ) {
                            return;
                        }

                        tableWasUpdated
                                .set(true);

                        this
                                .setNewShortestPath(
                                        currentRouter,
                                        neighbour,
                                        associateRouterId,
                                        possibleShortestPath
                                );
                    });
        });

        return tableWasUpdated.get();
    }

    public Long getNextShortestPathRouter(final long from, final long to) {
        return routingTable.get(from).get(to).getKey();
    }

    public Number getCurrentShortestPathDistance(final long from, final long to) {
        return routingTable.get(from).get(to).getValue();
    }

    public Long getNextHop(final long currentRouter, final Packet packet) {
        return routingTable
                .get(currentRouter)
                .get(packet.getDestId())
                .getKey();
    }

    public Map<Long, Pair<Long, Number>> getRouterRoutes(final long router) {
        return routingTable.get(router);
    }

    public Number getRouterDistance(final long fromRouter, final long toRouter) {
        return routingTable.get(fromRouter).get(toRouter).getValue();
    }

    public void setNewShortestPath(long currentRouter, long neighbour, long associateRouterId, Number newShortestPath) {
        routingTable.get(currentRouter).replace(associateRouterId, Pair.of(neighbour, newShortestPath));
        routingTable.get(associateRouterId).replace(currentRouter, Pair.of(neighbour, newShortestPath));
    }

    public Set<Long> getNeighbourRoutes(long ofRouter) {
        return routingTable.get(ofRouter).keySet();
    }

    public Number getDistanceFromRouterToNeighbour(final long routerId, final long neighbourId) {
        return routingTable
                .get(routerId)
                .get(neighbourId)
                .getValue();
    }

    public Map<Long, Map<Long, Pair<Long, Number>>> getRoutingTable() {
        return routingTable;
    }

    public void removeRouter(final long routerToRemove) {
        routingTable
                .remove(routerToRemove);
        routingTable
                .forEach((currentRouter, routerBondMap) -> routerBondMap
                        .replace(routerToRemove, Pair.
                                of(routerToRemove, NO_CONNECTION)));
    }

    public void updateRouterBond(final long router, final long neighbour, final int newDistance) {
        routingTable
                .get(router)
                .replace(neighbour, Pair
                        .of(neighbour, newDistance));
        routingTable
                .get(neighbour)
                .replace(router, Pair
                        .of(router, newDistance));
    }

    public Set<Long> getRouterIds() {
        return routingTable.keySet();
    }
    public Set<Long> getAssociateRouterMap(long routerId) {
        return routingTable.getOrDefault(routerId, new HashMap<>()).keySet();
    }

    public boolean hasRouter(final long router) {
        return routingTable.containsKey(router);
    }

    @Override
    public String toString() {
        return "RoutingTable{" +
                "routingTable=" + routingTable +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoutingTable that = (RoutingTable) o;
        return Objects.equals(routingTable, that.routingTable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routingTable);
    }
}
