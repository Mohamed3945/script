UPDATE parameter_definition
SET xml_section = 'GAS_PANEL'
WHERE xml_section = 'PASSPORT';

ALTER TABLE parameter_definition
    DROP CONSTRAINT chk_parameter_definition_xml_section;

ALTER TABLE parameter_definition
    ADD CONSTRAINT chk_parameter_definition_xml_section
        CHECK (xml_section IN ('REGULAR', 'GAS_PANEL', 'STEP_ATTRIBUTE'));
