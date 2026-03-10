package ru.paramonova.clients;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.paramonova.dto.SolveRequest;
import ru.paramonova.models.WorkerInfo;

@Component
public class WorkerClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendSubtask(WorkerInfo worker, SolveRequest request) {
        System.out.println("ОТПРАВИЛ ЗАДАЧУ НА ВОРКЕР");
        String url = worker.getAddress() + "/solveSubtask";
        restTemplate.postForEntity(url, request, Void.class);
    }
}
