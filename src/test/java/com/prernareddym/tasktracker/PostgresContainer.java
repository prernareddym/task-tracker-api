package com.prernareddym.tasktracker;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.io.IOException;

/**
 * Starts one real PostgreSQL process per test JVM (no Docker daemon required)
 * and shares it between all integration tests.
 */
final class PostgresContainer {

    private static final EmbeddedPostgres INSTANCE = start();

    private PostgresContainer() {
    }

    private static EmbeddedPostgres start() {
        try {
            EmbeddedPostgres pg = EmbeddedPostgres.builder().start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    pg.close();
                } catch (IOException ignored) {
                    // best effort on shutdown
                }
            }));
            return pg;
        } catch (IOException e) {
            throw new IllegalStateException("Could not start embedded PostgreSQL", e);
        }
    }

    static String jdbcUrl() {
        return INSTANCE.getJdbcUrl("postgres", "postgres");
    }
}
