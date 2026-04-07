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
        List<Result> allResults = new ArrayList<>();
        long startB = batch.getStartBlackCombination();
        long endB = startB + batch.getNumberBlackCombinations();
        long startW = batch.getStartWhiteCombination();
        long endW = startW + batch.getNumberWhiteCombinations();
        int blackCirclesNumber = task.getBlackCircles().size();
        int whiteCirclesNumber = task.getWhiteCircles().size();

        List<BatchChunkThread> threads = new ArrayList<>();
        int numThreads = Runtime.getRuntime().availableProcessors();
        long sizeB = (endB - startB) / numThreads;
        for (int i = 0; i < numThreads; i++) {
            long startBT = startB + i * sizeB;
            long endBT = startBT + sizeB;
            threads.add(new BatchChunkThread(task, batch, whiteCirclesNumber, blackCirclesNumber,
                    startW, endW, startBT, endBT));
            threads.get(i).start();
        }
        for (int i = 0; i < numThreads; i++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                throw new RuntimeException("Возникла проблема в потоке калькулятора " + e);
            }
            allResults.addAll(threads.get(i).results);
        }
        return allResults;
    }
}