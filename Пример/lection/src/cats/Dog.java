package cats;

public class Dog implements Barkable{
    private String name;

    public Dog(String name) {
        this.name = name;
    }

    @Override
    public void bark() {
        System.out.println(name+": baw waw");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
