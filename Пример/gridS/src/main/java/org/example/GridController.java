package org.example;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/task")
public class GridController {
    int x;

    @PostMapping
    public int addTask(Task task){
        return 1;
    }

    @GetMapping
    public SubTask getSubTask(@RequestParam("taskId") int taskId){
        return new SubTask(x++);
    }
}
