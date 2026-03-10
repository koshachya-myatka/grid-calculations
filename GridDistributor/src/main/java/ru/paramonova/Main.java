package ru.paramonova;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        MyGridClient client = new MyGridClient("localhost", 8080, new DistributorService());
        for (int i = 0; i < 3; i++) {
            client.registerWorker();
        }
        try {
            String pathToFile = new File(System.getProperty("user.dir")).getParent();
            int taskId = client.addTask(pathToFile + "\\test2.txt");
            client.registerTask(taskId);
            client.getTaskInfo(taskId);
            client.streamBatches(taskId);
        } finally {
            client.await();
        }
    }
}