package ru.paramonova.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ResultRequest {
    private int workerId;
    private int taskId;
    private int subtaskId;
    private String jsonResult;
}
