package com.visibleai.attnbuddy.task;

import com.visibleai.attnbuddy.model.Task;
import com.visibleai.attnbuddy.task.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(Authentication auth, @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(getUserId(auth), request));
    }

    @GetMapping("/now")
    public NowResponse now(Authentication auth) {
        return taskService.getNextTask(getUserId(auth));
    }

    @PostMapping("/{id}/done")
    public TaskResponse done(Authentication auth, @PathVariable Long id) {
        return taskService.markDone(getUserId(auth), id);
    }

    @PostMapping("/{id}/skip")
    public TaskResponse skip(Authentication auth, @PathVariable Long id) {
        return taskService.skip(getUserId(auth), id);
    }

    @PostMapping("/{id}/snooze")
    public TaskResponse snooze(Authentication auth, @PathVariable Long id,
                               @Valid @RequestBody SnoozeRequest request) {
        return taskService.snooze(getUserId(auth), id, request.until());
    }

    @GetMapping
    public Page<TaskResponse> list(Authentication auth,
                                    @RequestParam(required = false) Task.Status status,
                                    Pageable pageable) {
        return taskService.listTasks(getUserId(auth), status, pageable);
    }

    private Long getUserId(Authentication auth) {
        return (Long) auth.getCredentials();
    }
}
