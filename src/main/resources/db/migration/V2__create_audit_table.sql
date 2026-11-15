CREATE TABLE audit_event
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    occurred_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor_id    VARCHAR(100) NOT NULL,
    action      VARCHAR(50)  NOT NULL,
    risk_level  VARCHAR(50)  NOT NULL,
    target_type VARCHAR(100),
    target_id   VARCHAR(100),
    ip_address  INET,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);