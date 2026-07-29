package com.visibleai.attnbuddy.task;

import com.visibleai.attnbuddy.model.LifeDomain;
import com.visibleai.attnbuddy.model.Task;
import com.visibleai.attnbuddy.model.User;
import com.visibleai.attnbuddy.repository.LifeDomainRepository;
import com.visibleai.attnbuddy.repository.TaskRepository;
import com.visibleai.attnbuddy.repository.UserRepository;
import com.visibleai.attnbuddy.task.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final LifeDomainRepository domainRepository;
    private final PrioritizationEngine prioritizationEngine;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository,
                       LifeDomainRepository domainRepository, PrioritizationEngine prioritizationEngine) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.domainRepository = domainRepository;
        this.prioritizationEngine = prioritizationEngine;
    }

    @Transactional
    public TaskResponse createTask(Long userId, TaskRequest request) {
        User user = userRepository.getReferenceById(userId);
        Task task = new Task(user, request.title());
        task.setNotes(request.notes());
        task.setUrgency(request.urgency() != null ? request.urgency() : Task.Urgency.NOT_URGENT);
        task.setImportance(request.importance() != null ? request.importance() : Task.Importance.IMPORTANT);
        task.setDueDate(request.dueDate());

        if (request.domainId() != null) {
            LifeDomain domain = domainRepository.findById(request.domainId())
                    .filter(d -> d.getUser().getId().equals(userId))
                    .orElseThrow(() -> new IllegalArgumentException("Domain not found"));
            task.setDomain(domain);
        }

        return TaskResponse.from(taskRepository.save(task));
    }

    public NowResponse getNextTask(Long userId) {
        List<Task> pending = taskRepository.findPendingTasksForUser(userId, Instant.now());
        long pendingCount = pending.size();

        return prioritizationEngine.pickNext(pending)
                .map(scored -> new NowResponse(TaskResponse.from(scored.task(), scored.score()), pendingCount))
                .orElse(new NowResponse(null, 0));
    }

    @Transactional
    public TaskResponse markDone(Long userId, Long taskId) {
        Task task = getTaskForUser(userId, taskId);
        task.markDone();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse skip(Long userId, Long taskId) {
        Task task = getTaskForUser(userId, taskId);
        task.skip();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse snooze(Long userId, Long taskId, Instant until) {
        Task task = getTaskForUser(userId, taskId);
        task.snooze(until);
        return TaskResponse.from(taskRepository.save(task));
    }

    public Page<TaskResponse> listTasks(Long userId, Task.Status status, Pageable pageable) {
        Page<Task> tasks = status != null
                ? taskRepository.findByUserIdAndStatus(userId, status, pageable)
                : taskRepository.findByUserId(userId, pageable);
        return tasks.map(TaskResponse::from);
    }

    private Task getTaskForUser(Long userId, Long taskId) {
        return taskRepository.findById(taskId)
                .filter(t -> t.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
    }
}
