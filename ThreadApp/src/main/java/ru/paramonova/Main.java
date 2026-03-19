package ru.paramonova;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        int[] array = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int sumResult = threadSum(array, 5);
    }

    public static int threadSum(int[] array, int numThread) {
        List<Integer> results = new ArrayList<>();
        for (int i = 0; i < numThread - 1; i++) {
            int startInd = i * array.length / numThread;
            int endInd = i * array.length / numThread + array.length / numThread;
            Thread thread = new Thread(() -> {
                results.add(sumArray(Arrays.copyOfRange(array, startInd, endInd)));
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        int res = 0;
        for (int result : results) {
            res += result;
        }
        return res;
    }

    public static int sumArray(int[] array) {
        int res = 0;
        for (int j : array) {
            res += j;
        }
        return res;
    }
}