package ru.paramonova.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Line {
    int x;
    int y;
    int position;
}
