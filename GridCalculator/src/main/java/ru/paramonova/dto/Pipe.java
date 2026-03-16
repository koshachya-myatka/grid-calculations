package ru.paramonova.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Pipe {
    int x;
    int y;
    boolean color;
    int position;
}
