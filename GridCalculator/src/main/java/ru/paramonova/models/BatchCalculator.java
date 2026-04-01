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
        for (PipeList pipeList : batch.getCombinations()) {
            Result currRes = calculateCombination(batch, task, pipeList.getPipes());
            if (currRes.isConnected()) {
                results.add(currRes);
            }
        }
        return results;
    }

    private Result calculateCombination(Batch batch, Task task, List<Pipe> pipes) {
        PipeMatrix pipeMatrix = new PipeMatrix(batch.getBatchId(), task.getFieldWidth(),
                task.getFieldLength(), pipes);
        return pipeMatrix.calculateResult();
    }
}