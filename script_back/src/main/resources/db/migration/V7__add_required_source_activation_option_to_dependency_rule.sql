ALTER TABLE parameter_dependency_rule
    ADD COLUMN required_source_activation_option_id BIGINT NULL;

ALTER TABLE parameter_dependency_rule
    ADD CONSTRAINT fk_pdr_required_source_activation_option
    FOREIGN KEY (required_source_activation_option_id)
    REFERENCES parameter_option (id);

CREATE INDEX idx_pdr_required_source_activation_option_id
    ON parameter_dependency_rule (required_source_activation_option_id);
