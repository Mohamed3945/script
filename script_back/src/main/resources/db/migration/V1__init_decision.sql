CREATE TABLE decision_question (
    id BIGINT NOT NULL AUTO_INCREMENT,
    active BIT(1) NOT NULL,
    code VARCHAR(255) NOT NULL,
    create_time DATETIME(6) NOT NULL,
    is_entry_point BIT(1) NOT NULL,
    label VARCHAR(255) NOT NULL,
    order_index INT(11) NOT NULL,
    question_type ENUM('BOOLEAN','INFO','SINGLE_CHOICE') NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_decision_question_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE decision_option (
    id BIGINT NOT NULL AUTO_INCREMENT,
    create_time DATETIME(6) NOT NULL,
    label VARCHAR(255) NOT NULL,
    order_index INT(11) NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    value VARCHAR(255) NOT NULL,
    question_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_decision_option_question_id (question_id),
    CONSTRAINT fk_decision_option_question
        FOREIGN KEY (question_id) REFERENCES decision_question(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE decision_result_profile (
    id BIGINT NOT NULL AUTO_INCREMENT,
    active BIT(1) NOT NULL,
    code VARCHAR(255) NOT NULL,
    create_time DATETIME(6) NOT NULL,
    description TEXT DEFAULT NULL,
    golden_recipe_id BIGINT DEFAULT NULL,
    machine_id BIGINT DEFAULT NULL,
    revise_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_decision_result_profile_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE decision_transition (
    id BIGINT NOT NULL AUTO_INCREMENT,
    create_time DATETIME(6) NOT NULL,
    revise_time DATETIME(6) NOT NULL,
    current_question_id BIGINT NOT NULL,
    next_question_id BIGINT DEFAULT NULL,
    option_id BIGINT NOT NULL,
    result_profile_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_decision_transition_current_question_id (current_question_id),
    KEY idx_decision_transition_next_question_id (next_question_id),
    KEY idx_decision_transition_option_id (option_id),
    KEY idx_decision_transition_result_profile_id (result_profile_id),
    CONSTRAINT fk_decision_transition_next_question
        FOREIGN KEY (next_question_id) REFERENCES decision_question(id),
    CONSTRAINT fk_decision_transition_result_profile
        FOREIGN KEY (result_profile_id) REFERENCES decision_result_profile(id),
    CONSTRAINT fk_decision_transition_current_question
        FOREIGN KEY (current_question_id) REFERENCES decision_question(id),
    CONSTRAINT fk_decision_transition_option
        FOREIGN KEY (option_id) REFERENCES decision_option(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

INSERT INTO decision_question
(id, active, code, create_time, is_entry_point, label, order_index, question_type, revise_time)
VALUES
(1, b'1', 'PROCESS_TYPE', NOW(6), b'1', 'Quelle est votre type de process de base ISSG!?!', 1, 'SINGLE_CHOICE', NOW(6)),
(2, b'1', 'PROCESS_TEMP', NOW(6), b'0', 'Température de process?!', 2, 'SINGLE_CHOICE', NOW(6)),
(3, b'1', 'PROCESS_LEVEL', NOW(6), b'0', 'Niveau de process ?', 3, 'SINGLE_CHOICE', NOW(6)),
(4, b'1', 'RESISTIVITY_HIGH_TEMP', NOW(6), b'0', 'Niveau de résistivité ?', 4, 'SINGLE_CHOICE', NOW(6)),
(5, b'1', 'RESISTIVITY_LOW_TEMP_NON_CRITIQUE', NOW(6), b'0', 'Niveau de résistivité ?', 5, 'SINGLE_CHOICE', NOW(6)),
(6, b'1', 'ANNEX_QUESTION', NOW(6), b'0', 'Question annexe', 6, 'INFO', NOW(6)),
(7, b'1', 'RESISTIVITY_LOW_TEMP_CRITIQUE', NOW(6), b'0', 'Niveau de résistivité ?', 5, 'SINGLE_CHOICE', NOW(6));

INSERT INTO decision_option
(id, create_time, label, order_index, revise_time, value, question_id)
VALUES
(1, NOW(6), 'ISSG', 1, NOW(6), 'ISSG', 1),
(2, NOW(6), '> 950', 1, NOW(6), 'GT_950', 2),
(3, NOW(6), '<= 950', 2, NOW(6), 'LE_950', 2),
(4, NOW(6), 'Pas dans la liste', 1, NOW(6), 'NON_CRITIQUE', 3),
(5, NOW(6), 'Critique', 2, NOW(6), 'CRITIQUE', 3),
(6, NOW(6), 'Substrate Resistivity>10mOhm.cm', 1, NOW(6), 'GT_N', 4),
(7, NOW(6), 'Substrate Resistivity<=10mOhm.cm', 2, NOW(6), 'LE_N', 4),
(8, NOW(6), 'Substrate Resistivity>10mOhm.cm', 1, NOW(6), 'GT_N', 5),
(9, NOW(6), 'Substrate Resistivity<=10mOhm.cm', 2, NOW(6), 'LE_N', 5),
(10, NOW(6), 'Question annexe', 1, NOW(6), 'ANNEX_DONE', 6),
(11, NOW(6), 'Substrate Resistivity>10mOhm.cm', 1, NOW(6), 'GT_N', 7),
(12, NOW(6), 'Substrate Resistivity<=10mOhm.cm', 2, NOW(6), 'LE_N', 7);

INSERT INTO decision_result_profile
(id, active, code, create_time, description, golden_recipe_id, machine_id, revise_time)
VALUES
(1, b'1', 'RES_GOLDEN_NON_CRITIQUE_OLM_HIGH', NOW(6), 'Golden_Non_Critique_OLM + High_Temp_OLM', 8, 1, NOW(6)),
(2, b'1', 'RES_GOLDEN_NON_CRITIQUE_OLT_HIGH', NOW(6), 'Golden_Non_Critique_OLT + High_Temp_OLT', 8, 1, NOW(6)),
(3, b'1', 'RES_GOLDEN_NON_CRITIQUE_OLM_LOW', NOW(6), 'Golden_Non_Critique_OLM + Low_Temp_OLM', 8, 1, NOW(6)),
(4, b'1', 'RES_GOLDEN_NON_CRITIQUE_OLT_LOW', NOW(6), 'Golden_Non_Critique_OLT + Low_Temp_OLT', 8, 1, NOW(6)),
(5, b'1', 'RES_GOLDEN_CRITIQUE_OLM_LOW', NOW(6), 'Golden_Critique_OLM + Low_Temp_LPC_OLM', 8, 1, NOW(6)),
(7, b'1', 'RES_GOLDEN_CRITIQUE_OLT_LPC', NOW(6), 'Golden_Critique_OLT + Low_Temp_LPC_OLT', 8, 1, NOW(6));

INSERT INTO decision_transition
(id, create_time, revise_time, current_question_id, next_question_id, option_id, result_profile_id)
VALUES
(1, NOW(6), NOW(6), 1, 2, 1, NULL),
(2, NOW(6), NOW(6), 2, 4, 2, NULL),
(3, NOW(6), NOW(6), 2, 3, 3, NULL),
(4, NOW(6), NOW(6), 3, 5, 4, NULL),
(5, NOW(6), NOW(6), 3, 7, 5, NULL),
(6, NOW(6), NOW(6), 4, NULL, 6, 1),
(7, NOW(6), NOW(6), 4, NULL, 7, 2),
(8, NOW(6), NOW(6), 5, NULL, 8, 3),
(9, NOW(6), NOW(6), 5, NULL, 9, 4),
(10, NOW(6), NOW(6), 7, NULL, 11, 5),
(11, NOW(6), NOW(6), 7, NULL, 12, 7);

ALTER TABLE decision_question AUTO_INCREMENT = 8;
ALTER TABLE decision_option AUTO_INCREMENT = 13;
ALTER TABLE decision_result_profile AUTO_INCREMENT = 8;
ALTER TABLE decision_transition AUTO_INCREMENT = 12;