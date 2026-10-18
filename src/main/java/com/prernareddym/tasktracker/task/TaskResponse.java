package com.prernareddym.tasktracker.task;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Instant createdAt,
        Instant updatedAt) {

    static TaskResponse from(Task t) {
        return new TaskResponse(t.getId(), t.getTitle(), t.getDescription(), t.getStatus(), t.getPriority(),
                t.getDueDate(), t.getCreatedAt(), t.getUpdatedAt());
    }
}
