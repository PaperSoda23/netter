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
    private final Map<Long, Map<Long, Pair<Long, Number>>> routingTable;


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
            final Number costToNeighbour = this
                    .getRouterDistance(currentRouter, neighbour);

            currentRouterRoutes
                    .forEach((associateRouterId, routerPair) -> {
                        final Number currentShortestPath = routerPair.getValue().intValue();
                        final Number possibleShortestPath = this
                                .getRouterDistance(neighbour, associateRouterId).intValue() + costToNeighbour.intValue();

                        if (associateRouterId.equals(neighbour))
                            return;

                        if (currentShortestPath.intValue() < possibleShortestPath.intValue())
                            return;

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

    public Long getNextHop(final long currentRouter, final Packet packet) {
        return routingTable
                .get(currentRouter)
                .get(packet.getDestId())
                .getKey();
    }

    public Map<Long, Pair<Long, Number>> getRouterRoutes(final long router) {
        return routingTable.get(router);
    }

    public Number getRouterDistance(final long routerId, final long associateRouter) {
        return routingTable.get(routerId).get(associateRouter).getValue();
    }

    public void setNewShortestPath(long currentRouter, long neighbour, long associateRouterId, Number newShortestPath) {
        routingTable.get(currentRouter).replace(neighbour, Pair.of(associateRouterId, newShortestPath));
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
}
