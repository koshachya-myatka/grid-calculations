package ru.paramonova.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class Circle {
    private double x;
    private double y;
    private boolean color;
}
