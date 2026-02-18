import java.util.Objects;

public abstract class Atribute {
    private final String name;
    private final Object value;

    public Atribute(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Atribute atribute = (Atribute) o;
        return Objects.equals(name, atribute.name) && Objects.equals(value, atribute.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return "Atribute{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
