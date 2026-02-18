package cats;

//прокси
public class WarningMeow extends Cat{
    Cat meowable;

    public WarningMeow(Cat meowable) {
        super("qwertyu");
        this.meowable = meowable;
    }

    @Override
    public void meow() {
        System.out.print("Warning!!! ");
        meowable.meow();
    }

    @Override
    public void meow(int x) {
        meowable.meow(x);
    }

    @Override
    public String getName() {
        return meowable.getName();
    }

    @Override
    public void setName(String name) {
        meowable.setName(name);
    }
}
