package ru.paramonova.mainGridRPC.models;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import ru.paramonova.grpc.Circle;
import ru.paramonova.grpc.Pipe;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
public class GridTask {
    private int id;
    private int fieldWidth; // горизонталь
    private int fieldLength; // вертикаль
    private List<Circle> circles = new ArrayList<>();
    private List<List<Pipe>> combinations = new ArrayList<>();

    @SneakyThrows
    public GridTask(BufferedReader reader, int taskId) {
        this.id = taskId;
        String line;
        if ((line = reader.readLine()) != null) {
            String[] fieldSize = line.split(",");
            if (fieldSize.length != 2) {
                throw new IllegalArgumentException("Неверный формат строки с размерами поля");
            }
            fieldWidth = Integer.parseInt(fieldSize[0].trim());
            fieldLength = Integer.parseInt(fieldSize[1].trim());
        } else {
            throw new IllegalArgumentException("Файл пустой");
        }

        List<Circle> whiteCircles = new ArrayList<>();
        List<Circle> blackCircles = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] values = line.split(",");
            if (values.length != 3) {
                throw new IllegalArgumentException("Неверный формат строки с описанием круга " + line);
            }
            int x = Integer.parseInt(values[0].trim());
            int y = Integer.parseInt(values[1].trim());
            boolean color = "1".equals(values[2].trim());
            if (x < 0 || x >= fieldWidth || y < 0 || y >= fieldLength) {
                throw new IllegalArgumentException(
                        String.format("Круг с координатами (%d, %d) выходит за пределы поля", x, y)
                );
            }
            Circle circle = Circle.newBuilder()
                    .setX(x)
                    .setY(y)
                    .setColor(color)
                    .build();
            this.circles.add(circle);
            if (circle.getColor()) {
                whiteCircles.add(circle);
            } else {
                blackCircles.add(circle);
            }
        }
        createCombinations(whiteCircles, blackCircles);
    }

    public void createCombinations(List<Circle> whiteCircles, List<Circle> blackCircles) {
        List<String> whitePipesPositionCombinations = createPositionCombinations(8, whiteCircles.size());
        List<String> blackPipesPositionCombinations = createPositionCombinations(4, blackCircles.size());
        List<List<Pipe>> whitePipeLists = createPipes(whitePipesPositionCombinations, whiteCircles);
        List<List<Pipe>> blackPipeLists = createPipes(blackPipesPositionCombinations, blackCircles);
        List<List<Pipe>> allPipeCombinations = new ArrayList<>();
        for (List<Pipe> whitePipes : whitePipeLists) {
            for (List<Pipe> blackPipes : blackPipeLists) {
                List<Pipe> combinedList = new ArrayList<>();
                combinedList.addAll(whitePipes);
                combinedList.addAll(blackPipes);
                allPipeCombinations.add(combinedList);
            }
        }
        this.combinations.addAll(allPipeCombinations);
        System.out.printf("Задача: %s, кол-во получившихся комбинаций: %d%n", this.id, allPipeCombinations.size());
    }

    private List<List<Pipe>> createPipes(List<String> positionCombinations, List<Circle> circles) {
        List<List<Pipe>> pipeList = new ArrayList<>();
        for (String positions : positionCombinations) {
            List<Pipe> pipes = new ArrayList<>();
            for (int i = 0; i < circles.size(); i++) {
                Circle circle = circles.get(i);
                int position = Character.getNumericValue(positions.charAt(i));
                pipes.add(Pipe.newBuilder()
                        .setX(circle.getX())
                        .setY(circle.getY())
                        .setColor(circle.getColor())
                        .setPosition(position)
                        .build());
            }
            pipeList.add(pipes);
        }
        return pipeList;
    }

    private List<String> createPositionCombinations(int alphabetLength, int circlesNumber) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.pow(alphabetLength, circlesNumber); i++) {
            result.add(numberToPositionCombination(alphabetLength, circlesNumber, i));
        }
        return result;
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
