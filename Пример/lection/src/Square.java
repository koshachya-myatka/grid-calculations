public class Square extends Rectangle{
    public Square(Point2D leftUp, Point2D rightDown) {
        super(leftUp, rightDown);
        int height = rightDown.getX() - leftUp.getX();
        int weight = leftUp.getY() - rightDown.getY();
        if(height!=weight) throw new IllegalArgumentException();

    }

    @Override
    public void setLeftUp(Point2D leftUp) {
        int height = getRightDown().getX() - leftUp.getX();
        int weight = leftUp.getY() - getRightDown().getY();
        if(height!=weight) throw new IllegalArgumentException();
        super.setLeftUp(leftUp);
    }

    @Override
    public void setRightDown(Point2D rightDown) {
        int height = rightDown.getX() - getLeftUp().getX();
        int weight = getLeftUp().getY() - rightDown.getY();
        if(height!=weight) throw new IllegalArgumentException();
        super.setRightDown(rightDown);
    }
}
