package st.tt.script_back.mappers;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import st.tt.script_back.dto.ParameterDefinitionDetailDto;
import st.tt.script_back.dto.ParameterDefinitionDto;
import st.tt.script_back.dto.ParameterOptionDto;
import st.tt.script_back.entities.ParameterDefinition;

@Component
public class ParameterDefinitionMapper {

    public ParameterDefinitionDto toDto(ParameterDefinition definition) {
        if (definition == null) {
            return null;
        }
        return new ParameterDefinitionDto(
                definition.getId(),
            definition.getCode(),
                definition.getName(),
                definition.getAlias(),
                definition.getUnit(),
                definition.getDescription(),
                definition.getValueType(),
                definition.isRequiredOnStep(),
                definition.getStepType(),
                definition.getDefaultValueJson()
        );
    }

    public ParameterDefinitionDetailDto toDetailDto(ParameterDefinition definition, List<ParameterOptionDto> options) {
        if (definition == null) {
            return null;
        }
        return new ParameterDefinitionDetailDto(
                definition.getId(),
            definition.getCode(),
                definition.getName(),
                definition.getAlias(),
                definition.getUnit(),
                definition.getDescription(),
                definition.getValueType(),
                definition.isRequiredOnStep(),
                definition.getStepType(),
                definition.getDefaultValueJson(),
                options == null ? Collections.emptyList() : options
        );
    }

    public List<ParameterDefinitionDto> toDtoList(List<ParameterDefinition> definitions) {
        if (definitions == null) {
            return Collections.emptyList();
        }
        return definitions.stream().map(this::toDto).toList();
    }

    public ParameterDefinition toEntity(ParameterDefinitionDto dto) {
        if (dto == null) {
            return null;
        }
        ParameterDefinition entity = new ParameterDefinition();
        entity.setId(dto.getId());
        updateEntityFromDto(dto, entity);
        return entity;
    }

    public void updateEntityFromDto(ParameterDefinitionDto dto, ParameterDefinition entity) {
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setAlias(dto.getAlias());
        entity.setUnit(dto.getUnit());
        entity.setDescription(dto.getDescription());
        entity.setValueType(dto.getValueType());
        entity.setRequiredOnStep(dto.isRequiredOnStep());
        entity.setStepType(dto.getStepType());
        entity.setDefaultValueJson(dto.getDefaultValueJson());
    }
}
