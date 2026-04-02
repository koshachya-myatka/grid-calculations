package ru.paramonova;

public class SummingThread extends Thread {
    int start;
    int end;
    public int result = 0;

    SummingThread(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        for (int i = start; i < end; i++) {
            result += Main.array[i];
        }
    }
}
