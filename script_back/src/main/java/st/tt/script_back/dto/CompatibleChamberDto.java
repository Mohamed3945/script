package st.tt.script_back.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompatibleChamberDto {
    private Long chamberId;
    private String chamberCode;
    private String chamberName;
    private List<String> matchedCapabilityCodes;
}
