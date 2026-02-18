package cats;

//адаптирует
public class BarkableCat implements Barkable{
    Meowable meowable;

    public BarkableCat(Meowable meowable) {
        this.meowable = meowable;
    }

    @Override
    public void bark() {
        meowable.meow();
    }
}
