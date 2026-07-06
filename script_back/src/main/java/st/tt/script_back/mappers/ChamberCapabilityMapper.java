package st.tt.script_back.mappers;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import st.tt.script_back.dto.ChamberCapabilityDto;
import st.tt.script_back.entities.ChamberCapability;

@Component
public class ChamberCapabilityMapper {

    public ChamberCapabilityDto toDto(ChamberCapability capability) {
        if (capability == null) {
            return null;
        }
        return new ChamberCapabilityDto(
                capability.getId(),
                capability.getCode(),
                capability.getLabel(),
                capability.getCategory(),
                capability.isActive()
        );
    }

    public List<ChamberCapabilityDto> toDtoList(List<ChamberCapability> capabilities) {
        if (capabilities == null) {
            return Collections.emptyList();
        }
        return capabilities.stream().map(this::toDto).toList();
    }

    public ChamberCapability toEntity(ChamberCapabilityDto dto) {
        if (dto == null) {
            return null;
        }
        ChamberCapability entity = new ChamberCapability();
        entity.setId(dto.getId());
        updateEntityFromDto(dto, entity);
        return entity;
    }

    public void updateEntityFromDto(ChamberCapabilityDto dto, ChamberCapability entity) {
        entity.setCode(dto.getCode());
        entity.setLabel(dto.getLabel());
        entity.setCategory(dto.getCategory());
        entity.setActive(dto.isActive());
    }
}
