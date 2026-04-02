package ru.paramonova;


public class DbConnectionPool {
    private int size;
    private int count = 0;

    public DbConnectionPool(int numConnections) {
        size = numConnections;
    }

    public ThreadConnection tryGetConnection(Thread thread) {
        synchronized (this) {
            if (count == size) {
                return null;
            }
            count++;
        }
        return new ThreadConnection();
    }

    class ThreadConnection {
        private Thread owner = Thread.currentThread();
        boolean open = true;

        public String getString() {
            if (!owner.equals(Thread.currentThread())) {
                throw new RuntimeException();
            }
            return "Да, босс?";
        }

        public void close() {
            if (!owner.equals(Thread.currentThread())) {
                throw new RuntimeException();
            }
            open = false;
            count--;
        }
    }
}