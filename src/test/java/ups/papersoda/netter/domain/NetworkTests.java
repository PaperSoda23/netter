package ups.papersoda.netter.domain;

import org.junit.jupiter.api.Test;
import ups.papersoda.netter.domain.mapper.RouterMapper;
import ups.papersoda.netter.dto.ConnectionDTO;
import ups.papersoda.netter.dto.RouterDTO;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NetworkTests {
    @Test
    public void transmits_packet() {
        // [[1]]--7--[[2]]--5--[[3]]
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


        assertThat(packets.get(0).getPath()).isEqualTo(List.of(1L, 2L, 3L));
    }
}
