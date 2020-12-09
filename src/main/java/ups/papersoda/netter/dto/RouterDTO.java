package ups.papersoda.netter.dto;

import java.util.List;

public class RouterDTO {
    private final long id;
    private final List<ConnectionDTO> connections;


    public RouterDTO(long id, List<ConnectionDTO> connections) {
        this.id = id;
        this.connections = connections;
    }


    public long id() {
        return id;
    }
    public List<ConnectionDTO> getConnections() { return this.connections; }

    @Override
    public String toString() {
        return "RouterDTO{" +
                "id=" + id +
                ", connections=" + connections +
                '}';
    }
}
