package ups.papersoda.netter.domain;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ups.papersoda.netter.domain.mapper.RouterMapper;
import ups.papersoda.netter.dto.ConnectionDTO;
import ups.papersoda.netter.dto.RouterDTO;

import static org.assertj.core.api.Assertions.*;


import java.util.*;

public class RoutingTableTests {
    @Nested
    class Initialization {
        @Test
        public void has_ids_of_routers_as_keys() {
            List<Router> routers = Arrays.asList(
                    new Router(1L, Map.of()),
                    new Router(2L, Map.of())
            );

//            routers.get(0)
//                    .setNeighbours(Map.of(2L,
//                            Pair.of(
//                                    routers.get(1),
//                                    new Connection(5, 2L, 1L))
//                    ));
            RoutingTable routingTable = RoutingTable.createRoutingTable(routers);

            assertThat(routingTable.getRouterIds()).containsExactlyInAnyOrder(1L, 2L);
        }

        @Test
        public void has_only_other_routers_as_viable_paths_but_not_self() {
            List<Router> routers = List.of(
                    new Router(1L, Map.of()),
                    new Router(2L, Map.of()),
                    new Router(3L, Map.of())
            );

            RoutingTable routingTable = RoutingTable.createRoutingTable(routers);

            assertThat(routingTable.getAssociateRouterMap(1L))
                    .containsExactlyInAnyOrder(2L, 3L);
            assertThat(routingTable.getAssociateRouterMap(2L))
                    .doesNotContain(2L);
            assertThat(routingTable.getAssociateRouterMap(3L))
                    .hasSize(routers.size() - 1);
        }

        @Test
        public void router_only_knows_distance_to_other_router_only_if_other_router_is_neighbour() {
            var routers = new HashMap<Long, Router>() {{
                put(1L, new Router(1L, new HashMap<>()));
                put(2L, new Router(2L, new HashMap<>()));
                put(3L, new Router(3L, new HashMap<>()));
            }};

            routers.get(1L)
                    .addNeighbour(2L, routers.get(2L),
                            new Connection(5, 2L, 1L));
            routers.get(3L)
                    .addNeighbour(1L, routers.get(1L),
                            new Connection(2, 2L, 3L));

            var routingTable = RoutingTable.createRoutingTable(routers.values());

            assertThat(routingTable.getDistanceFromRouterToNeighbour(1L, 2L))
                    .isEqualTo(5);
            assertThat(routingTable.getDistanceFromRouterToNeighbour(3L, 1L))
                    .isEqualTo(2);
            assertThat(routingTable.getDistanceFromRouterToNeighbour(2L, 3L))
                    .isEqualTo(RoutingTable.NO_CONNECTION);
        }
    }

    @Nested
    class Integration {
        @Test
        public void sets_new_shortest_path_mutually() {
            List<RouterDTO> routerDTOs = new ArrayList<>(){{
                add(new RouterDTO(1L, new ArrayList<>() {{ add(new ConnectionDTO(1L, 7, 1L, 2L)); }}));
                add(new RouterDTO(2L, new ArrayList<>() {{ add(new ConnectionDTO(2L, 5, 2L, 3L)); }}));
                add(new RouterDTO(3L, new ArrayList<>()));
            }};
            var routers = new RouterMapper().transformToRouters(routerDTOs);
            var routingTable = RoutingTable.createRoutingTable(routers.values());

            routingTable.setNewShortestPath(1L, 2L, 3L, 12);

            assertThat(routingTable.getCurrentShortestPathDistance(1, 3).intValue()).isEqualTo(12);
            assertThat(routingTable.getNextShortestPathRouter(1, 3).longValue()).isEqualTo(2L);
            assertThat(routingTable.getCurrentShortestPathDistance(3, 1).intValue()).isEqualTo(12);
            assertThat(routingTable.getNextShortestPathRouter(3, 1).longValue()).isEqualTo(2L);
        }

        @Test
        public void gets_next_router_hop() {
            List<RouterDTO> routerDTOs = new ArrayList<>(){{
                add(new RouterDTO(1L, new ArrayList<>() {{ add(new ConnectionDTO(1L, 7, 1L, 2L)); }}));
                add(new RouterDTO(2L, new ArrayList<>() {{ add(new ConnectionDTO(2L, 5, 2L, 3L)); }}));
                add(new RouterDTO(3L, new ArrayList<>()));
            }};
            var packet = new Packet(1, 1L, 3L);

            var routers = new RouterMapper().transformToRouters(routerDTOs);
            var routingTable = RoutingTable.createRoutingTable(routers.values());
            System.out.println(routingTable);

            System.out.println(routingTable);
            var nextHop = routingTable.getNextHop(routers.get(1L), packet);

            assertThat(nextHop).isEqualTo(2L);
        }
    }

    @Nested
    class Behavior {
        @Test
        public void neighbour_bonds_are_mutual() {
            var routers = new HashMap<Long, Router>() {{
                put(1L, new Router(1L, new HashMap<>()));
                put(2L, new Router(2L, new HashMap<>()));
            }};
            routers.get(1L).addNeighbour(2L, routers.get(2L), new Connection(5, 2L, 1L));

            var routingTable = RoutingTable.createRoutingTable(new ArrayList<>(routers.values()));
            System.out.println(routingTable);
            assertThat(routingTable.getDistanceFromRouterToNeighbour(1L, 2L))
                    .isEqualTo(5);
            assertThat(routingTable.getDistanceFromRouterToNeighbour(2L, 1L))
                    .isEqualTo(5);
        }

