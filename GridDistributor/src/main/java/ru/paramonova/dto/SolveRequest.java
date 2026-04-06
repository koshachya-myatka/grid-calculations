package ru.paramonova.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class SolveRequest {
    int taskId;
    long subtaskId;
    byte[] jarCalculator;
    String jsonTaskData;
    String jsonSubtaskData;
    String resultUrl;
}