package busantravel.util;

public class RoutePoint {
    private final String id;
    private final String name;
    private final Coordinate coordinate;

    public RoutePoint(String id, String name, Coordinate coordinate) {
        this.id = id;
        this.name = name;
        this.coordinate = coordinate;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }
}
