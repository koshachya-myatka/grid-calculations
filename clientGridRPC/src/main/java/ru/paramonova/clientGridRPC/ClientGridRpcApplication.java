package ru.paramonova.clientGridRPC;


public class ClientGridRpcApplication {
    // java ClientGridRpcApplication "D:\Study\Grid Calculations\test2.txt"

    public static void main(String[] args) throws Exception {
        GridClient client = new GridClient("localhost", 8080);
        try {
//            int taskId = client.addTask(args[0]);
            int taskId = 1;
            client.getTaskInfo(taskId);
            client.streamSubtasks(taskId, 3);
        } finally {
            client.shutdown();
        }
    }
}
