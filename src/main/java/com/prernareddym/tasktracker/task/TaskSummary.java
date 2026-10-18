package com.prernareddym.tasktracker.task;

/** Per-status task counts for the signed-in user. */
public record TaskSummary(long todo, long inProgress, long done, long total) {
}
