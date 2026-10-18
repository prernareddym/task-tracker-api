package com.prernareddym.tasktracker.task;

import com.prernareddym.tasktracker.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks")
public class TaskController {

    private static final int MAX_PAGE_SIZE = 100;

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List your tasks, newest first, with optional filters")
    public Page<TaskResponse> list(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @Parameter(description = "Case-insensitive title search") @RequestParam(name = "q", required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
        return service.list(user.id(), status, priority, query, pageable);
    }

    @GetMapping("/summary")
    @Operation(summary = "Count your tasks per status")
    public TaskSummary summary(@AuthenticationPrincipal AuthenticatedUser user) {
        return service.summary(user.id());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one of your tasks")
    public TaskResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return service.get(user.id(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a task")
    public TaskResponse create(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody TaskRequest request) {
        return service.create(user.id(), request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task (omitted status/priority keep their current value)")
    public TaskResponse update(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id,
                               @Valid @RequestBody TaskRequest request) {
        return service.update(user.id(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a task")
    public void delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        service.delete(user.id(), id);
    }
}