        @Test
        public void neighbour_bonds_are_mutually_updated() {
            var routers = new HashMap<Long, Router>() {{
                put(1L, new Router(1L, new HashMap<>()));
                put(2L, new Router(2L, new HashMap<>()));
            }};
            routers.get(1L)
                    .addNeighbour(2L, routers.get(2L), new Connection(5, 1L, 2L));

            var routingTable = RoutingTable.createRoutingTable(new ArrayList<>(routers.values()));
            assertThat(routingTable.getDistanceFromRouterToNeighbour(1L, 2L))
                    .isEqualTo(5);
            assertThat(routingTable.getDistanceFromRouterToNeighbour(2L, 1L))
                    .isEqualTo(5);

            routingTable.updateRouterBond(1L, 2L, 7);

            assertThat(routingTable.getDistanceFromRouterToNeighbour(1L, 2L))
                    .isEqualTo(7);
            assertThat(routingTable.getDistanceFromRouterToNeighbour(2L, 1L))
                    .isEqualTo(7);
        }

        @Test
        public void removes_router_from_routing_table() {
            var routers = new HashMap<Long, Router>() {{
                put(1L, new Router(1L, new HashMap<>()));
                put(2L, new Router(2L, new HashMap<>()));
            }};
            routers.get(1L).addNeighbour(2L, routers.get(2L), new Connection(5, 2L, 1L));

            var routingTable = RoutingTable.createRoutingTable(new ArrayList<>(routers.values()));

            assertThat(routingTable.getDistanceFromRouterToNeighbour(1L, 2L))
                    .isEqualTo(5);

            routingTable.removeRouter(2L);

            assertThat(routingTable.hasRouter(2L))
                    .isFalse();
            assertThat(routingTable.getDistanceFromRouterToNeighbour(1L, 2L))
                    .isEqualTo(RoutingTable.NO_CONNECTION);
        }

        @Test
        public void tryUpdateTable_returns_true_when_table_should_be_updated() {
            var routers = List.of(
                    new Router(1L, new HashMap<>()),
                    new Router(2L, new HashMap<>()),
                    new Router(3L, new HashMap<>())
            );
            var connections_1L = new HashMap<Long, Pair<Router, Connection>>(){{
                put(3L, Pair.of(routers.get(2), new Connection(10, 3L, 1L)));
                put(2L, Pair.of(routers.get(1), new Connection(2, 2L, 1L)));
            }};
            var connections_2L = new HashMap<Long, Pair<Router, Connection>>(){{
                put(3L, Pair.of(routers.get(2), new Connection(3, 3L, 2L)));
            }};

            routers.get(0).setNeighbours(connections_1L);
            routers.get(1).setNeighbours(connections_2L);

            var routingTable = RoutingTable.createRoutingTable(routers);


            boolean shouldUpdate = routingTable.tryUpdateTable(routers.get(0));

            assertThat(shouldUpdate).isTrue();
        }

        @Test
        public void tryUpdateTable_performs_mutual_update_when_update_should_be_performed() {
            var routers = List.of(
                    new Router(1L, new HashMap<>()),
                    new Router(2L, new HashMap<>()),
                    new Router(3L, new HashMap<>())
            );
            var connections_1L = new HashMap<Long, Pair<Router, Connection>>(){{
                put(3L, Pair.of(routers.get(2), new Connection(10, 3L, 1L)));
                put(2L, Pair.of(routers.get(1), new Connection(2, 2L, 1L)));
            }};
            var connections_2L = new HashMap<Long, Pair<Router, Connection>>(){{
                put(3L, Pair.of(routers.get(2), new Connection(3, 3L, 2L)));
            }};

            routers.get(0).setNeighbours(connections_1L);
            routers.get(1).setNeighbours(connections_2L);

            var routingTable = RoutingTable.createRoutingTable(routers);


            boolean shouldUpdate = routingTable.tryUpdateTable(routers.get(0));

            assertThat(shouldUpdate).isTrue();
            assertThat(routingTable.getNextShortestPathRouter(1L, 3L)).isEqualTo(2L);
            assertThat(routingTable.getCurrentShortestPathDistance(1L, 3L)).isEqualTo(5);
        }

        @Test
        public void tryUpdateTable_does_not_change_routing_table_when_update_should_not_be_performed() {
            var routers = List.of(
                    new Router(1L, new HashMap<>()),
                    new Router(2L, new HashMap<>()),
                    new Router(3L, new HashMap<>())
            );
            var connections_1L = new HashMap<Long, Pair<Router, Connection>>(){{
                put(3L, Pair.of(routers.get(2), new Connection(1, 3L, 1L)));
                put(2L, Pair.of(routers.get(1), new Connection(2, 2L, 1L)));
            }};
            var connections_2L = new HashMap<Long, Pair<Router, Connection>>(){{
                put(3L, Pair.of(routers.get(2), new Connection(3, 3L, 2L)));
            }};

            routers.get(0).setNeighbours(connections_1L);
            routers.get(1).setNeighbours(connections_2L);

            var routingTable = RoutingTable.createRoutingTable(routers);


            boolean shouldUpdate = routingTable.tryUpdateTable(routers.get(0));


            assertThat(shouldUpdate)
                    .isFalse();
            assertThat(routingTable.getRoutingTable())
                    .isEqualTo(Map.of(
                    1L, Map.of(
                                2L, Pair.of(2L, 2),
                                3L, Pair.of(3L, 1)
                        ),
                    2L, Map.of(
                            1L, Pair.of(1L, 2),
                            3L, Pair.of(3L, 3)
                        ),
                    3L, Map.of(
                                1L, Pair.of(1L, 1),
                                2L, Pair.of(2L, 3)
                        )
                    ));
        }
    }

    @Nested
    class Errors {
        @Test
        public void errors_when_created_with_no_routers() {
            assertThatThrownBy(() -> RoutingTable.createRoutingTable(new ArrayList<>()))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("routing table: no routers provided");
        }
    }
}
