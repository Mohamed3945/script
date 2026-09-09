ALTER TABLE parameter_definition
    ADD CONSTRAINT chk_parameter_definition_step_type
    CHECK (step_type IN ('STEP', 'PRESTEP', 'ENDPOINT'));

ALTER TABLE parameter_group
    ADD CONSTRAINT chk_parameter_group_step_type
    CHECK (step_type IN ('STEP', 'PRESTEP', 'ENDPOINT'));

CREATE TABLE IF NOT EXISTS step_endpoint (
    id BIGINT NOT NULL AUTO_INCREMENT,
    step_id BIGINT NOT NULL,
    clause VARCHAR(8) NULL,
    create_time DATETIME(6) NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_step_endpoint_step UNIQUE (step_id),
    CONSTRAINT fk_step_endpoint_step FOREIGN KEY (step_id) REFERENCES step (id),
    CONSTRAINT chk_step_endpoint_clause CHECK (clause IS NULL OR clause IN ('AND', 'OR'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS step_endpoint_condition (
    id BIGINT NOT NULL AUTO_INCREMENT,
    endpoint_id BIGINT NOT NULL,
    endpoint_parameter_id BIGINT NOT NULL,
    value_json LONGTEXT NULL,
    selected_option_id BIGINT NULL,
    operator VARCHAR(8) NOT NULL,
    order_index INT NOT NULL,
    create_time DATETIME(6) NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_endpoint_condition_order UNIQUE (endpoint_id, order_index),
    CONSTRAINT fk_endpoint_condition_endpoint FOREIGN KEY (endpoint_id) REFERENCES step_endpoint (id),
    CONSTRAINT fk_endpoint_condition_parameter FOREIGN KEY (endpoint_parameter_id) REFERENCES parameter_definition (id),
    CONSTRAINT fk_endpoint_condition_selected_option FOREIGN KEY (selected_option_id) REFERENCES parameter_option (id),
    CONSTRAINT chk_endpoint_condition_xor CHECK ((value_json IS NULL) <> (selected_option_id IS NULL)),
    CONSTRAINT chk_endpoint_condition_operator CHECK (operator IN ('EQ', 'NEQ', 'LT', 'LTE', 'GT', 'GTE'))
) ENGINE=InnoDB;

CREATE INDEX idx_step_endpoint_step_id ON step_endpoint (step_id);
CREATE INDEX idx_endpoint_condition_endpoint_id ON step_endpoint_condition (endpoint_id);
CREATE INDEX idx_endpoint_condition_parameter_id ON step_endpoint_condition (endpoint_parameter_id);
CREATE INDEX idx_endpoint_condition_selected_option_id ON step_endpoint_condition (selected_option_id);