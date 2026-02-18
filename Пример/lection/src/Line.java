import java.util.Objects;

public class Line<T extends Point2D> implements Cloneable, Lengthable{
    private T start;
    private T end;

    public Line(T start, T end) {
        if (start.getClass()!=end.getClass()) throw new IllegalArgumentException();
        this.start = (T) start.clone();
        this.end = (T) end.clone();
    }

    public static Line<Point2D> of(int x1, int y1, int x2, int y2){
        Point2D st = new Point2D(x1,y1);
        Point2D end = new Point2D(x2,y2);
        return new Line<>(st, end);
    }

    public static Line<Point3D> of(int x1, int y1, int z1, int x2, int y2, int z2){
        Point3D st = new Point3D(x1,y1,z1);
        Point3D end = new Point3D(x2,y2,z2);
        return new Line<>(st, end);
    }


    public Line(Line<T> line){
        this(line.start, line.end);
    }

    public T getStart() {
        return start;
    }

    public void setStart(T start) {
        this.start = (T)start.clone();
    }

    public T getEnd() {
        return end;
    }

    public void setEnd(T end) {
        this.end = (T)end.clone();
    }

    public double length(){
        return this.start.distTo(end);
    }

    @Override
    public Line<T> clone(){
        try{
            Line res = (Line) super.clone();
            res.start = start.clone();
            res.end = end.clone();
            return res;

        } catch (CloneNotSupportedException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Line line = (Line) o;
        return Objects.equals(start, line.start) && Objects.equals(end, line.end)
                ||Objects.equals(start, line.end) && Objects.equals(end, line.start);
    }

    @Override
    public int hashCode() {
        return start.hashCode()+end.hashCode();
    }

    @Override
    public String toString() {
        return "Line{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}
