public class Rectangle extends Figure{
    private Point2D leftUp;
    private Point2D rightDown;

    public Rectangle(Point2D leftUp, Point2D rightDown) {
        this.leftUp = leftUp;
        this.rightDown = rightDown;
    }

    public Point2D getLeftUp() {
        return leftUp;
    }

    public void setLeftUp(Point2D leftUp) {
        this.leftUp = leftUp;
    }

    public Point2D getRightDown() {
        return rightDown;
    }

    public void setRightDown(Point2D rightDown) {
        this.rightDown = rightDown;
    }

    @Override
    public double area(){
        int height = rightDown.getX() - leftUp.getX();
        int weight = leftUp.getY() - rightDown.getY();

        return height*weight;
    }
}
