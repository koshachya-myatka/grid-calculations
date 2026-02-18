import java.util.List;

public interface Grade<T> {
    boolean check(T value);
    T avgGrade(List<T> lst);
    boolean isExcellent(List<T> lst);
}
