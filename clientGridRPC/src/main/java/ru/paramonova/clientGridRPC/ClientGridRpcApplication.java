package ru.paramonova.clientGridRPC;


public class ClientGridRpcApplication {
    // java ClientGridRpcApplication "D:\Study\Grid Calculations\test2.txt"

    public static void main(String[] args) throws Exception {
        MyGridClient client = new MyGridClient("localhost", 8080);
        try {
            int taskId = client.addTask(args[0]);
//            int taskId = 0;
            client.getTaskInfo(taskId);
            client.streamBatches(taskId, 10);
        } finally {
            client.shutdown();
        }
    }
}
