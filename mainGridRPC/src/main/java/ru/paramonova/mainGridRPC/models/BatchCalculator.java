package ru.paramonova.mainGridRPC.models;

import lombok.Getter;
import lombok.ToString;
import ru.paramonova.grpc.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
public class BatchCalculator {
    private Task task;
    private Batch batch;

    public BatchCalculator(Task task, Batch batch) {
        this.task = task;
        this.batch = batch;
    }

    public boolean calculateCombinations() {
        boolean result = false;
        int startB = batch.getStartBlackCombination();
        int endB = startB + batch.getNumberBlackCombinations();
        int startW = batch.getStartWhiteCombination();
        int endW = startW + batch.getNumberWhiteCombinations();
        int blackCirclesNumber = task.getBlackCirclesCount();
        int whiteCirclesNumber = task.getWhiteCirclesCount();
        for (int i = startB; i < endB; i++) {
            for (int j = startW; j < endW; j++) {
                List<Pipe> pipes = new ArrayList<>();
                String positionBlack = numberToPositionCombination(4, blackCirclesNumber, i);
                String positionWhite = numberToPositionCombination(8, whiteCirclesNumber, j);
                pipes.addAll(createPipes(positionBlack, task.getBlackCirclesList()));
                pipes.addAll(createPipes(positionWhite, task.getWhiteCirclesList()));
                result = calculateCombination(pipes);
            }
        }
        return result;
    }

    private boolean calculateCombination(List<Pipe> pipes) {
        //TODO вот тут должно быть адекватное решение для конкретной комбинации на возможность соединения труб линией
        return false;
    }

    private List<Pipe> createPipes(String positionCombination, List<Circle> circles) {
        List<Pipe> pipes = new ArrayList<>();
        for (int i = 0; i < circles.size(); i++) {
            Circle circle = circles.get(i);
            int position = Character.getNumericValue(positionCombination.charAt(i));
            pipes.add(Pipe.newBuilder()
                    .setX(circle.getX())
                    .setY(circle.getY())
                    .setColor(circle.getColor())
                    .setPosition(position)
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
