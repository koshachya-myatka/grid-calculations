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
                List<Integer> positionBlack = numberToPositionCombination(4, blackCirclesNumber, i);
                List<Integer> positionWhite = numberToPositionCombination(12, whiteCirclesNumber, j);
                pipes.addAll(createPipes(positionBlack, task.getBlackCircles()));
                pipes.addAll(createPipes(positionWhite, task.getWhiteCircles()));
                results.add(calculateCombination(task.getFieldWidth(), task.getFieldLength(), pipes));
            }
        }
        return results;
    }

    private Result calculateCombination(int width, int length, List<Pipe> pipes) {
        PipeMatrix pipeMatrix = new PipeMatrix(width, length, pipes);
        return pipeMatrix.calculateResult();
    }

    private List<Pipe> createPipes(List<Integer> positionCombination, List<Circle> circles) {
        List<Pipe> pipes = new ArrayList<>();
        for (int i = 0; i < circles.size(); i++) {
            Circle circle = circles.get(i);
            int position = positionCombination.get(i);
            pipes.add(Pipe.builder()
                    .x(circle.getX())
                    .y(circle.getY())
                    .color(circle.isColor())
                    .position(position)
                    .build());
        }
        return pipes;
    }

    private List<Integer> numberToPositionCombination(int alphabetLength, int circlesNumber, int number) {
        List<Integer> pipePositions = new ArrayList<>();
        while (number > 0) {
            pipePositions.add(number % alphabetLength);
            number /= alphabetLength;
        }
        while (pipePositions.size() < circlesNumber) {
            pipePositions.add(0);
        }
        return pipePositions.reversed();
    }
}