package ru.paramonova.dto;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class PipeDto {
    private int x;
    private int y;
    private boolean color;
    private int position;
}
