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
        long startB = batch.getStartBlackCombination();
        long endB = startB + batch.getNumberBlackCombinations();
        long startW = batch.getStartWhiteCombination();
        long endW = startW + batch.getNumberWhiteCombinations();
        int blackCirclesNumber = task.getBlackCircles().size();
        int whiteCirclesNumber = task.getWhiteCircles().size();
        for (long i = startB; i < endB; i++) {
            for (long j = startW; j < endW; j++) {
                List<Pipe> pipes = new ArrayList<>();
                List<Integer> positionBlack = numberToPositionCombination(4, blackCirclesNumber, i);
                List<Integer> positionWhite = numberToPositionCombination(12, whiteCirclesNumber, j);
                pipes.addAll(createPipes(positionBlack, task.getBlackCircles()));
                pipes.addAll(createPipes(positionWhite, task.getWhiteCircles()));
                Result currRes = calculateCombination(batch, task, pipes);
                if (currRes.isConnected()) {
                    results.add(currRes);
                }
            }
        }
        return results;
    }

    private Result calculateCombination(Batch batch, Task task, List<Pipe> pipes) {
        PipeMatrix pipeMatrix = new PipeMatrix(batch.getBatchId(), task.getFieldWidth(),
                task.getFieldLength(), pipes);
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

    private List<Integer> numberToPositionCombination(int alphabetLength, int circlesNumber, long number) {
        List<Integer> pipePositions = new ArrayList<>();
        while (number > 0) {
            pipePositions.add((int) number % alphabetLength);
            number /= alphabetLength;
        }
        while (pipePositions.size() < circlesNumber) {
            pipePositions.add(0);
        }
        return pipePositions.reversed();
    }
}