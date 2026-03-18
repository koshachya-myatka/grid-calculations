package ru.paramonova.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Circle {
    int x;
    int y;
    boolean color;
}
