package ru.paramonova.models;

import ru.paramonova.dto.Line;
import ru.paramonova.dto.Pipe;
import ru.paramonova.dto.Result;

import java.util.*;

public class PipeMatrix {
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
    private final Map<Integer, List<Integer>> forbiddenLineBorderXPositions = new HashMap<>();
    private final Map<Integer, List<Integer>> forbiddenLineBorderYPositions = new HashMap<>();
    private final long batchId;
    private final int width;
    private final int length;
    private final List<Pipe> allPipes = new ArrayList<>();
    private final List<Pipe> whitePipes = new ArrayList<>();
    private final List<Pipe> blackPipes = new ArrayList<>();
    private final int[][] matrix;

    public PipeMatrix(long batchId, int width, int length, List<Pipe> pipes) {
        this.batchId = batchId;
        this.width = width;
        this.length = length;
        this.allPipes.addAll(pipes);
        for (Pipe pipe : pipes) {
            if (pipe.isColor()) {
                whitePipes.add(pipe);
            } else {
                blackPipes.add(pipe);
            }
        }
        matrix = new int[length][width];
        for (int[] ints : matrix) {
            Arrays.fill(ints, -1);
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

    public Result calculateResult() {
        // проверка, что трубы упираются некорректно в границу
        for (Pipe pipe : whitePipes) {
            if (pipe.getX() == 0 || pipe.getX() == length - 1) {
                if (forbiddenWhitePipeXPositions.get(pipe.getX()).contains(pipe.getPosition())) {
                    return new Result(batchId, false, new ArrayList<>(), new ArrayList<>());
                }
            }
            if (pipe.getY() == 0 || pipe.getY() == width - 1) {
                if (forbiddenWhitePipeYPositions.get(pipe.getY()).contains(pipe.getPosition())) {
                    return new Result(batchId, false, new ArrayList<>(), new ArrayList<>());
                }
            }
        }
        // проверка, что трубы упираются некорректно в границу
        for (Pipe pipe : blackPipes) {
            if ((pipe.getX() == 0 || pipe.getX() == length - 1)
                    && forbiddenBlackPipeXPositions.get(pipe.getX()).contains(pipe.getPosition())) {
                return new Result(batchId, false, new ArrayList<>(), new ArrayList<>());
            }
            if ((pipe.getY() == 0 || pipe.getY() == width - 1)
                    && forbiddenBlackPipeYPositions.get(pipe.getY()).contains(pipe.getPosition())) {
                return new Result(batchId, false, new ArrayList<>(), new ArrayList<>());
            }
        }
        // проверка, что линии из разобранных труб некорректно упираются в границу
        if (!pipeToLine()) {
            return new Result(batchId, false, new ArrayList<>(), new ArrayList<>());
        }
        // перебор всех вариантов линий и проверка возможности соединения их
        return checkAllLineCombinations();
    }

    private boolean pipeToLine() {
        for (Pipe pipe : blackPipes) {
            int x = pipe.getX();
            int y = pipe.getY();
            switch (pipe.getPosition()) {
                case 0:
                    if (y + 1 < width && matrix[x][y + 1] > 0 && !allowedLineYPositions.get(5).contains(matrix[x][y + 1])) {
                        return false;
                    }
                    if (x + 1 < length && matrix[x + 1][y] > 0 && !allowedLineXPositions.get(5).contains(matrix[x + 1][y])) {
                        return false;
                    }
                    if (x - 1 >= 0 && y + 1 < width && matrix[x - 1][y + 1] > 0 && !allowedLineYPositions.get(2).contains(matrix[x - 1][y + 1])) {
                        return false;
                    }
                    if (x + 1 < length && y - 1 >= 0 && matrix[x + 1][y - 1] > 0 && !allowedLineXPositions.get(1).contains(matrix[x + 1][y - 1])) {
                        return false;
                    }
                    matrix[x - 1][y] = 2;
                    matrix[x][y - 1] = 1;
                    matrix[x][y] = 5;
                    break;
                case 1:
                    if (y + 2 < width && matrix[x][y + 2] > 0 && !allowedLineYPositions.get(1).contains(matrix[x][y + 2])) {
                        return false;
                    }
                    if (x + 1 < length && y + 1 < width && matrix[x + 1][y + 1] > 0 && !allowedLineXPositions.get(1).contains(matrix[x + 1][y + 1])) {
                        return false;
                    }
                    if (x - 1 >= 0 && y + 1 < width && matrix[x - 1][y + 1] > 0 && !allowedLineYPositions.get(2).contains(matrix[x - 1][y + 1])) {
                        return false;
                    }
                    if (x + 1 < length && matrix[x + 1][y] > 0 && !allowedLineXPositions.get(4).contains(matrix[x + 1][y])) {
                        return false;
                    }
                    matrix[x - 1][y] = 2;
                    matrix[x][y] = 4;
                    matrix[x][y + 1] = 1;
                    break;
                case 2:
                    if (y + 2 < width && matrix[x][y + 2] > 0 && !allowedLineYPositions.get(1).contains(matrix[x][y + 2])) {
                        return false;
                    }
                    if (x + 1 < length && y + 1 < width && matrix[x + 1][y + 1] > 0 && !allowedLineXPositions.get(1).contains(matrix[x + 1][y + 1])) {
                        return false;
                    }
                    if (x + 1 < length && y + 1 < width && matrix[x + 1][y + 1] > 0 && !allowedLineYPositions.get(2).contains(matrix[x + 1][y + 1])) {
                        return false;
                    }
                    if (x + 2 < length && matrix[x + 2][y] > 0 && !allowedLineXPositions.get(2).contains(matrix[x + 2][y])) {
                        return false;
                    }
                    matrix[x][y] = 3;
                    matrix[x][y + 1] = 1;
                    matrix[x + 1][y] = 2;
                    break;
                default:
                    if (y - 1 >= 0 && x + 1 < length && matrix[x + 1][y - 1] > 0 && !allowedLineXPositions.get(1).contains(matrix[x + 1][y - 1])) {
                        return false;
                    }
                    if (y + 1 < width && matrix[x][y + 1] > 0 && !allowedLineYPositions.get(6).contains(matrix[x][y + 1])) {
                        return false;
                    }
                    if (x + 1 < length && y + 1 < width && matrix[x + 1][y + 1] > 0 && !allowedLineYPositions.get(2).contains(matrix[x + 1][y + 1])) {
                        return false;
                    }
                    if (x + 2 < length && matrix[x + 2][y] > 0 && !allowedLineXPositions.get(2).contains(matrix[x + 2][y])) {
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
                if (y - 1 >= 0 && x + 1 < length && matrix[x + 1][y - 1] > 0 && !allowedLineXPositions.get(lines.get(0)).contains(matrix[x + 1][y - 1])) {
                    return false;
                }
                if (x + 1 < length && matrix[x + 1][y] > 0 && !allowedLineXPositions.get(lines.get(1)).contains(matrix[x + 1][y])) {
                    return false;
                }
                if (y + 1 < width && x + 1 < length && matrix[x + 1][y + 1] > 0 && !allowedLineXPositions.get(lines.get(2)).contains(matrix[x + 1][y + 1])) {
                    return false;
                }
                if (y + 2 < width && matrix[x][y + 2] > 0 && !allowedLineYPositions.get(lines.get(2)).contains(matrix[x][y + 2])) {
                    return false;
                }
                matrix[x][y - 1] = lines.get(0);
                matrix[x][y] = lines.get(1);
                matrix[x][y + 1] = lines.get(2);
            } else {
                if (x - 1 >= 0 && y + 1 < width && matrix[x - 1][y + 1] > 0 && !allowedLineYPositions.get(lines.get(0)).contains(matrix[x - 1][y + 1])) {
                    return false;
                }
                if (y + 1 < width && matrix[x][y + 1] > 0 && !allowedLineYPositions.get(lines.get(1)).contains(matrix[x][y + 1])) {
                    return false;
                }
                if (x + 1 < length && y + 1 < width && matrix[x + 1][y + 1] > 0 && !allowedLineYPositions.get(lines.get(2)).contains(matrix[x + 1][y + 1])) {
                    return false;
                }
                if (x + 2 < length && matrix[x + 2][y] > 0 && !allowedLineXPositions.get(lines.get(2)).contains(matrix[x + 2][y])) {
                    return false;
                }
                matrix[x - 1][y] = lines.get(0);
                matrix[x][y] = lines.get(1);
                matrix[x + 1][y] = lines.get(2);
            }
        }
        return true;
    }

    private Result checkAllLineCombinations() {
        int[][] currentMatrix = Arrays.stream(matrix)
                .map(int[]::clone)
                .toArray(int[][]::new);
        Result result = buildLine(currentMatrix);
        if (result != null) {
            return result;
        }
        return new Result(batchId, false, new ArrayList<>(), new ArrayList<>());
    }

    private Result buildLine(int[][] matrix) {
        // ищем необработанную клетку с максимальным кол-вом соседей (хотя бы 1)
        int[] coords = findNextCellWithNeighbor(matrix);
        if (coords == null) {
            if (isConnectedLine(matrix)) {
                List<Line> lines = new ArrayList<>();
                for (int x = 0; x < length; x++) {
                    for (int y = 0; y < width; y++) {
                        lines.add(new Line(x, y, matrix[x][y]));
                    }
                }
                return new Result(batchId, true, allPipes, lines);
            }
            return null;
        }
        int x = coords[0];
        int y = coords[1];
        // смотрим, есть ли хотя бы 1 допустимое положение линии для клетки
        boolean hasValid = false;
        for (int pos = 0; pos <= 6; pos++) {
            if (isValidPlacement(matrix, x, y, pos)) {
                hasValid = true;
                break;
            }
        }
        if (!hasValid) {
            return null;
        }
        // ставим значение позиции линии для клетки и идем рекурсивно строить линию дальше
        for (int pos = 0; pos <= 6; pos++) {
            if (!isValidPlacement(matrix, x, y, pos)) {
                continue;
            }
            matrix[x][y] = pos;
            Result res = buildLine(matrix);
            if (res != null) {
                return res;
            }
            matrix[x][y] = -1;
        }
        return null;
    }

    private int[] findNextCellWithNeighbor(int[][] matrix) {
        int res[] = new int[]{-1, -1};
        int num = 0;
        for (int x = 0; x < length; x++) {
            for (int y = 0; y < width; y++) {
                if (matrix[x][y] == -1) {
                    int currNum = numberNeighbors(matrix, x, y);
                    if (currNum > num) {
                        res[0] = x;
                        res[1] = y;
                        num = currNum;
                    }
                }
            }
        }
        if (num > 0) {
            return res;
        }
        return null;
    }

    private int numberNeighbors(int[][] m, int x, int y) {
        int num = 0;
        if (x > 0 && m[x - 1][y] > 0) num++;
        if (x < length - 1 && m[x + 1][y] > 0) num++;
        if (y > 0 && m[x][y - 1] > 0) num++;
        if (y < width - 1 && m[x][y + 1] > 0) num++;
        return num;
    }

    private boolean isValidPlacement(int[][] m, int x, int y, int pos) {
        // границы
        if (x == 0 && forbiddenLineBorderXPositions.get(0).contains(pos)) return false;
        if (x == length - 1 && forbiddenLineBorderXPositions.get(length - 1).contains(pos)) return false;
        if (y == 0 && forbiddenLineBorderYPositions.get(0).contains(pos)) return false;
        if (y == width - 1 && forbiddenLineBorderYPositions.get(width - 1).contains(pos)) return false;
        // соседи
        if (x > 0 && m[x - 1][y] > 0 &&
                !allowedLineXPositions.get(m[x - 1][y]).contains(pos)) return false;
        if (y > 0 && m[x][y - 1] > 0 &&
                !allowedLineYPositions.get(m[x][y - 1]).contains(pos)) return false;
        if (x < length - 1 && m[x + 1][y] > 0 &&
                !allowedLineXPositions.get(pos).contains(m[x + 1][y])) return false;
        if (y < width - 1 && m[x][y + 1] > 0 &&
                !allowedLineYPositions.get(pos).contains(m[x][y + 1])) return false;
        return true;
    }

    private boolean isConnectedLine(int[][] matrix) {
        boolean[][] visited = new boolean[length][width];
        int startX = -1, startY = -1;
        for (int x = 0; x < length; x++) {
            for (int y = 0; y < width; y++) {
                if (matrix[x][y] != 0) {
                    startX = x;
                    startY = y;
                    break;
                }
            }
        }
        if (startX == -1) return false;
        tryMove(matrix, visited, startX, startY);
        for (int x = 0; x < length; x++) {
            for (int y = 0; y < width; y++) {
                if (matrix[x][y] > 0 && !visited[x][y]) {
                    return false;
                }
                if (numConnectionLine(matrix, x, y) != 2) {
                    return false;
                }
            }
        }
        return true;
    }

    private int numConnectionLine(int[][] m, int x, int y) {
        int count = 0;
        int pos = m[x][y];
        if (x > 0 && m[x - 1][y] > 0 &&
                allowedLineXPositions.get(m[x - 1][y]).contains(pos)) count++;
        if (x < length - 1 && m[x + 1][y] > 0 &&
                allowedLineXPositions.get(pos).contains(m[x + 1][y])) count++;
        if (y > 0 && m[x][y - 1] != 0 &&
                allowedLineYPositions.get(m[x][y - 1]).contains(pos)) count++;
        if (y < width - 1 && m[x][y + 1] != 0 &&
                allowedLineYPositions.get(pos).contains(m[x][y + 1])) count++;
        return count;
    }

    private void tryMove(int[][] matrix, boolean[][] visited, int x, int y) {
        visited[x][y] = true;
        int pos = matrix[x][y];
        // вправо
        if (y < width - 1 && !visited[x][y + 1] && matrix[x][y + 1] != 0 &&
                allowedLineYPositions.get(pos).contains(matrix[x][y + 1])) {
            tryMove(matrix, visited, x, y + 1);
            return;
        }
        // вниз
        if (x < length - 1 && !visited[x + 1][y] && matrix[x + 1][y] != 0 &&
                allowedLineXPositions.get(pos).contains(matrix[x + 1][y])) {
            tryMove(matrix, visited, x + 1, y);
            return;
        }
        // влево
        if (y > 0 && !visited[x][y - 1] && matrix[x][y - 1] != 0 &&
                allowedLineYPositions.get(matrix[x][y - 1]).contains(pos)) {
            tryMove(matrix, visited, x, y - 1);
            return;
        }
        // вверх
        if (x > 0 && !visited[x - 1][y] && matrix[x - 1][y] != 0 &&
                allowedLineXPositions.get(matrix[x - 1][y]).contains(pos)) {
            tryMove(matrix, visited, x - 1, y);
        }
    }
}
