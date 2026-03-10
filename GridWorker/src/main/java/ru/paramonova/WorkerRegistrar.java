package ru.paramonova;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.paramonova.services.WorkerService;

@Component
@RequiredArgsConstructor
public class WorkerRegistrar implements CommandLineRunner {
    @Value("${server.port}")
    private String serverPort;

    private final WorkerService workerService;

    @Override
    public void run(String... args) {
        RestTemplate restTemplate = new RestTemplate();
        Integer workerId = restTemplate.postForObject(
                "http://localhost:8081/workers",
                "http://localhost:" + serverPort,
                Integer.class);
        if (workerId == null) {
            throw new RuntimeException("Не удалось зарегистрировать воркер");
        }
        workerService.setWorkerId(workerId);
        System.out.println("Воркер зарегистрирован с id " + workerId);
    }
}
