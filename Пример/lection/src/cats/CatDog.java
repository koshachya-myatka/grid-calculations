package cats;

public class CatDog implements Barkable,Meowable{
    private Cat cat;
    private Dog dog;

    public CatDog(String name) {
        cat=new Cat(name);
        dog=new Dog(name);
    }

    @Override
    public void bark() {
        dog.bark();
    }

    @Override
    public void meow() {
        cat.meow();
    }
}
