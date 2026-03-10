package ru.paramonova.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SolveRequest {
    int taskId;
    int subtaskId;
    byte[] jarCalculator;
    String jsonTaskData;
    String jsonSubtaskData;
    String distributorAddress;
}