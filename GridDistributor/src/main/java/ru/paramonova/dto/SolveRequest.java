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
    int subtaskId;
    byte[] jarCalculator;
    String jsonTaskData;
    String jsonSubtaskData;
    String distributorAddress;
}