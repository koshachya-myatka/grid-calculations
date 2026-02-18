package cats;

public class Cat implements Meowable{
    private String name;

    public Cat(String name) {
        this.name = name;
    }

    @Override
    public void meow() {
        System.out.println(name+": meow");
    }
    public void meow(int x) {
        System.out.println(name+": meow"+x);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
