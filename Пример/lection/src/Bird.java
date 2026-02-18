public abstract class Bird {
    private String text;

    public Bird(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void sing() {
        System.out.println(text);
    }
}
