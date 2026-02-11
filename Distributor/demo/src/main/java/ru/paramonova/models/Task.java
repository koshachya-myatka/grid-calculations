package ru.paramonova.models;

import lombok.Getter;
import lombok.ToString;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
public class Task {
    public int id;
    private String fileName;
    private double fieldWidth; // горизонталь
    private double fieldLength; // вертикаль
    private List<Circle> circles = new ArrayList<>();
    private List<List<Pipe>> combinations = new ArrayList<>();

    public Task(String fileName, int taskId) {
        this.id = taskId;
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым");
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            if ((line = reader.readLine()) != null) {
                String[] fieldSize = line.split(",");
                if (fieldSize.length != 2) {
                    throw new IllegalArgumentException("Неверный формат строки с размерами поля");
                }
                fieldWidth = Double.parseDouble(fieldSize[0].trim());
                fieldLength = Double.parseDouble(fieldSize[1].trim());
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
                double x = Double.parseDouble(values[0].trim());
                double y = Double.parseDouble(values[1].trim());
                boolean color = "1".equals(values[2].trim());
                if (x < 0 || x >= fieldWidth || y < 0 || y >= fieldLength) {
                    throw new IllegalArgumentException(
                            String.format("Круг с координатами (%.0f, %.0f) выходит за пределы поля", x, y)
                    );
                }
                Circle circle = new Circle(x, y, color);
                this.circles.add(circle);
                if (circle.isColor()) {
                    whiteCircles.add(circle);
                } else {
                    blackCircles.add(circle);
                }
            }
            createCombinations(whiteCircles, blackCircles);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении файла: " + fileName);
        }
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
        System.out.println("Кол-во получившихся комбинаций: " + allPipeCombinations.size());
    }

    private List<List<Pipe>> createPipes(List<String> positionCombinations, List<Circle> circles) {
        List<List<Pipe>> pipeList = new ArrayList<>();
        for (String positions : positionCombinations) {
            List<Pipe> pipes = new ArrayList<>();
            for (int i = 0; i < circles.size(); i++) {
                Circle circle = circles.get(i);
                int position = Character.getNumericValue(positions.charAt(i));
                pipes.add(new Pipe(circle.getX(), circle.getY(), circle.isColor(), position));
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
