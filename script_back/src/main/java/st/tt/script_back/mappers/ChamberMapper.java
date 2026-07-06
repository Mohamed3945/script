package st.tt.script_back.mappers;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import st.tt.script_back.dto.ChamberCapabilityDto;
import st.tt.script_back.dto.ChamberConfigurationDto;
import st.tt.script_back.dto.ChamberDetailDto;
import st.tt.script_back.dto.ChamberDto;
import st.tt.script_back.entities.Chamber;

@Component
public class ChamberMapper {

    public ChamberDto toDto(Chamber chamber) {
        if (chamber == null) {
            return null;
        }
        return new ChamberDto(
                chamber.getId(),
                chamber.getMachine() != null ? chamber.getMachine().getId() : null,
                chamber.getMachine() != null ? chamber.getMachine().getCode() : null,
                chamber.getMachine() != null ? chamber.getMachine().getName() : null,
                chamber.getCode(),
                chamber.getName()
        );
    }

    public ChamberDetailDto toDetailDto(
            Chamber chamber,
            List<ChamberCapabilityDto> capabilities,
            List<ChamberConfigurationDto> configurations) {
        if (chamber == null) {
            return null;
        }
        return new ChamberDetailDto(
                chamber.getId(),
                chamber.getMachine() != null ? chamber.getMachine().getId() : null,
                chamber.getMachine() != null ? chamber.getMachine().getCode() : null,
                chamber.getMachine() != null ? chamber.getMachine().getName() : null,
                chamber.getCode(),
                chamber.getName(),
                capabilities == null ? Collections.emptyList() : capabilities,
                configurations == null ? Collections.emptyList() : configurations
        );
    }

    public List<ChamberDto> toDtoList(List<Chamber> chambers) {
        if (chambers == null) {
            return Collections.emptyList();
        }
        return chambers.stream().map(this::toDto).toList();
    }

    public Chamber toEntity(ChamberDto dto) {
        if (dto == null) {
            return null;
        }
        Chamber entity = new Chamber();
        entity.setId(dto.getId());
        updateEntityFromDto(dto, entity);
        return entity;
    }

    public void updateEntityFromDto(ChamberDto dto, Chamber entity) {
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
    }
}
