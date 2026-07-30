package com.visibleai.brasstacks.task;

import com.visibleai.brasstacks.model.Task;
import com.visibleai.brasstacks.task.dto.*;
import com.visibleai.brasstacks.task.dto.TemplateResponse;
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
    private final TemplateService templateService;

    public TaskController(TaskService taskService, TemplateService templateService) {
        this.taskService = taskService;
        this.templateService = templateService;
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
    public TaskResponse skip(Authentication auth, @PathVariable Long id,
                             @RequestBody(required = false) SkipRequest request) {
        return taskService.skip(getUserId(auth), id, request != null ? request.context() : null);
    }

    @PostMapping("/{id}/snooze")
    public TaskResponse snooze(Authentication auth, @PathVariable Long id,
                               @Valid @RequestBody SnoozeRequest request) {
        return taskService.snooze(getUserId(auth), id, request.until(), request.context());
    }

    @PostMapping("/{id}/unsnooze")
    public TaskResponse unsnooze(Authentication auth, @PathVariable Long id) {
        return taskService.unsnooze(getUserId(auth), id);
    }

    @GetMapping
    public Page<TaskResponse> list(Authentication auth,
                                    @RequestParam(required = false) Task.Status status,
                                    Pageable pageable) {
        return taskService.listTasks(getUserId(auth), status, pageable);
    }

    @PostMapping("/{id}/focus")
    public ResponseEntity<Void> logFocus(Authentication auth, @PathVariable Long id,
                                          @Valid @RequestBody FocusSessionRequest request) {
        taskService.logFocusSession(getUserId(auth), id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/microsteps")
    public java.util.List<TaskResponse.MicroStepResponse> microSteps(Authentication auth, @PathVariable Long id) {
        return taskService.getMicroSteps(getUserId(auth), id);
    }

    @GetMapping("/templates")
    public java.util.List<TemplateResponse> templates(@RequestParam(required = false) String category) {
        if (category != null) {
            return templateService.listByCategory(category);
        }
        return templateService.listTemplates();
    }

    @PostMapping("/{id}/apply-template")
    public java.util.List<TaskResponse.MicroStepResponse> applyTemplate(
            Authentication auth, @PathVariable Long id, @RequestParam String templateId) {
        var template = templateService.getTemplate(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));
        return taskService.applyTemplate(getUserId(auth), id, template.steps());
    }

    private Long getUserId(Authentication auth) {
        return (Long) auth.getCredentials();
    }
}
