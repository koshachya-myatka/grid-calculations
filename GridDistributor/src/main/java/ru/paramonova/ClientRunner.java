package ru.paramonova;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.paramonova.clients.MyGridClient;

import java.io.File;

@Component
@RequiredArgsConstructor
public class ClientRunner implements CommandLineRunner {
    private final MyGridClient client;

    @Override
    public void run(String... args) throws Exception {
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
