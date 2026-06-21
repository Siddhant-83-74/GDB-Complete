CREATE TABLE security_alerts (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT,
    message    VARCHAR(500),
    alert_date TIMESTAMP
);
