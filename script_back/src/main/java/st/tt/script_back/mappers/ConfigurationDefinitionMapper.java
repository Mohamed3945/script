package st.tt.script_back.mappers;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import st.tt.script_back.dto.ConfigurationDefinitionDetailDto;
import st.tt.script_back.dto.ConfigurationDefinitionDto;
import st.tt.script_back.entities.ConfigurationDefinition;

@Component
public class ConfigurationDefinitionMapper {

    public ConfigurationDefinitionDto toDto(ConfigurationDefinition definition) {
        if (definition == null) {
            return null;
        }
        return new ConfigurationDefinitionDto(
                definition.getId(),
                definition.getCode(),
                definition.getName(),
                definition.getValueType(),
                definition.getUnit(),
                definition.getQuestionForForm(),
                definition.getQuestionGroup(),
                definition.getDisplayOrder(),
                definition.isActive()
        );
    }

    public ConfigurationDefinitionDetailDto toDetailDto(ConfigurationDefinition definition) {
        if (definition == null) {
            return null;
        }
        return new ConfigurationDefinitionDetailDto(
                definition.getId(),
                definition.getCode(),
                definition.getName(),
                definition.getValueType(),
                definition.getUnit(),
                definition.getQuestionForForm(),
                definition.getQuestionGroup(),
                definition.getDisplayOrder(),
                definition.isActive()
        );
    }

    public List<ConfigurationDefinitionDto> toDtoList(List<ConfigurationDefinition> definitions) {
        if (definitions == null) {
            return Collections.emptyList();
        }
        return definitions.stream().map(this::toDto).toList();
    }

    public ConfigurationDefinition toEntity(ConfigurationDefinitionDto dto) {
        if (dto == null) {
            return null;
        }
        ConfigurationDefinition entity = new ConfigurationDefinition();
        entity.setId(dto.getId());
        updateEntityFromDto(dto, entity);
        return entity;
    }

    public void updateEntityFromDto(ConfigurationDefinitionDto dto, ConfigurationDefinition entity) {
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setValueType(dto.getValueType());
        entity.setUnit(dto.getUnit());
        entity.setQuestionForForm(dto.getQuestionForForm());
        entity.setQuestionGroup(dto.getQuestionGroup());
        entity.setDisplayOrder(dto.getDisplayOrder());
        entity.setActive(dto.isActive());
    }
}
