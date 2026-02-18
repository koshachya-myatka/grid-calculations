public class Point3D extends Point2D{
    private int z;
    public Point3D(int x, int y, int z) {
        super(x, y);
        this.z = z;
    }

    public double distTo(Point2D point3D) {
        return 2;
    }
}
