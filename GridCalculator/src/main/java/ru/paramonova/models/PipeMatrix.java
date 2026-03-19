package ru.paramonova.models;

import ru.paramonova.dto.Pipe;

import java.util.*;

public class PipeMatrix {
    // x - вниз от 0 до length
    // y - вправо от 0 до width
    private final Map<Integer, List<Integer>> forbiddenBlackPipeYPositions = new HashMap<>();
    private final Map<Integer, List<Integer>> forbiddenBlackPipeXPositions = new HashMap<>();
    private final Map<Integer, List<Integer>> forbiddenWhitePipeYPositions = new HashMap<>();
    private final Map<Integer, List<Integer>> forbiddenWhitePipeXPositions = new HashMap<>();
    private final Map<Integer, List<Integer>> allowedLineXPositions =
            Map.of(1, List.of(0, 1, 3, 6),
                    2, List.of(2, 4, 5),
                    3, List.of(2, 4, 5),
                    4, List.of(0, 1, 3, 6),
                    5, List.of(0, 1, 3, 6),
                    6, List.of(2, 4, 5));
    private final Map<Integer, List<Integer>> allowedLineYPositions =
            Map.of(1, List.of(1, 5, 6),
                    2, List.of(0, 2, 3, 4),
                    3, List.of(1, 5, 6),
                    4, List.of(1, 5, 6),
                    5, List.of(0, 2, 3, 4),
                    6, List.of(0, 2, 3, 4));
    private final Map<Integer, List<Integer>> forbiddenLineBorderXPositions = new HashMap<>();
    private final Map<Integer, List<Integer>> forbiddenLineBorderYPositions = new HashMap<>();
    private final int width;
    private final int length;
    private final List<Pipe> whitePipes = new ArrayList<>();
    private final List<Pipe> blackPipes = new ArrayList<>();
    private final int[][] matrix;

    public PipeMatrix(int width, int length, List<Pipe> pipes) {
        this.width = width;
        this.length = length;
        for (Pipe pipe : pipes) {
            if (pipe.isColor()) {
                whitePipes.add(pipe);
            } else {
                blackPipes.add(pipe);
            }
        }
        matrix = new int[width][length];
        for (int[] ints : matrix) {
            Arrays.fill(ints, 0);
        }
        forbiddenBlackPipeXPositions.put(0, List.of(0, 1));
        forbiddenBlackPipeXPositions.put(length - 1, List.of(2, 3));
        forbiddenBlackPipeYPositions.put(0, List.of(0, 3));
        forbiddenBlackPipeYPositions.put(width - 1, List.of(1, 2));
        forbiddenWhitePipeXPositions.put(0, List.of(1, 3, 4, 6, 7, 8, 9, 10, 11));
        forbiddenWhitePipeXPositions.put(length - 1, List.of(0, 2, 5, 6, 7, 8, 9, 10, 11));
        forbiddenWhitePipeYPositions.put(0, List.of(0, 1, 2, 3, 4, 5, 7, 9, 11));
        forbiddenWhitePipeYPositions.put(width - 1, List.of(0, 1, 2, 3, 4, 5, 6, 8, 10));
        forbiddenLineBorderXPositions.put(0, List.of(2, 4, 5));
        forbiddenLineBorderXPositions.put(length - 1, List.of(2, 3, 6));
        forbiddenLineBorderYPositions.put(0, List.of(1, 5, 6));
        forbiddenLineBorderYPositions.put(width - 1, List.of(1, 3, 4));
    }

    public boolean calculateResult() {
        for (Pipe pipe : whitePipes) {
            if ((pipe.getX() == 0 || pipe.getX() == length - 1)
                    && forbiddenWhitePipeXPositions.get(pipe.getX()).contains(pipe.getPosition())) {
                return false;
            }
            if ((pipe.getY() == 0 || pipe.getY() == width - 1)
                    && forbiddenWhitePipeYPositions.get(pipe.getY()).contains(pipe.getPosition())) {
                return false;
            }
        }
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
        if (!pipeToLine()) {
            return false;
        }
        return checkAllLineCombinations();
    }

