package ups.papersoda.netter.domain;

import ups.papersoda.netter.domain.mapper.RouterMapperInt;
import ups.papersoda.netter.dto.RouterDTO;

import java.util.List;
import java.util.Map;

public class Network {
    private final Map<Long, Router> routers;
    private final RoutingTable routingTable;


    public Network(RouterMapperInt routerMapper, List<? extends RouterDTO> routerDTOS) {
        routers = routerMapper.transformToRouters(routerDTOS);
        routingTable = RoutingTable.createRoutingTable(routers.values());
        routers.forEach((routerId, router) -> router.setRoutingTable(routingTable));
    }


    public void beingPacketTransmission(final List<? extends Packet> packets) {
        packets.forEach(this::transmitPacket);
    }

    private void transmitPacket(final Packet packet) {
        routers.get(packet.getSourceId()).receivePacket(packet);
    }
}

