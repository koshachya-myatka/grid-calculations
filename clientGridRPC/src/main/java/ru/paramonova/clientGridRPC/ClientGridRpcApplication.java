package ru.paramonova.clientGridRPC;


public class ClientGridRpcApplication {
    // java ClientGridRpcApplication "D:\Study\Grid Calculations\test2.txt"
    // java ClientGridRpcApplication "D:\Study\grid-calculations\test2.txt"

    public static void main(String[] args) throws Exception {
        MyGridClient client = new MyGridClient("localhost", 8080, new DistributorService());
        for (int i = 0; i < 3; i++) {
            client.registerWorker();
        }
        try {
            int taskId = client.addTask("D:\\Study\\Grid Calculations\\test2.txt");
            client.registerTask(taskId);
            client.getTaskInfo(taskId);
            client.streamBatches(taskId);
        } finally {
            client.await();
        }
    }
}
