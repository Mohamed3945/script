ALTER TABLE step_endpoint
    ADD COLUMN locked_by_golden TINYINT(1) NOT NULL DEFAULT 0;
