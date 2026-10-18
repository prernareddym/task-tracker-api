package com.prernareddym.tasktracker.task;

import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

final class TaskSpecifications {

    private TaskSpecifications() {
    }

    static Specification<Task> ownedBy(Long ownerId) {
        return (root, query, cb) -> cb.equal(root.get("ownerId"), ownerId);
    }

    static Specification<Task> hasStatus(TaskStatus status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    static Specification<Task> hasPriority(TaskPriority priority) {
        return priority == null ? null : (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }

    static Specification<Task> titleContains(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String pattern = "%" + text.trim().toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), pattern, '\\');
    }
}
