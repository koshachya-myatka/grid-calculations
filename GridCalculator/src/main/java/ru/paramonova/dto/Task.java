package ru.paramonova.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Task {
    int taskId;
    int fieldWidth;
    int fieldLength;
    int totalWhiteCombinations;
    int totalBlackCombinations;
    long totalLineCombinations;
    List<Circle> whiteCircles;
    List<Circle> blackCircles;
}