    private boolean pipeToLine() {
        for (Pipe pipe : blackPipes) {
            int x = pipe.getX();
            int y = pipe.getY();
            switch (pipe.getPosition()) {
                case 0:
                    if (y + 1 < width && matrix[x][y + 1] != 0 && !allowedLineYPositions.get(5).contains(matrix[x][y + 1])) {
                        return false;
                    }
                    if (x + 1 < length && matrix[x + 1][y] != 0 && !allowedLineYPositions.get(5).contains(matrix[x + 1][y])) {
                        return false;
                    }
                    if (x - 1 >= 0 && y + 1 < width && matrix[x - 1][y + 1] != 0 && !allowedLineYPositions.get(2).contains(matrix[x - 1][y + 1])) {
                        return false;
                    }
                    if (x + 1 < length && y - 1 >= 0 && matrix[x + 1][y - 1] != 0 && !allowedLineYPositions.get(1).contains(matrix[x + 1][y - 1])) {
                        return false;
                    }
                    matrix[x - 1][y] = 2;
                    matrix[x][y - 1] = 1;
                    matrix[x][y] = 5;
                    break;
                case 1:
                    if (y + 2 < width && matrix[x][y + 2] != 0 && !allowedLineYPositions.get(1).contains(matrix[x][y + 2])) {
                        return false;
                    }
                    if (x + 1 < length && y + 1 < width && matrix[x + 1][y + 1] != 0 && !allowedLineYPositions.get(1).contains(matrix[x + 1][y + 1])) {
                        return false;
                    }
                    if (x - 1 >= 0 && y + 1 < width && matrix[x - 1][y + 1] != 0 && !allowedLineYPositions.get(2).contains(matrix[x - 1][y + 1])) {
                        return false;
                    }
                    if (x + 1 < length && matrix[x + 1][y] != 0 && !allowedLineYPositions.get(4).contains(matrix[x + 1][y])) {
                        return false;
                    }
                    matrix[x - 1][y] = 2;
                    matrix[x][y] = 4;
                    matrix[x][y + 1] = 1;
                    break;
                case 2:
                    if (y + 2 < width && matrix[x][y + 2] != 0 && !allowedLineYPositions.get(1).contains(matrix[x][y + 2])) {
                        return false;
                    }
                    if (x + 1 < length && y + 1 < width && matrix[x + 1][y + 1] != 0 && !allowedLineYPositions.get(1).contains(matrix[x + 1][y + 1])) {
                        return false;
                    }
                    if (x + 1 < length && y + 1 < width && matrix[x + 1][y + 1] != 0 && !allowedLineYPositions.get(2).contains(matrix[x + 1][y + 1])) {
                        return false;
                    }
                    if (x + 2 < length && matrix[x + 2][y] != 0 && !allowedLineYPositions.get(2).contains(matrix[x + 2][y])) {
                        return false;
                    }
                    matrix[x][y] = 3;
                    matrix[x][y + 1] = 1;
                    matrix[x + 1][y] = 2;
                    break;
                default:
                    if (y - 1 >= 0 && x + 1 < length && matrix[x + 1][y - 1] != 0 && !allowedLineYPositions.get(1).contains(matrix[x + 1][y - 1])) {
                        return false;
                    }
                    if (y + 1 < width && matrix[x][y + 1] != 0 && !allowedLineYPositions.get(6).contains(matrix[x][y + 1])) {
                        return false;
                    }
                    if (x + 1 < length && y + 1 < width && matrix[x + 1][y + 1] != 0 && !allowedLineYPositions.get(2).contains(matrix[x + 1][y + 1])) {
                        return false;
                    }
                    if (x + 2 < length && matrix[x + 2][y] != 0 && !allowedLineYPositions.get(2).contains(matrix[x + 2][y])) {
                        return false;
                    }
                    matrix[x][y - 1] = 1;
                    matrix[x][y] = 6;
                    matrix[x + 1][y] = 2;
            }
        }
        Map<Integer, List<Integer>> whiteLinePositions = new HashMap<>();
        whiteLinePositions.putAll(
                Map.of(0, List.of(1, 1, 6), 1, List.of(1, 1, 5), 2, List.of(3, 1, 1), 3, List.of(4, 1, 1),
                        4, List.of(4, 1, 5), 5, List.of(3, 1, 6)));
        whiteLinePositions.putAll(
                Map.of(6, List.of(3, 2, 2), 7, List.of(6, 2, 2),
                        8, List.of(2, 2, 4), 9, List.of(2, 2, 5), 10, List.of(3, 2, 4), 11, List.of(6, 2, 5)));
        for (Pipe pipe : whitePipes) {
            int x = pipe.getX();
            int y = pipe.getY();
            List<Integer> lines = whiteLinePositions.get(pipe.getPosition());
            if (pipe.getPosition() <= 5) {
                if (y - 1 >= 0 && x + 1 < length && matrix[x + 1][y - 1] != 0 && !allowedLineYPositions.get(lines.get(0)).contains(matrix[x + 1][y - 1])) {
                    return false;
                }
                if (x + 1 < length && matrix[x + 1][y] != 0 && !allowedLineYPositions.get(lines.get(1)).contains(matrix[x + 1][y])) {
                    return false;
                }
                if (y + 1 < width && x + 1 < length && matrix[x + 1][y + 1] != 0 && !allowedLineYPositions.get(lines.get(2)).contains(matrix[x + 1][y + 1])) {
                    return false;
                }
                if (y + 2 < width && x + 1 < length && matrix[x + 1][y + 2] != 0 && !allowedLineYPositions.get(lines.get(2)).contains(matrix[x + 1][y + 2])) {
                    return false;
                }
                matrix[x][y - 1] = lines.get(0);
                matrix[x][y] = lines.get(1);
                matrix[x][y + 1] = lines.get(2);
            } else {
                if (x - 1 >= 0 && y + 1 < width && matrix[x - 1][y + 1] != 0 && !allowedLineYPositions.get(lines.get(0)).contains(matrix[x - 1][y + 1])) {
                    return false;
                }
                if (y + 1 < width && matrix[x][y + 1] != 0 && !allowedLineYPositions.get(lines.get(1)).contains(matrix[x][y + 1])) {
                    return false;
                }
                if (x + 1 < length && y + 1 < width && matrix[x + 1][y + 1] != 0 && !allowedLineYPositions.get(lines.get(2)).contains(matrix[x + 1][y + 1])) {
                    return false;
                }
                if (x + 2 < length && matrix[x + 2][y] != 0 && !allowedLineYPositions.get(lines.get(2)).contains(matrix[x + 2][y])) {
                    return false;
                }
                matrix[x - 1][y] = lines.get(0);
                matrix[x][y] = lines.get(1);
                matrix[x + 1][y] = lines.get(2);
            }
        }
        return true;
    }

    private boolean checkAllLineCombinations() {
        boolean check = false;
        for (int numCombination = 0; numCombination < width * length; numCombination++) {
            int[][] currentMatrix = Arrays.stream(matrix)
                    .map(int[]::clone)
                    .toArray(int[][]::new);
            List<Integer> lines = numberToLineCombination(width * length, numCombination);
            int index = -1;
            for (int x = 0; x < length; x++) {
                for (int y = 0; y < width; y++) {
                    index++;
                    if (currentMatrix[x][y] != 0) {
                        continue;
                    }
                    currentMatrix[x][y] = lines.get(index);
                }
            }
            for (int x = 0; x < length; x++) {
                for (int y = 0; y < width; y++) {
                    //todo тут нужны флаги и continue/break
                }
            }
        }
        return false;
    }

    private List<Integer> numberToLineCombination(int cellNumber, int numberCombination) {
        List<Integer> linePositions = new ArrayList<>();
        while (numberCombination > 0) {
            linePositions.add(numberCombination % 7);
            numberCombination /= 7;
        }
        while (linePositions.size() < cellNumber) {
            linePositions.add(0);
        }
        return linePositions;
    }
}
