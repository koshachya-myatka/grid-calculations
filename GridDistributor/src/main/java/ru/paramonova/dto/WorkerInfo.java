package ru.paramonova.dto;

import lombok.*;

import java.util.concurrent.atomic.AtomicBoolean;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class WorkerInfo {
    private int workerId;
    private String address;
    private AtomicBoolean busy = new AtomicBoolean(false);
    private Integer taskId;

    public WorkerInfo(int workerId, String address) {
        this.workerId = workerId;
        this.address = address;
    }
}
