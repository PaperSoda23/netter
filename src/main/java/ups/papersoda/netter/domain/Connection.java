package ups.papersoda.netter.domain;

public class Connection {
    private long id;
    private int weight;
    private final long toRouter;
    private final long fromRouter;


    public Connection(int weight, long toRouter, long fromRouter) {
        this.weight = weight;
        this.toRouter = toRouter;
        this.fromRouter = fromRouter;
    }


    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public int getWeight() {
        return weight;
    }
    public void setWeight(int weight) {
        this.weight = weight;
    }
    public long getToRouter() {
        return toRouter;
    }
    public long getFromRouter() {
        return fromRouter;
    }


    @Override
    public String toString() {
        return "Connection{" +
                "id=" + id +
                ", weight=" + weight +
                ", toRouter=" + toRouter +
                ", fromRouter=" + fromRouter +
                '}';
    }
    @Override
    public int hashCode() {
        return (int)id;
    }
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Connection))
            return false;

        Connection conn = (Connection)obj;

        return (
                conn.toRouter == toRouter &&
                conn.fromRouter == fromRouter &&
                conn.weight == weight
        );
    }
}

