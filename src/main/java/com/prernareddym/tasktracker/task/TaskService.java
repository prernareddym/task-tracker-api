package com.prernareddym.tasktracker.task;

import com.prernareddym.tasktracker.error.NotFoundException;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** All reads and writes are scoped to the owner, so one user can never see or change another user's tasks. */
@Service
@Transactional
public class TaskService {

    private final TaskRepository tasks;

    public TaskService(TaskRepository tasks) {
        this.tasks = tasks;
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> list(Long ownerId, TaskStatus status, TaskPriority priority, String query, Pageable pageable) {
        Specification<Task> spec = Specification.where(TaskSpecifications.ownedBy(ownerId))
                .and(TaskSpecifications.hasStatus(status))
                .and(TaskSpecifications.hasPriority(priority))
                .and(TaskSpecifications.titleContains(query));
        return tasks.findAll(spec, pageable).map(TaskResponse::from);
    }

    @Transactional(readOnly = true)
    public TaskResponse get(Long ownerId, Long id) {
        return TaskResponse.from(find(ownerId, id));
    }

    public TaskResponse create(Long ownerId, TaskRequest request) {
        Task task = new Task(ownerId, request.title().trim());
        task.update(request.title().trim(), request.description(),
                request.status() != null ? request.status() : TaskStatus.TODO,
                request.priority() != null ? request.priority() : TaskPriority.MEDIUM,
                request.dueDate());
        return TaskResponse.from(tasks.save(task));
    }

    public TaskResponse update(Long ownerId, Long id, TaskRequest request) {
        Task task = find(ownerId, id);
        task.update(request.title().trim(), request.description(),
                request.status() != null ? request.status() : task.getStatus(),
                request.priority() != null ? request.priority() : task.getPriority(),
                request.dueDate());
        return TaskResponse.from(task);
    }

    public void delete(Long ownerId, Long id) {
        tasks.delete(find(ownerId, id));
    }

    @Transactional(readOnly = true)
    public TaskSummary summary(Long ownerId) {
        Map<TaskStatus, Long> counts = new EnumMap<>(TaskStatus.class);
        tasks.countByStatus(ownerId).forEach(c -> counts.put(c.getStatus(), c.getTotal()));
        long todo = counts.getOrDefault(TaskStatus.TODO, 0L);
        long inProgress = counts.getOrDefault(TaskStatus.IN_PROGRESS, 0L);
        long done = counts.getOrDefault(TaskStatus.DONE, 0L);
        return new TaskSummary(todo, inProgress, done, todo + inProgress + done);
    }

    private Task find(Long ownerId, Long id) {
        return tasks.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new NotFoundException("Task " + id + " not found"));
    }
}
