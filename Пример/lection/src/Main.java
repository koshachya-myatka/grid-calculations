import cats.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//5564 5895 4155 04
public class Main {
    public static void main(String[] args) {
        List<String> lstS=List.of("asd","Awer","tyu", "12","34","af", "12");
        /*1) посчитать количество каждой буквы
        2) дан список строк, разбить строки по пробелам, пустые строки убрать,
        лишние пробелы убрать. Для каждой подстроки
        выполнить её нормализацию (т.е. все буквы кроме маленькой первые. первая большая)
        получить итоговые строки отсортированные в списки по первой букве
         */
        Map<String,Integer> map=lstS.stream()
                .flatMapToInt(String::chars)
                .collect();


        long charCount=lstS.stream()
                .flatMapToInt(String::chars)
                .distinct()
                .count();
        System.out.println(charCount);

        long res = lstS.stream()
                .distinct().count();
        System.out.println(res);

        String str = lstS.stream()
                .filter(x -> x.startsWith("A") || x.startsWith("a"))
                .map(x -> "A" + x.substring(1))
                .reduce("", (x, y) -> x+y);
        System.out.println(str);//AsdAwerAf

        int num = lstS.stream()
                .filter(x -> x.length() == 2)
                .filter(x->x.matches("\\d+"))
                .map(Integer::parseInt)
                .reduce(0, (x, y) -> x+y);
        System.out.println(num); //46
        //преобразуем в числа
        //оставляем двузначные
        //складываем
        //сумму выводим на экран
        List<Integer> lst=Stream.of(1,2,3,4)
                .map(x->x*2)
                .toList();
        System.out.println(lst);



    }
    public static void takeCare(Meowable meowable){
        meowable.meow();
        meowable.meow();
        meowable.meow();
    }
    public static void takeCareDog(Barkable barkable){
        barkable.bark();
        barkable.bark();
    }

    public static <T, P> List<P> map(List<T> list, Action<T, P> action) {
        List<P> l = new ArrayList<>(list.size());
        for (T t : list) {
            l.add(action.act(t));
        }
        return l;
    }

    public static <T> List<T> filter(List<T> list, Filterator<T> fil) {
        List<T> res = new ArrayList<>();
        for (T t : list) {
            if (fil.filter(t)) res.add(t);
        }

        return res;
    }

    public static <T, R> R collect(List<T> list, Supplier<R> gen, BiConsumer<R, T> comb) {
        R res = gen.get();
        for (T t : list) {
            comb.accept(res, t);
        }
        return res;
    }
}

interface Filterator<T> {
    boolean filter(T t);
}

class Landth implements Action<String, Integer> {
    public Integer act(String str) {
        return str.length();
    }
}

class Abs implements Action<Integer, Integer> {
    @Override
    public Integer act(Integer integer) {
        return Math.abs(integer);
    }
}