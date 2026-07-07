ALTER TABLE step_parameter
    ADD COLUMN user_modified BIT(1) NOT NULL DEFAULT b'0' AFTER locked_by_golden;
