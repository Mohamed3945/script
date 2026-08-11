ALTER TABLE parameter_definition
    ADD COLUMN xml_section VARCHAR(32) NOT NULL DEFAULT 'REGULAR' AFTER step_type;

ALTER TABLE parameter_definition
    ADD CONSTRAINT chk_parameter_definition_xml_section
        CHECK (xml_section IN ('REGULAR', 'PASSPORT'));
