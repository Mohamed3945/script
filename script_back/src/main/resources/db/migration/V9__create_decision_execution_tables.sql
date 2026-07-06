CREATE TABLE decision_execution (
    id BIGINT NOT NULL AUTO_INCREMENT,
    result_profile_id BIGINT NOT NULL,
    validated_golden_recipe_id BIGINT NOT NULL,
    selected_machine_id BIGINT NOT NULL,
    created_derived_recipe_id BIGINT NULL,
    creator_id BIGINT NOT NULL,
    create_time DATETIME(6) NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_de_result_profile
        FOREIGN KEY (result_profile_id) REFERENCES decision_result_profile(id),
    CONSTRAINT fk_de_validated_golden_recipe
        FOREIGN KEY (validated_golden_recipe_id) REFERENCES recipe(id),
    CONSTRAINT fk_de_created_derived_recipe
        FOREIGN KEY (created_derived_recipe_id) REFERENCES recipe(id)
) ENGINE=InnoDB;

CREATE TABLE decision_execution_answer (
    id BIGINT NOT NULL AUTO_INCREMENT,
    decision_execution_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    order_index INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_dea_execution
        FOREIGN KEY (decision_execution_id) REFERENCES decision_execution(id),
    CONSTRAINT fk_dea_question
        FOREIGN KEY (question_id) REFERENCES decision_question(id),
    CONSTRAINT fk_dea_option
        FOREIGN KEY (option_id) REFERENCES decision_option(id),
    CONSTRAINT uq_dea_execution_question UNIQUE (decision_execution_id, question_id)
) ENGINE=InnoDB;

CREATE INDEX idx_de_result_profile_id ON decision_execution (result_profile_id);
CREATE INDEX idx_de_validated_golden_recipe_id ON decision_execution (validated_golden_recipe_id);
CREATE INDEX idx_de_created_derived_recipe_id ON decision_execution (created_derived_recipe_id);
CREATE INDEX idx_dea_execution_id ON decision_execution_answer (decision_execution_id);
CREATE INDEX idx_dea_question_id ON decision_execution_answer (question_id);
CREATE INDEX idx_dea_option_id ON decision_execution_answer (option_id);
