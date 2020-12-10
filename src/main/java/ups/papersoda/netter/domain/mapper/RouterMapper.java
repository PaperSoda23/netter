package ups.papersoda.netter.domain.mapper;

import org.apache.commons.lang3.tuple.Pair;
import ups.papersoda.netter.domain.Connection;
import ups.papersoda.netter.domain.Router;
import ups.papersoda.netter.dto.ConnectionDTO;
import ups.papersoda.netter.dto.RouterDTO;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class RouterMapper implements RouterMapperInt {
    public Map<Long, Router> transformToRouters(Collection<? extends RouterDTO> routerDTOS) {
        Map<Long, Router> routers = createRouters.apply(routerDTOS);
        assignNeighbours.accept(routers, routerDTOS);
        return routers;
    }

    public final Function<Collection<? extends RouterDTO>, Map<Long, Router>> createRouters = (routerDTOS ->
            routerDTOS.stream()
                    .collect(Collectors
                            .toMap(
                                    RouterDTO::id,
                                    (r) -> new Router(r.id(), Map.of())
                            )
                    )
    );

    public final BiFunction<Router, Collection<? extends RouterDTO>, RouterDTO> getAssociateRouterDTO = (router, routerDTOs) -> {
        Optional<? extends RouterDTO> routerDTO = routerDTOs.stream()
                .filter(rDTO -> rDTO.id() == router.id())
                .findFirst();

        return routerDTO.get();
    };

    public final BiConsumer<Map<Long, Router>, Collection<? extends RouterDTO>> assignNeighbours = (routers, routerDTOs) -> routers.values()
            .forEach(router -> {
                var associatedRouterDTO = getAssociateRouterDTO.apply(router, routerDTOs);
                var neighbourIds = getNeighboursIds(associatedRouterDTO);
                Map<Long, Pair<Router, Connection>> neighbours = getNeighbours(
                        associatedRouterDTO,
                        routers.values(),
                        neighbourIds
                );
                router.setNeighbours(neighbours);
            });

    public Set<Long> getNeighboursIds(final RouterDTO routerDTO) {
        return routerDTO
                .getConnections().stream()
                    .map(ConnectionDTO::getToRouter)
                        .collect(Collectors
                                .toSet()
                        );
    }

    public Map<Long, Pair<Router, Connection>> getNeighbours(
            final RouterDTO routerDTO,
            final Collection<Router> routers,
            final Set<Long> neighbourIds
    ) {
        return routers.stream()
                .filter(r -> neighbourIds.contains(r.id()))
                .collect(Collectors
                        .toMap(
                                Router::id,
                                (neighbour) -> Pair.of(
                                        neighbour,
                                        createConnection(routerDTO, neighbour.id())
                                )
                        )
                );
    }

    public Connection createConnection(final RouterDTO routerDTO, final long routerToConnect) {
        var connectionDTO = routerDTO
                .getConnections().stream()
                    .filter(connection -> connection.getToRouter() == routerToConnect)
                        .findFirst()
                        .orElse(null);

        if (connectionDTO == null)
            throw new NullPointerException("create connection: connection can't be created");

        return new Connection(connectionDTO.getWeight().intValue(), routerToConnect, connectionDTO.getFromRouter());
    }
}
