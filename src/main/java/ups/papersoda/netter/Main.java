package ups.papersoda.netter;

import ups.papersoda.netter.domain.Network;
import ups.papersoda.netter.domain.Packet;
import ups.papersoda.netter.domain.mapper.RouterMapper;
import ups.papersoda.netter.dto.ConnectionDTO;
import ups.papersoda.netter.dto.RouterDTO;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<RouterDTO> routers = new ArrayList<>(){{
            add(new RouterDTO(1L, new ArrayList<>() {{
                add(new ConnectionDTO(1L, 7, 1L, 2L));
            }}));
            add(new RouterDTO(2L, new ArrayList<>() {{
                add(new ConnectionDTO(2L, 5, 2L, 3L));
            }}));
            add(new RouterDTO(3L, new ArrayList<>()));
        }};

        Network network = new Network(new RouterMapper(), routers);

        List<Packet> packets = new ArrayList<>(){{
            add(new Packet(1L, 1L, 3L));
        }};

        network.beingPacketTransmission(packets);
        System.out.println(packets.get(0));
    }
}
