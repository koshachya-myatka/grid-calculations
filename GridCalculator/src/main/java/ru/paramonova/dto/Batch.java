package ru.paramonova.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Batch {
    long batchId;
    int taskId;
    long startWhiteCombination;
    long numberWhiteCombinations;
    long startBlackCombination;
    long numberBlackCombinations;
}
