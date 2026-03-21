package ru.paramonova.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Result {
    int batchId;
    boolean connected;
    List<Pipe> pipes;
    List<Line> lines;
}
