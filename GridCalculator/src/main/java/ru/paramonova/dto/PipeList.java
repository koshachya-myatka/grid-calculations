package ru.paramonova.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class PipeList {
    List<Pipe> pipes;
}
