package st.tt.script_back.mappers;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import st.tt.script_back.dto.ChamberConfigurationDto;
import st.tt.script_back.entities.Chamber;
import st.tt.script_back.entities.ChamberConfiguration;
import st.tt.script_back.entities.ConfigurationDefinition;

@Component
public class ChamberConfigurationMapper {

    public ChamberConfigurationDto toDto(ChamberConfiguration configuration) {
        if (configuration == null) {
            return null;
        }
        return new ChamberConfigurationDto(
                configuration.getId(),
                configuration.getChamber() != null ? configuration.getChamber().getId() : null,
                configuration.getChamber() != null ? configuration.getChamber().getCode() : null,
                configuration.getChamber() != null ? configuration.getChamber().getName() : null,
                configuration.getConfigurationDefinition() != null ? configuration.getConfigurationDefinition().getId() : null,
                configuration.getConfigurationDefinition() != null ? configuration.getConfigurationDefinition().getCode() : null,
                configuration.getConfigurationDefinition() != null ? configuration.getConfigurationDefinition().getName() : null,
                configuration.getCode(),
                configuration.getName(),
                configuration.getNominalValue(),
                configuration.getMinValue(),
                configuration.getMaxValue()
        );
    }

    public List<ChamberConfigurationDto> toDtoList(List<ChamberConfiguration> configurations) {
        if (configurations == null) {
            return Collections.emptyList();
        }
        return configurations.stream().map(this::toDto).toList();
    }

    public ChamberConfiguration toEntity(ChamberConfigurationDto dto) {
        if (dto == null) {
            return null;
        }
        ChamberConfiguration entity = new ChamberConfiguration();
        entity.setId(dto.getId());
        updateEntityFromDto(dto, entity);
        return entity;
    }

    public void updateEntityFromDto(ChamberConfigurationDto dto, ChamberConfiguration entity) {
        entity.setChamber(toChamberRef(dto.getChamberId()));
        entity.setConfigurationDefinition(toDefinitionRef(dto.getConfigurationDefinitionId()));
        entity.setCode(dto.getChamberConfigurationCode());
        entity.setName(dto.getChamberConfigurationName());
        entity.setNominalValue(dto.getNominalValue());
        entity.setMinValue(dto.getMinValue());
        entity.setMaxValue(dto.getMaxValue());
    }

    private Chamber toChamberRef(Long id) {
        if (id == null) {
            return null;
        }
        Chamber chamber = new Chamber();
        chamber.setId(id);
        return chamber;
    }

    private ConfigurationDefinition toDefinitionRef(Long id) {
        if (id == null) {
            return null;
        }
        ConfigurationDefinition definition = new ConfigurationDefinition();
        definition.setId(id);
        return definition;
    }
}
