package cats;
//прокся + декоратор
public class MeowCounter implements Meowable{
    Meowable meowable;
    int count;

    public MeowCounter(Meowable meowable) {
        this.meowable = meowable;
    }

    public int getCount() {
        return count;
    }

    @Override
    public void meow() {
        meowable.meow();
        count++;
    }
}
