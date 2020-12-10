package ups.papersoda.netter.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Packet {
    private long id;
    private long sourceId;
    private long destId;
    private final List<Long> path = new ArrayList<>();
    private byte[] data;


    public Packet(long id, long sourceId, long destId) {
        this.sourceId = sourceId;
        this.destId = destId;
        this.id = id;
    }


    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getSourceId() {
        return sourceId;
    }
    public void setSourceId(long sourceId) {
        this.sourceId = sourceId;
    }
    public long getDestId() {
        return destId;
    }
    public void setDestId(long destId) {
        this.destId = destId;
    }
    public List<Long> getPath() {
        return path;
    }
    public void addToPath(long router) {
        path.add(router);
    }


    @Override
    public String toString() {
        return "Packet{" +
                "id=" + id +
                ", sourceId=" + sourceId +
                ", destId=" + destId +
                ", path=" + path +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
