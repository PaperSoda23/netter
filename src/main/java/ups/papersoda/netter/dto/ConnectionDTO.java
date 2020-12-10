package ups.papersoda.netter.dto;

public class ConnectionDTO {
    private final long id;
    private Number weight;
    private long fromRouter;
    private long toRouter;


    public ConnectionDTO(long id, Number weight, long fromRouter, long toRouter) {
        this.id = id;
        this.weight = weight;
        this.fromRouter = fromRouter;
        this.toRouter = toRouter;
    }


    public long getId() {
        return id;
    }
    public Number getWeight() {
        return weight;
    }
    public void setWeight(int weight) {
        this.weight = weight;
    }
    public long getFromRouter() {
        return fromRouter;
    }
    public void setFromRouter(long from) {
        this.fromRouter = from;
    }
    public long getToRouter() {
        return toRouter;
    }
    public void setToRouter(long to) {
        this.toRouter = to;
    }


    @Override
    public String toString() {
        return "connection=id" + id + "from" + fromRouter + "to" + toRouter + "weight" + weight;
    }
}
