package ru.paramonova.models;

import ru.paramonova.annotations.Calculator;
import ru.paramonova.annotations.Main;
import ru.paramonova.annotations.Param;
import ru.paramonova.dto.*;

import java.util.ArrayList;
import java.util.List;

@Calculator
public class BatchCalculator {
    @Main
    public List<Result> calculate(@Param("task") Task task, @Param("batch") Batch batch) {
        if (task.getTaskId() != batch.getTaskId()) {
            throw new RuntimeException("Полученная подзадача не относится к полученной задаче");
        }
        List<Result> results = new ArrayList<>();
        int startB = batch.getStartBlackCombination();
        int endB = startB + batch.getNumberBlackCombinations();
        int startW = batch.getStartWhiteCombination();
        int endW = startW + batch.getNumberWhiteCombinations();
        int blackCirclesNumber = task.getBlackCircles().size();
        int whiteCirclesNumber = task.getWhiteCircles().size();
        for (int i = startB; i < endB; i++) {
            for (int j = startW; j < endW; j++) {
                List<Pipe> pipes = new ArrayList<>();
                String positionBlack = numberToPositionCombination(4, blackCirclesNumber, i);
                String positionWhite = numberToPositionCombination(8, whiteCirclesNumber, j);
                pipes.addAll(createPipes(positionBlack, task.getBlackCircles()));
                pipes.addAll(createPipes(positionWhite, task.getWhiteCircles()));
                results.add(calculateCombination(pipes));
            }
        }
        return results;
    }

    private Result calculateCombination(List<Pipe> pipes) {
        //TODO вот тут должно быть адекватное решение для конкретной комбинации на возможность соединения труб линией
        return Result.builder()
                .connected(false)
                .pipes(pipes)
                .build();
    }

    private List<Pipe> createPipes(String positionCombination, List<Circle> circles) {
        List<Pipe> pipes = new ArrayList<>();
        for (int i = 0; i < circles.size(); i++) {
            Circle circle = circles.get(i);
            int position = Character.getNumericValue(positionCombination.charAt(i));
            pipes.add(Pipe.builder()
                    .x(circle.getX())
                    .y(circle.getY())
                    .color(circle.isColor())
                    .position(position)
                    .build());
        }
        return pipes;
    }

    private String numberToPositionCombination(int alphabetLength, int circlesNumber, int number) {
        StringBuilder builder = new StringBuilder();
        while (number > 0) {
            builder.append(number % alphabetLength);
            number /= alphabetLength;
        }
        while (builder.length() < circlesNumber) {
            builder.append("0");
        }
        return new StringBuilder(builder).reverse().toString();
    }
}