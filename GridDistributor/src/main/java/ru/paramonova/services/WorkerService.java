package ru.paramonova.services;

import org.springframework.stereotype.Service;
import ru.paramonova.models.WorkerInfo;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class WorkerService {
    private final Map<Integer, WorkerInfo> workers = new ConcurrentHashMap<>();
    private final AtomicInteger nextWorkerId = new AtomicInteger(0);

    public int registerWorker(String address) {
        int workerId = nextWorkerId.getAndIncrement();
        WorkerInfo worker = new WorkerInfo(workerId, address);
        workers.put(workerId, worker);
        return workerId;
    }

    public Optional<WorkerInfo> findFreeWorker() {
        for (WorkerInfo worker : workers.values()) {
            if (worker.getBusy().compareAndSet(false, true)) {
                return Optional.of(worker);
            }
        }
        return Optional.empty();
    }

    public void releaseWorker(int id) {
        WorkerInfo worker = workers.get(id);
        if (worker != null) {
            worker.getBusy().set(false);
        }
    }
}
