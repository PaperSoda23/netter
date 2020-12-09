package ups.papersoda.netter.domain;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ups.papersoda.netter.domain.mapper.RouterMapper;
import ups.papersoda.netter.dto.ConnectionDTO;
import ups.papersoda.netter.dto.RouterDTO;

import java.util.*;

import static org.assertj.core.api.Assertions.*;


public class RouterMapperTests {
    private RouterMapper routerMapper;

    @BeforeEach
    public void init() {
        this.routerMapper = new RouterMapper();
    }

    @Test
    public void creates_routers_from_router_dtos() {
        List<RouterDTO> routerDTOList = List.of(
                new RouterDTO(1L, new ArrayList<>()),
                new RouterDTO(2L, new ArrayList<>())
        );

        Map<Long, Router> routers = routerMapper.createRouters.apply(routerDTOList);
        Map<Long, Router> expected = Map.of(1L, new Router(1L, Map.of()), 2L, new Router(2L, Map.of()));

        assertThat(routers)
                .hasSameSizeAs(expected)
                .containsExactlyInAnyOrderEntriesOf(expected);
    }

    @Test
    public void gets_associated_router_dto() {
        var routerDTOs = List.of(
                new RouterDTO(1L, new ArrayList<>()),
                new RouterDTO(2L, new ArrayList<>())
        );

        var router = new Router(1L, new HashMap<>());

        RouterDTO expectedDTO = routerMapper.getAssociateRouterDTO.apply(router, routerDTOs);

        assertThat(expectedDTO)
                .isSameAs(routerDTOs.get(0))
                .isNotSameAs(routerDTOs.get(1));
    }

    @Test
    public void gets_router_neighbour_ids() {
        var connections = List.of(
            new ConnectionDTO(1L, 3, 1L, 2L),
            new ConnectionDTO(2L, 5, 3L, 5L)
        );
        var routerDTO = new RouterDTO(1L, connections);

        Set<Long> neighbourIds = routerMapper.getNeighboursIds(routerDTO);

        assertThat(neighbourIds)
                .isEqualTo(Set.of(2L, 5L))
                .isNotEqualTo(Set.of(2L))
                .isNotEqualTo(Set.of(2L, 3L))
                .isNotEqualTo(Set.of(2L, 5L, 9L));
    }

    @Nested
    class createConnection {
        @Test
        public void creates_connection() {
            var connections = List.of(
                    new ConnectionDTO(1L, 5, 1L,2L),
                    new ConnectionDTO(2L, 9, 1L, 3L)
            );
            var routerDTO = new RouterDTO(1L, connections);

            var connection_1 = routerMapper.createConnection(routerDTO, 3L);
            var connection_2 = routerMapper.createConnection(routerDTO, 2L);

            assertThat(connection_1.getWeight())
                    .isEqualTo(9);
            assertThat(connection_1.getToRouter())
                    .isEqualTo(3L);
            assertThat(connection_1.getFromRouter())
                    .isEqualTo(1L);

            assertThat(connection_2.getWeight()).
                    isEqualTo(5);
            assertThat(connection_2.getToRouter())
                    .isEqualTo(2L);
            assertThat(connection_2.getFromRouter())
                    .isEqualTo(1L);
        }

        @Test
        public void create_connection_errors_when_requested_connection_not_exists() {
            var routerDTO = new RouterDTO(1L, new ArrayList<>());

            assertThatThrownBy(() -> routerMapper.createConnection(routerDTO, 3L))
                    .isExactlyInstanceOf(NullPointerException.class)
                    .hasMessageContaining("create connection: connection can't be created");
        }
    }

    @Test
    public void gets_neighbours() {
        Map<Long, Router> routers = Map.of(
                1L, new Router(1L, Map.of()),
                2L, new Router(2L, Map.of()),
                3L, new Router(3L, Map.of())
        );
        var neighbourIds = Set.of(2L, 3L);
        var routerDTO = new RouterDTO(1L, List.of(
                new ConnectionDTO(1L, 3, 1L, 2L),
                new ConnectionDTO(2L, 5, 1L, 3L)
        ));

        Map<Long, Pair<Router, Connection>> neighbours = routerMapper
                .getNeighbours(routerDTO, routers.values(), neighbourIds);

        assertThat(neighbours)
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        2L, Pair.of(new Router(2L, Map.of()), new Connection(3, 2L, 1L)),
                        3L, Pair.of(new Router(3L, Map.of()), new Connection(5, 3L, 1L))
                ));
    }

    @Test
    public void assigns_neighbours() {
        Map<Long, Router> routers = Map.of(
                1L, new Router(1L, Map.of()),
                2L, new Router(2L, Map.of()),
                3L, new Router(3L, Map.of())
        );
        var routerDTOs = List.of(
                new RouterDTO(1L, List.of(
                    new ConnectionDTO(1L, 3, 1L, 2L),
                    new ConnectionDTO(2L, 5, 1L, 3L))
                ),
                new RouterDTO(2L, new ArrayList<>()),
                new RouterDTO(3L, new ArrayList<>())
        );

        routerMapper.assignNeighbours.accept(routers, routerDTOs);

        assertThat(routers.get(1L)
                .getNeighbour(2L).getKey())
                    .isEqualTo(routers.get(2L));
        assertThat(routers.get(1L)
                .getNeighbour(2L).getValue())
                    .isEqualTo(new Connection(3, 2L, 1L));

        assertThat(routers.get(1L)
                .getNeighbour(3L).getKey())
                    .isEqualTo(routers.get(3L));
        assertThat(routers.get(1L)
                .getNeighbour(3L).getValue())
                    .isEqualTo(new Connection(5, 3L, 1L));
    }

    @Test
    public void transforms_to_routers() {
        var routerDTOs = List.of(
                new RouterDTO(1L, List.of(
                        new ConnectionDTO(1L, 3, 1L, 2L),
                        new ConnectionDTO(2L, 5, 1L, 3L))
                ),
                new RouterDTO(2L, new ArrayList<>()),
                new RouterDTO(3L, new ArrayList<>())
        );

        Map<Long, Router> routers = routerMapper.transformToRouters(routerDTOs);

        assertThat(routers.keySet())
                .hasSize(routerDTOs.size());

        assertThat(routers.get(1L)
                .neighbourCount())
                    .isEqualTo(2);

        assertThat(routers.get(2L)
                .neighbourCount())
                    .isEqualTo(0);

        assertThat(routers.get(1L)
                .getNeighbour(2L).getKey())
                    .isEqualTo(new Router(2L, Map.of()));

        assertThat(routers.get(1L)
                    .getNeighbour(3L).getValue())
                        .isEqualTo(new Connection(5, 3L, 1L));

        assertThat(routers.get(2L)
                .getNeighbour(3L))
                    .isNull();
    }
}
