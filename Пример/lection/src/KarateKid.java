import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class KarateKid {
    String name;

    public KarateKid(String name) {
        this.name = name;
    }

    public void punch(){
        System.out.println(name+": KYA!!!");
    }
    public void kick(){
        System.out.println(name+": O!");
    }
    public void jump(){
        System.out.println(name+": aaaa!!");
    }
}

interface KarateAction{
    void perform(KarateKid kid);
}

class Combo implements KarateAction{
    String title;
    List<KarateAction> actions = new ArrayList<>();

    public Combo(String title, List<KarateAction> actions) {
        this.title = title;
        this.actions = actions;
    }

    public static ComboBuilder name(String name) {
        return new ComboBuilder(name);
    }

    public void perform(KarateKid kid){
        for(KarateAction k: actions){
            k.perform(kid);
        }
    }

    static class ComboBuilder{
        String name;
        List<KarateAction> actions = new ArrayList<>();

        public ComboBuilder(String name) {
            this.name = name;
        }

        public ComboBuilder action(KarateAction action) {
            actions.add(action);
            return this;
        }

        public Combo make() {
            return new Combo(name,actions);
        }
    }
}
