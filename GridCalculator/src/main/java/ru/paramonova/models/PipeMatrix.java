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
    private final int batchId;
    private final int width;
    private final int length;
    private final List<Pipe> allPipes = new ArrayList<>();
    private final List<Pipe> whitePipes = new ArrayList<>();
    private final List<Pipe> blackPipes = new ArrayList<>();
    private final int[][] matrix;

    public PipeMatrix(int batchId, int width, int length, List<Pipe> pipes) {
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

    public Result calculateResult() {
        // проверка, что трубы упираются некорректно в границу
        for (Pipe pipe : whitePipes) {
            if (pipe.getX() == 0 || pipe.getX() == length - 1) {
                if (forbiddenWhitePipeXPositions.get(pipe.getX()).contains(pipe.getPosition())) {
                    return new Result(batchId, false, allPipes, new ArrayList<>());
                }
            }
            if (pipe.getY() == 0 || pipe.getY() == width - 1) {
                if (forbiddenWhitePipeYPositions.get(pipe.getY()).contains(pipe.getPosition())) {
                    return new Result(batchId, false, allPipes, new ArrayList<>());
                }
            }
        }
        // проверка, что трубы упираются некорректно в границу
        for (Pipe pipe : blackPipes) {
            if ((pipe.getX() == 0 || pipe.getX() == length - 1)
                    && forbiddenBlackPipeXPositions.get(pipe.getX()).contains(pipe.getPosition())) {
                return new Result(batchId, false, allPipes, new ArrayList<>());
            }
            if ((pipe.getY() == 0 || pipe.getY() == width - 1)
                    && forbiddenBlackPipeYPositions.get(pipe.getY()).contains(pipe.getPosition())) {
                return new Result(batchId, false, allPipes, new ArrayList<>());
            }
        }
        // проверка, что линии из разобранных труб некорректно упираются в границу
        if (!pipeToLine()) {
            return new Result(batchId, false, allPipes, new ArrayList<>());
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
                    if (y + 1 < width && matrix[x][y + 1] != 0 && !allowedLineYPositions.get(5).contains(matrix[x][y + 1])) {
                        return false;
                    }
                    if (x + 1 < length && matrix[x + 1][y] != 0 && !allowedLineXPositions.get(5).contains(matrix[x + 1][y])) {
                        return false;
                    }
                    if (x - 1 >= 0 && y + 1 < width && matrix[x - 1][y + 1] != 0 && !allowedLineYPositions.get(2).contains(matrix[x - 1][y + 1])) {
                        return false;
                    }
                    if (x + 1 < length && y - 1 >= 0 && matrix[x + 1][y - 1] != 0 && !allowedLineXPositions.get(1).contains(matrix[x + 1][y - 1])) {
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
                    if (x + 1 < length && y + 1 < width && matrix[x + 1][y + 1] != 0 && !allowedLineXPositions.get(1).contains(matrix[x + 1][y + 1])) {
                        return false;
                    }
                    if (x - 1 >= 0 && y + 1 < width && matrix[x - 1][y + 1] != 0 && !allowedLineYPositions.get(2).contains(matrix[x - 1][y + 1])) {
                        return false;
                    }
                    if (x + 1 < length && matrix[x + 1][y] != 0 && !allowedLineXPositions.get(4).contains(matrix[x + 1][y])) {
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
                    if (x + 1 < length && y + 1 < width && matrix[x + 1][y + 1] != 0 && !allowedLineXPositions.get(1).contains(matrix[x + 1][y + 1])) {
                        return false;
                    }
                    if (x + 1 < length && y + 1 < width && matrix[x + 1][y + 1] != 0 && !allowedLineYPositions.get(2).contains(matrix[x + 1][y + 1])) {
                        return false;
                    }
                    if (x + 2 < length && matrix[x + 2][y] != 0 && !allowedLineXPositions.get(2).contains(matrix[x + 2][y])) {
                        return false;
                    }
                    matrix[x][y] = 3;
                    matrix[x][y + 1] = 1;
                    matrix[x + 1][y] = 2;
                    break;
                default:
                    if (y - 1 >= 0 && x + 1 < length && matrix[x + 1][y - 1] != 0 && !allowedLineXPositions.get(1).contains(matrix[x + 1][y - 1])) {
                        return false;
                    }
                    if (y + 1 < width && matrix[x][y + 1] != 0 && !allowedLineYPositions.get(6).contains(matrix[x][y + 1])) {
                        return false;
                    }
                    if (x + 1 < length && y + 1 < width && matrix[x + 1][y + 1] != 0 && !allowedLineYPositions.get(2).contains(matrix[x + 1][y + 1])) {
                        return false;
                    }
                    if (x + 2 < length && matrix[x + 2][y] != 0 && !allowedLineXPositions.get(2).contains(matrix[x + 2][y])) {
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
                if (y - 1 >= 0 && x + 1 < length && matrix[x + 1][y - 1] != 0 && !allowedLineXPositions.get(lines.get(0)).contains(matrix[x + 1][y - 1])) {
                    return false;
                }
                if (x + 1 < length && matrix[x + 1][y] != 0 && !allowedLineXPositions.get(lines.get(1)).contains(matrix[x + 1][y])) {
                    return false;
                }
                if (y + 1 < width && x + 1 < length && matrix[x + 1][y + 1] != 0 && !allowedLineXPositions.get(lines.get(2)).contains(matrix[x + 1][y + 1])) {
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
                if (x + 2 < length && matrix[x + 2][y] != 0 && !allowedLineXPositions.get(lines.get(2)).contains(matrix[x + 2][y])) {
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

        Result result = dfs(currentMatrix);

        if (result != null) {
            return result;
        }

        return new Result(batchId, false, allPipes, new ArrayList<>());
    }

    private Result dfs(int[][] matrix) {
        int[] cell = findNextCell(matrix);

        if (cell == null) {
            if (isSingleConnectedComponent(matrix)) {
                return buildResult(matrix);
            }
            return null;
        }

        int x = cell[0];
        int y = cell[1];

        for (int pos = 0; pos <= 6; pos++) {

            if (!isValidPlacement(matrix, x, y, pos)) continue;

            matrix[x][y] = pos;

            if (!checkLocalConsistency(matrix, x, y)) {
                matrix[x][y] = 0;
                continue;
            }

            Result res = dfs(matrix);
            if (res != null) return res;

            matrix[x][y] = 0;
        }

        return null;
    }

    private int[] findNextCell(int[][] matrix) {
        for (int x = 0; x < length; x++) {
            for (int y = 0; y < width; y++) {
                if (matrix[x][y] == 0 && hasNeighbor(matrix, x, y)) {
                    return new int[]{x, y};
                }
            }
        }
        return null;
    }

    private boolean hasNeighbor(int[][] m, int x, int y) {
        return (x > 0 && m[x - 1][y] != 0)
                || (x < length - 1 && m[x + 1][y] != 0)
                || (y > 0 && m[x][y - 1] != 0)
                || (y < width - 1 && m[x][y + 1] != 0);
    }

    private boolean isValidPlacement(int[][] m, int x, int y, int pos) {

        // границы
        if (x == 0 && forbiddenLineBorderXPositions.get(0).contains(pos)) return false;
        if (x == length - 1 && forbiddenLineBorderXPositions.get(length - 1).contains(pos)) return false;
        if (y == 0 && forbiddenLineBorderYPositions.get(0).contains(pos)) return false;
        if (y == width - 1 && forbiddenLineBorderYPositions.get(width - 1).contains(pos)) return false;

        // соседи
        if (x > 0 && m[x - 1][y] != 0 &&
                !allowedLineXPositions.get(m[x - 1][y]).contains(pos)) return false;

        if (y > 0 && m[x][y - 1] != 0 &&
                !allowedLineYPositions.get(m[x][y - 1]).contains(pos)) return false;

        if (x < length - 1 && m[x + 1][y] != 0 &&
                !allowedLineXPositions.get(pos).contains(m[x + 1][y])) return false;

        if (y < width - 1 && m[x][y + 1] != 0 &&
                !allowedLineYPositions.get(pos).contains(m[x][y + 1])) return false;

        return true;
    }

    private boolean checkLocalConsistency(int[][] m, int x, int y) {

        int connections = countConnections(m, x, y);

        // максимум 2 соединения
        if (connections > 2) return false;

        // если уже 1 соединение → должна быть возможность сделать второе
        if (connections == 1 && !canHaveSecondConnection(m, x, y)) {
            return false;
        }

        return true;
    }

    private int countConnections(int[][] m, int x, int y) {
        int count = 0;
        int pos = m[x][y];

        if (x > 0 && m[x - 1][y] != 0 &&
                allowedLineXPositions.get(pos).contains(m[x - 1][y])) count++;

        if (x < length - 1 && m[x + 1][y] != 0 &&
                allowedLineXPositions.get(pos).contains(m[x + 1][y])) count++;

        if (y > 0 && m[x][y - 1] != 0 &&
                allowedLineYPositions.get(pos).contains(m[x][y - 1])) count++;

        if (y < width - 1 && m[x][y + 1] != 0 &&
                allowedLineYPositions.get(pos).contains(m[x][y + 1])) count++;

        return count;
    }

    private boolean canHaveSecondConnection(int[][] m, int x, int y) {

        int pos = m[x][y];

        // проверяем есть ли хотя бы один потенциальный сосед
        if (x > 0 && m[x - 1][y] == 0) return true;
        if (x < length - 1 && m[x + 1][y] == 0) return true;
        if (y > 0 && m[x][y - 1] == 0) return true;
        if (y < width - 1 && m[x][y + 1] == 0) return true;

        return false;
    }

    private boolean isSingleConnectedComponent(int[][] m) {

        boolean[][] visited = new boolean[length][width];
        int startX = -1, startY = -1;

        for (int x = 0; x < length; x++) {
            for (int y = 0; y < width; y++) {
                if (m[x][y] != 0) {
                    startX = x;
                    startY = y;
                    break;
                }
            }
        }

        if (startX == -1) return false;

        dfsVisit(m, visited, startX, startY);

        for (int x = 0; x < length; x++) {
            for (int y = 0; y < width; y++) {
                if (m[x][y] != 0 && !visited[x][y]) {
                    return false;
                }
            }
        }

        return true;
    }

    private void dfsVisit(int[][] m, boolean[][] visited, int x, int y) {
        visited[x][y] = true;

        int pos = m[x][y];

        if (x > 0 && !visited[x - 1][y] && m[x - 1][y] != 0 &&
                allowedLineXPositions.get(pos).contains(m[x - 1][y])) {
            dfsVisit(m, visited, x - 1, y);
        }

        if (x < length - 1 && !visited[x + 1][y] && m[x + 1][y] != 0 &&
                allowedLineXPositions.get(pos).contains(m[x + 1][y])) {
            dfsVisit(m, visited, x + 1, y);
        }

        if (y > 0 && !visited[x][y - 1] && m[x][y - 1] != 0 &&
                allowedLineYPositions.get(pos).contains(m[x][y - 1])) {
            dfsVisit(m, visited, x, y - 1);
        }

        if (y < width - 1 && !visited[x][y + 1] && m[x][y + 1] != 0 &&
                allowedLineYPositions.get(pos).contains(m[x][y + 1])) {
            dfsVisit(m, visited, x, y + 1);
        }
    }

    private Result buildResult(int[][] m) {
        List<Line> lines = new ArrayList<>();

        for (int x = 0; x < length; x++) {
            for (int y = 0; y < width; y++) {
                lines.add(new Line(x, y, m[x][y]));
            }
        }

        return new Result(batchId, true, allPipes, lines);
    }
}
