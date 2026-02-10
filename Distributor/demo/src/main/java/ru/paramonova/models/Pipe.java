package ru.paramonova.models;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class Pipe {
    private double x;
    private double y;
    private boolean color;
    private int position;
}
