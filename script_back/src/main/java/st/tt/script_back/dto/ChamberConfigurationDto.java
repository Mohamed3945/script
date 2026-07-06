package st.tt.script_back.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChamberConfigurationDto {
    private Long id;

    private Long chamberId;
    private String chamberCode;
    private String chamberName;

    private Long configurationDefinitionId;
    private String configurationDefinitionCode;
    private String configurationDefinitionName;

    private String chamberConfigurationCode;
    private String chamberConfigurationName;

    private BigDecimal nominalValue;
    private BigDecimal minValue;
    private BigDecimal maxValue;
}
