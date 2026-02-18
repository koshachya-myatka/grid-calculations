import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PolyLine implements Lengthable, Cloneable{
    private List<Point2D> points;

    public PolyLine(List<Point2D> points) {
        if(points.size()<2) throw new IllegalArgumentException();
        this.points = new ArrayList<>(points);
    }

    public PolyLine(Point2D... points) {
        this(new ArrayList<>(List.of(points)));
    }

    public PolyLine(PolyLine original) {
        this(original.getPoints());
    }

    public List<Point2D> getPoints() {
        return new ArrayList<>(points);
    }

    public void setPoints(List<Point2D> points) {
        if(points.size()<2) throw new IllegalArgumentException();
        this.points = new ArrayList<>(points);
    }

    public void addPoint(Point2D point) {
        this.points.add(point);
    }

    @Override
    public double length() {
        return 0;
    }

    public List<Line> toLines(){
        return null;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PolyLine polyLine)) return false;
        List<Line> thisLines= toLines();
        List<Line> thatLines= polyLine.toLines();
        if(thisLines.size()!=thatLines.size()) return false;
        return thisLines.containsAll(thatLines);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(toLines());
    }

    @Override
    public PolyLine clone() {
        try {
            PolyLine pl = (PolyLine) super.clone();
            pl.points = new ArrayList<>(this.points);
            for (Point2D p : pl.points){
                p = p.clone();
            }
            return pl;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "PolyLine{" +
                "points=" + points +
                '}';
    }
}
