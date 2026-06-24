package st.tt.script_back.mappers;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import st.tt.script_back.dto.ParameterOptionDto;
import st.tt.script_back.entities.ParameterDefinition;
import st.tt.script_back.entities.ParameterOption;

@Component
public class ParameterOptionMapper {

    public ParameterOptionDto toDto(ParameterOption option) {
        if (option == null) {
            return null;
        }
        return new ParameterOptionDto(
                option.getId(),
                option.getDefinition() != null ? option.getDefinition().getId() : null,
            option.getCode(),
                option.getLabel(),
                option.getOrderIndex()
        );
    }

    public List<ParameterOptionDto> toDtoList(List<ParameterOption> options) {
        if (options == null) {
            return Collections.emptyList();
        }
        return options.stream().map(this::toDto).toList();
    }

    public ParameterOption toEntity(ParameterOptionDto dto) {
        if (dto == null) {
            return null;
        }
        ParameterOption entity = new ParameterOption();
        entity.setId(dto.getId());
        updateEntityFromDto(dto, entity);
        return entity;
    }

    public void updateEntityFromDto(ParameterOptionDto dto, ParameterOption entity) {
        entity.setDefinition(toDefinitionRef(dto.getDefinitionId()));
        entity.setCode(dto.getCode());
        entity.setLabel(dto.getLabel());
        entity.setOrderIndex(dto.getOrderIndex());
    }

    private ParameterDefinition toDefinitionRef(Long id) {
        if (id == null) {
            return null;
        }
        ParameterDefinition definition = new ParameterDefinition();
        definition.setId(id);
        return definition;
    }
}
