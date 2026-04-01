package ru.paramonova.services;

import lombok.Getter;
import ru.paramonova.grpc.*;

import java.util.*;

public class BatchCreator {
    // x - вниз от 0 до length
    // y - вправо от 0 до width
    private final Map<Integer, List<Integer>> forbiddenBlackPipeYPositions = new HashMap<>();
    private final Map<Integer, List<Integer>> forbiddenBlackPipeXPositions = new HashMap<>();
    private final Map<Integer, List<Integer>> forbiddenWhitePipeYPositions = new HashMap<>();
    private final Map<Integer, List<Integer>> forbiddenWhitePipeXPositions = new HashMap<>();
    private final Map<Integer, List<Integer>> allowedLineXPositions =
            Map.of(0, List.of(0, 1, 3, 6),
                    1, List.of(0, 1, 3, 6),
                    2, List.of(2, 4, 5),
                    3, List.of(2, 4, 5),
                    4, List.of(0, 1, 3, 6),
                    5, List.of(0, 1, 3, 6),
                    6, List.of(2, 4, 5));
    private final Map<Integer, List<Integer>> allowedLineYPositions =
            Map.of(0, List.of(0, 2, 3, 4),
                    1, List.of(1, 5, 6),
                    2, List.of(0, 2, 3, 4),
                    3, List.of(1, 5, 6),
                    4, List.of(1, 5, 6),
                    5, List.of(0, 2, 3, 4),
                    6, List.of(0, 2, 3, 4));
    private final List<Circle> allCircles = new ArrayList<>();
    private final List<Circle> whiteCircles = new ArrayList<>();
    private final List<Circle> blackCircles = new ArrayList<>();
    private final int width;
    private final int length;
    private final int[][] matrix;
    private int[] whitePositions;
    private int[] blackPositions;
    private boolean finished = false;

    public BatchCreator(Task task) {
        width = task.getFieldWidth();
        length = task.getFieldLength();
        whiteCircles.addAll(task.getWhiteCirclesList());
        blackCircles.addAll(task.getBlackCirclesList());
        allCircles.addAll(whiteCircles);
        allCircles.addAll(blackCircles);
        matrix = new int[length][width];
        for (int[] ints : matrix) {
            Arrays.fill(ints, -1);
        }
        whitePositions = new int[whiteCircles.size()];
        blackPositions = new int[blackCircles.size()];
        forbiddenBlackPipeXPositions.put(0, List.of(0, 1));
        forbiddenBlackPipeXPositions.put(length - 1, List.of(2, 3));
        forbiddenBlackPipeYPositions.put(0, List.of(0, 3));
        forbiddenBlackPipeYPositions.put(width - 1, List.of(1, 2));
        forbiddenWhitePipeXPositions.put(0, List.of(1, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15));
        forbiddenWhitePipeXPositions.put(length - 1, List.of(0, 2, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15));
        forbiddenWhitePipeYPositions.put(0, List.of(0, 1, 2, 3, 4, 5, 6, 7, 9, 11, 13, 14, 15));
        forbiddenWhitePipeYPositions.put(width - 1, List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 12, 14, 15));
    }

    public boolean isFinished() {
        return finished;
    }

    public PipeList create() {
        if (finished) {
            return null;
        }
        while (true) {
            if (!nextCombination()) {
                return null;
            }
            List<Pipe> pipes = buildPipes();
            if (checkBorders(pipes)) {
                return PipeList.newBuilder()
                        .addAllPipes(pipes)
                        .build();
            }
        }
    }

    private boolean nextCombination() {
        if (incrementPosition(whitePositions, 16)) {
            return true;
        }
        Arrays.fill(whitePositions, 0);
        if (incrementPosition(blackPositions, 4)) {
            return true;
        }
        finished = true;
        return false;
    }

    private List<Pipe> buildPipes() {
        List<Pipe> pipes = new ArrayList<>();
        for (int i = 0; i < whitePositions.length; i++) {
            Circle c = whiteCircles.get(i);
            pipes.add(Pipe.newBuilder()
                    .setX(c.getX())
                    .setY(c.getY())
                    .setColor(true)
                    .setPosition(whitePositions[i])
                    .build());
        }
        for (int i = 0; i < blackPositions.length; i++) {
            Circle c = blackCircles.get(i);
            pipes.add(Pipe.newBuilder()
                    .setX(c.getX())
                    .setY(c.getY())
                    .setColor(false)
                    .setPosition(blackPositions[i])
                    .build());
        }
        return pipes;
    }

    private boolean incrementPosition(int[] arr, int base) {
        for (int i = 0; i < arr.length; i++) {
            arr[i]++;
            if (arr[i] < base) {
                return true;
            }
            arr[i] = 0;
        }
        return false;
    }

    private boolean checkBorders(List<Pipe> pipes) {
        List<Pipe> whitePipes = pipes.stream().filter(Pipe::getColor).toList();
        List<Pipe> blackPipes = pipes.stream().filter(pipe -> !pipe.getColor()).toList();
        // проверка, что белые трубы упираются некорректно в границу
        for (Pipe pipe : whitePipes) {
            if (pipe.getX() == 0 || pipe.getX() == length - 1) {
                if (forbiddenWhitePipeXPositions.get(pipe.getX()).contains(pipe.getPosition())) {
                    return false;
                }
            }
            if (pipe.getY() == 0 || pipe.getY() == width - 1) {
                if (forbiddenWhitePipeYPositions.get(pipe.getY()).contains(pipe.getPosition())) {
                    return false;
                }
            }
        }
        // проверка, что черные трубы упираются некорректно в границу
        for (Pipe pipe : blackPipes) {
            if ((pipe.getX() == 0 || pipe.getX() == length - 1)
                    && forbiddenBlackPipeXPositions.get(pipe.getX()).contains(pipe.getPosition())) {
                return false;
            }
            if ((pipe.getY() == 0 || pipe.getY() == width - 1)
                    && forbiddenBlackPipeYPositions.get(pipe.getY()).contains(pipe.getPosition())) {
                return false;
            }
        }
        return true;
    }
}