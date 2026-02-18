public class Color extends Atribute{
    public Color(String value) {
        super("цвет", value);
    }

    @Override
    public String getValue() {
        return (String) super.getValue();
    }
}
