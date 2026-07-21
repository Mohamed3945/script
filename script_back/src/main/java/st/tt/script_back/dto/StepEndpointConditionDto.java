package st.tt.script_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.EndpointOperator;
import st.tt.script_back.enums.ParameterValueType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StepEndpointConditionDto {

    private Long id;

    // Paramètre endpoint choisi
    private Long endpointParameterId;

    // Dénormalisé pour l'affichage — readonly, calculé depuis l'entité
    private String endpointParameterAlias;
    private String endpointParameterName;
    private String endpointParameterUnit;
    private ParameterValueType endpointParameterValueType;

    // Valeur — XOR
    private String valueJson;           // NUMBER, STRING
    private Long selectedOptionId;      // ENUM
    private String selectedOptionLabel; // dénormalisé — readonly

    private EndpointOperator operator;
    private Integer orderIndex;
}