import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
/*
-----1) не моя проблема
2) ограничить T
3) rule и все все все

 */
public class Student<T extends Comparable<T>> implements Cloneable, Comparable<Student<T>>{
    private String name;
    private List<T> grades = new ArrayList<>();
    private final Grade<T> rule;

    public Student(String name, Grade<T> rule) {
        this.name = name;
        this.rule = rule;
    }

    public Student(String name) {
        this(name, null);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<T> getGrades() {
        return new ArrayList<>(grades);
    }

    public void setGrades(List<T> grades) {
        for (T x : grades) {
            if (!rule.check(x)) throw new IllegalMarkException();
        }
        grades.clear();
        grades.addAll(grades);
    }

    public void addGrade(T grade) {
        if (!rule.check(grade)) throw new IllegalMarkException();
        grades.add(grade);
    }
    public boolean isExcellent() {
        if (grades.isEmpty()) return false;

        return rule.isExcellent(grades);
    }
    public T avgGrade() {
        return rule.avgGrade(grades);
    }

    @Override
    public int compareTo(Student<T> o) {
        return this.avgGrade().compareTo(o.avgGrade());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(name, student.name) && Objects.equals(avgGrade(), student.avgGrade());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, avgGrade());
    }

    @Override
    protected Student<T> clone() {
        try{
            Student copy = (Student) super.clone();
            copy.setGrades(this.grades);
            return copy;
        }
        catch (CloneNotSupportedException c){
            throw new RuntimeException(c);
        }
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", grades=" + grades +
                '}';
    }

}
interface Excellent<T>{
    boolean isExcellent(List<T> ts);
}