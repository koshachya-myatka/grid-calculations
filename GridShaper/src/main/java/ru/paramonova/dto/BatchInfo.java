package ru.paramonova.dto;

import lombok.*;
import ru.paramonova.grpc.Batch;

@Getter
@Setter
public class BatchInfo {
    Batch batch;
    BatchStatus status;
    long lastSendTime;

    public BatchInfo(Batch batch) {
        this.batch = batch;
        this.status = BatchStatus.NEW;
        this.lastSendTime = 0L;
    }
}
