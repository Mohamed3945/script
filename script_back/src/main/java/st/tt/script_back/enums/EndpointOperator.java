package st.tt.script_back.enums;

/**
 * Opérateur de comparaison pour une condition d'endpoint Soft Machine.
 * toXmlValue() retourne la chaîne exacte attendue dans l'attribut Condition= du XML.
 */
public enum EndpointOperator {
    EQ,   
    NEQ,  
    LT,   
    LTE,  
    GT,   
    GTE;  

    /**
     * Retourne la valeur XML à écrire dans l'attribut Condition= du tag <CONDITION>.
     * Exemples observés dans les XML Soft Machine : "EQ", "LT", "GT"
     */
    public String toXmlValue() {
        return this.name(); // EQ → "EQ", LT → "LT", etc.
    }
}