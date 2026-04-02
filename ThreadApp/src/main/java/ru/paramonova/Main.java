package ru.paramonova;

import java.util.ArrayList;
import java.util.List;

public class Main {
    static int[] array = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    public static void main(String[] args) {
//        System.out.println(threadSum(5));
        DbConnectionPool pool = new DbConnectionPool(5);
        Thread[] threads = new Thread[7];
        for (int i = 0; i < 7; i++) {
            threads[i] = new Thread();
        }
    }

    public static int threadSum(int numThread) {
        int result = 0;
        List<SummingThread> threads = new ArrayList<>();
        for (int i = 0; i < numThread; i++) {
            int startInd = i * array.length / numThread;
            int endInd = i * array.length / numThread + array.length / numThread;
            threads.add(new SummingThread(startInd, endInd));
            threads.get(i).start();
        }
        for (int i = 0; i < numThread; i++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            result += threads.get(i).result;
        }
        return result;
    }
}