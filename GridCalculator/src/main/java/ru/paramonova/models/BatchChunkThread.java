package ru.paramonova.models;

import ru.paramonova.dto.*;

import java.util.ArrayList;
import java.util.List;

public class BatchChunkThread extends Thread {
    Task task;
    Batch batch;
    int whiteCirclesNumber;
    int blackCirclesNumber;
    long startW;
    long endW;
    long startB;
    long endB;
    public List<Result> results = new ArrayList<>();

    public BatchChunkThread(Task task, Batch batch, int whiteCirclesNumber, int blackCirclesNumber,
                            long startW, long endW, long startB, long endB) {
        this.task = task;
        this.batch = batch;
        this.whiteCirclesNumber = whiteCirclesNumber;
        this.blackCirclesNumber = blackCirclesNumber;
        this.startW = startW;
        this.endW = endW;
        this.startB = startB;
        this.endB = endB;
    }

    @Override
    public void run() {
        for (long i = startB; i < endB; i++) {
            for (long j = startW; j < endW; j++) {
                List<Pipe> pipes = new ArrayList<>();
                List<Integer> positionBlack = numberToPositionCombination(4, blackCirclesNumber, i);
                List<Integer> positionWhite = numberToPositionCombination(16, whiteCirclesNumber, j);
                pipes.addAll(createPipes(positionBlack, task.getBlackCircles()));
                pipes.addAll(createPipes(positionWhite, task.getWhiteCircles()));
                Result currRes = calculateCombination(batch, task, pipes);
                if (currRes.isConnected()) {
                    results.add(currRes);
                }
            }
        }
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