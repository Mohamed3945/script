package st.tt.script_back.enums;

/**
 * Détermine dans quelle section XML un ParameterDefinition est écrit.
 *
 * REGULAR        → <PARAMS_STEP><REGULAR><PARAM .../>
 * GAS_PANEL      → <PARAMS_STEP><GAS_PANEL><PARAM .../>
 * STEP_ATTRIBUTE → attribut direct du tag <STEP ChLocation="..." Mode="..." MaxTime="..."/>
 *                  jamais écrit comme <PARAM>
 */
public enum XmlSection {
    REGULAR,
    GAS_PANEL,
    STEP_ATTRIBUTE
}