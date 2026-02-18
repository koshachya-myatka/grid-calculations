import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Point {
    private int[] coordinats = new int[3];
    private List<Atribute> atributes;

    public Point(int x) {
        coordinats[0] = x;
    }

    public Point(int x, int y) {
        coordinats[0] = x;
        coordinats[1] = y;
    }

    public Point(int x, int y, int z) {
        coordinats[0] = x;
        coordinats[1] = y;
        coordinats[2] = z;
    }

    public Point addAtribute(Atribute atribute){
        atributes.add(atribute);
        return this;
    }

    public List<Atribute> getAtributes() {
        return new ArrayList<>(atributes);
    }

    public void setAtributes(List<Atribute> atributes) {
        this.atributes = new ArrayList<>(atributes);
    }

    public void setCoordinats(int[] coordinats) {
        if (coordinats.length > 3) throw new IllegalArgumentException();
        for (int i = 0; i < coordinats.length; i++){
            this.coordinats[i] = coordinats[i];
        }
    }

    public int[] getCoordinats() {
        int[] res = new int[3];
        for (int i = 0; i < coordinats.length; i++){
            res[i] = coordinats[i];
        }
        return res;
    }

    @Override
    public String toString() {
        return "Точка с координатами: " + Arrays.toString(coordinats) + "; характеристики: " + atributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        if (!Arrays.equals(coordinats, point.coordinats)) return false;
        if (atributes.size() != point.atributes.size()) return false;
        for (Atribute atribute : atributes){
            if(!(point.atributes.contains(atribute))) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (Atribute atribute : atributes){
            result += atribute.hashCode();
        }
        result = 31 * result + Arrays.hashCode(coordinats);
        return result;
    }
}
