import java.util.Random;

public class Connection {
    private Random random= new Random();
    public void register(){
        if(!random.nextBoolean()) throw new ConnectionLostException();
        System.out.println("connection has been registred");
    }
    public void unregister(){
        System.out.println("connection has been terminated");
    }
    public String getData(){
        register();
        if(!random.nextBoolean()) throw new ConnectionLostException();
        String resultData="data";
        unregister();
        return resultData;

    }

}
