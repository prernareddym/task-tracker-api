CREATE TABLE tasks (
    id          BIGSERIAL PRIMARY KEY,
    owner_id    BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title       VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    status      VARCHAR(20)  NOT NULL DEFAULT 'TODO',
    priority    VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    due_date    DATE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT tasks_status_chk   CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE')),
    CONSTRAINT tasks_priority_chk CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH'))
);

CREATE INDEX idx_tasks_owner_status ON tasks (owner_id, status);
