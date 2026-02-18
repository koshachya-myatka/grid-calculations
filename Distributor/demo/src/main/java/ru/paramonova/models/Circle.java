package ru.paramonova.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class Circle {
    private int x;
    private int y;
    private boolean color;
}
