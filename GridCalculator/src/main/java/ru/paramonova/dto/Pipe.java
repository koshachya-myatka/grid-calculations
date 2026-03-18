package ru.paramonova.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Pipe {
    int x;
    int y;
    boolean color;
    int position;
}
