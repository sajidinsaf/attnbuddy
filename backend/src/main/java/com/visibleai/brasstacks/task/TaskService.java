package com.visibleai.brasstacks.task;

import com.visibleai.brasstacks.model.*;
import com.visibleai.brasstacks.repository.*;
import com.visibleai.brasstacks.task.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final LifeDomainRepository domainRepository;
    private final DailySessionRepository sessionRepository;
    private final PrioritizationEngine prioritizationEngine;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository,
                       LifeDomainRepository domainRepository, DailySessionRepository sessionRepository,
                       PrioritizationEngine prioritizationEngine) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.domainRepository = domainRepository;
        this.sessionRepository = sessionRepository;
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

        // Micro-step setup
        if (request.parentTaskId() != null) {
            Task parent = getTaskForUser(userId, request.parentTaskId());
            task.setParentTask(parent);
            task.setPosition(taskRepository.findMaxPositionByParentId(parent.getId()) + 1);
            // Inherit domain and urgency/importance from parent
            task.setDomain(parent.getDomain());
            task.setUrgency(parent.getUrgency());
            task.setImportance(parent.getImportance());
        }

        // Sustained task setup
        Task.TaskType type = request.taskType() != null ? request.taskType() : Task.TaskType.ONE_TIME;
        task.setTaskType(type);
        if (type == Task.TaskType.SUSTAINED) {
            if (request.dailyEffortMinutes() == null || request.dailyEffortMinutes() < 1) {
                throw new IllegalArgumentException("Sustained tasks require daily effort minutes");
            }
            task.setDailyEffortMinutes(request.dailyEffortMinutes());
            task.setTargetDate(request.targetDate());
            // Set dueDate to targetDate if not explicitly set
            if (task.getDueDate() == null && request.targetDate() != null) {
                task.setDueDate(request.targetDate());
            }
        }

        task = taskRepository.save(task);

        // Create today's session for sustained tasks
        if (task.isSustained()) {
            ensureTodaySession(task, user);
        }

        return toResponse(task);
    }

    public NowResponse getNextTask(Long userId) {
        // Ensure daily sessions exist for all sustained tasks
        ensureAllTodaySessions(userId);

        List<Task> pending = taskRepository.findPendingTasksForUser(userId, Instant.now());

        // Filter out sustained tasks that already have a completed session today
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        List<Task> eligible = new ArrayList<>();
        for (Task task : pending) {
            if (task.isSustained()) {
                var session = sessionRepository.findByTaskIdAndSessionDate(task.getId(), today);
                if (session.isPresent() && session.get().getStatus() == DailySession.Status.DONE) {
                    continue; // Already did today's session
                }
            }
            eligible.add(task);
        }

        long pendingCount = eligible.size();

        return prioritizationEngine.pickNext(eligible)
                .map(scored -> new NowResponse(toResponse(scored.task(), scored.score()), pendingCount))
                .orElse(new NowResponse(null, 0));
    }

    @Transactional
    public TaskResponse markDone(Long userId, Long taskId) {
        Task task = getTaskForUser(userId, taskId);

        if (task.isSustained()) {
            // Mark today's session as done, not the whole task
            LocalDate today = LocalDate.now(ZoneId.systemDefault());
            DailySession session = sessionRepository.findByTaskIdAndSessionDate(taskId, today)
                    .orElseGet(() -> ensureTodaySession(task, task.getUser()));
            session.markDone();
            sessionRepository.save(session);

            // Check if target date has passed — if so, mark the sustained task complete
            if (task.getTargetDate() != null && Instant.now().isAfter(task.getTargetDate())) {
                task.markDone();
                taskRepository.save(task);
            }
        } else {
            task.markDone();
            taskRepository.save(task);
        }

        // Auto-complete parent if all micro-steps are now done
        if (task.isMicroStep()) {
            autoCompleteParentIfAllDone(task.getParentTask());
        }

        return toResponse(task);
    }

    private void autoCompleteParentIfAllDone(Task parent) {
        long incomplete = taskRepository.countIncompleteMicroSteps(parent.getId());
        if (incomplete == 0) {
            parent.markDone();
            taskRepository.save(parent);
        }
    }

    @Transactional
    public TaskResponse skip(Long userId, Long taskId) {
        Task task = getTaskForUser(userId, taskId);
        task.skip();
        taskRepository.save(task);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse snooze(Long userId, Long taskId, Instant until) {
        Task task = getTaskForUser(userId, taskId);
        task.snooze(until);
        taskRepository.save(task);
        return toResponse(task);
    }

    public List<TaskResponse.MicroStepResponse> getMicroSteps(Long userId, Long taskId) {
        getTaskForUser(userId, taskId); // verify ownership
        return taskRepository.findMicroStepsByParentId(taskId).stream()
                .map(s -> new TaskResponse.MicroStepResponse(s.getId(), s.getTitle(), s.getStatus(), s.getPosition()))
                .toList();
    }

    @Transactional
    public List<TaskResponse.MicroStepResponse> applyTemplate(Long userId, Long taskId, List<String> stepTitles) {
        Task parent = getTaskForUser(userId, taskId);
        User user = parent.getUser();
        int position = taskRepository.findMaxPositionByParentId(parent.getId()) + 1;

        List<TaskResponse.MicroStepResponse> created = new ArrayList<>();
        for (String title : stepTitles) {
            Task step = new Task(user, title);
            step.setParentTask(parent);
            step.setPosition(position++);
            step.setDomain(parent.getDomain());
            step.setUrgency(parent.getUrgency());
            step.setImportance(parent.getImportance());
            step = taskRepository.save(step);
            created.add(new TaskResponse.MicroStepResponse(step.getId(), step.getTitle(), step.getStatus(), step.getPosition()));
        }
        return created;
    }

    public Page<TaskResponse> listTasks(Long userId, Task.Status status, Pageable pageable) {
        Page<Task> tasks = status != null
                ? taskRepository.findByUserIdAndStatus(userId, status, pageable)
                : taskRepository.findByUserId(userId, pageable);
        return tasks.map(this::toResponse);
    }

    private Task getTaskForUser(Long userId, Long taskId) {
        return taskRepository.findById(taskId)
                .filter(t -> t.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
    }

    private DailySession ensureTodaySession(Task task, User user) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        return sessionRepository.findByTaskIdAndSessionDate(task.getId(), today)
                .orElseGet(() -> sessionRepository.save(
                        new DailySession(task, user, today, task.getDailyEffortMinutes())
                ));
    }

    private void ensureAllTodaySessions(Long userId) {
        List<Task> sustained = taskRepository.findPendingTasksForUser(userId, Instant.now()).stream()
                .filter(Task::isSustained)
                .toList();
        User user = sustained.isEmpty() ? null : sustained.get(0).getUser();
        for (Task task : sustained) {
            ensureTodaySession(task, user);
        }
    }

    private TaskResponse toResponse(Task task) {
        return toResponse(task, null);
    }

    private TaskResponse toResponse(Task task, Integer score) {
        TaskResponse.SustainedProgress progress = null;
        if (task.isSustained() && task.getId() != null) {
            int completedSessions = sessionRepository.completedSessionCount(task.getId());
            int totalMinutes = sessionRepository.totalMinutesLogged(task.getId());
            int estimatedTotal = estimateTotalMinutes(task);
            double percent = estimatedTotal > 0 ? Math.min(100.0, (totalMinutes * 100.0) / estimatedTotal) : 0;
            progress = new TaskResponse.SustainedProgress(completedSessions, totalMinutes, estimatedTotal, percent);
        }

        List<TaskResponse.MicroStepResponse> microSteps = null;
        if (!task.isMicroStep()) {
            List<Task> steps = taskRepository.findMicroStepsByParentId(task.getId());
            if (!steps.isEmpty()) {
                microSteps = steps.stream()
                        .map(s -> new TaskResponse.MicroStepResponse(s.getId(), s.getTitle(), s.getStatus(), s.getPosition()))
                        .toList();
            }
        }

        return TaskResponse.from(task, score, progress, microSteps);
    }

    private int estimateTotalMinutes(Task task) {
        if (task.getDailyEffortMinutes() == null) return 0;
        if (task.getTargetDate() == null) return task.getDailyEffortMinutes() * 14; // default 2 weeks
        long days = Duration.between(task.getCreatedAt(), task.getTargetDate()).toDays();
        return (int) (days * task.getDailyEffortMinutes());
    }
}
