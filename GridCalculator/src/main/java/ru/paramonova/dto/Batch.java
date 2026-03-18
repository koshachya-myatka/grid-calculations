package ru.paramonova.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Batch {
    int batchId;
    int taskId;
    int startWhiteCombination;
    int numberWhiteCombinations;
    int startBlackCombination;
    int numberBlackCombinations;
}
