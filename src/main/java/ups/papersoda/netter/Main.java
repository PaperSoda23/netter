package ups.papersoda.netter;

import org.apache.commons.lang3.tuple.Pair;
import ups.papersoda.netter.domain.Connection;
import ups.papersoda.netter.domain.Network;
import ups.papersoda.netter.domain.Packet;
import ups.papersoda.netter.domain.mapper.RouterMapper;
import ups.papersoda.netter.dto.ConnectionDTO;
import ups.papersoda.netter.dto.RouterDTO;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
//        List<RouterDTO> routers = new ArrayList<>(){{
//            new RouterDTO(1L, new ArrayList<>() {{
//                new ConnectionDTO(1L, 5, 2L);
//            }});
//            new RouterDTO(2L, new ArrayList<>() {{
//                new ConnectionDTO(2L, 3, 3L);
//            }});
//            new RouterDTO(3L, new ArrayList<>());
//        }};
        List<? extends RouterDTO> routers = new ArrayList<>();

        Network network = new Network(new RouterMapper(), routers);


        List<Packet> packets = new ArrayList<>(){{
            new Packet(1L, 1L, 3L);
        }};

        network.beingPacketTransmission(packets);
    }
}
