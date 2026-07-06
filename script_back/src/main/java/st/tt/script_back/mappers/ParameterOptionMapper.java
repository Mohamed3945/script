package st.tt.script_back.mappers;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import st.tt.script_back.dto.ParameterOptionDto;
import st.tt.script_back.entities.ParameterDefinition;
import st.tt.script_back.entities.ParameterOption;

/**
 * ParameterOptionMapper class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Component
public class ParameterOptionMapper {

    /**
     * Executes toDto.
     *
     * @param option input argument consumed by toDto.
     * @return computed ParameterOptionDto result returned by toDto.
     */
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

    /**
     * Executes toDtoList.
     *
     * @param options input argument consumed by toDtoList.
     * @return computed List<ParameterOptionDto> result returned by toDtoList.
     */
    public List<ParameterOptionDto> toDtoList(List<ParameterOption> options) {
        if (options == null) {
            return Collections.emptyList();
        }
        return options.stream().map(this::toDto).toList();
    }

    /**
     * Executes toEntity.
     *
     * @param dto input argument consumed by toEntity.
     * @return computed ParameterOption result returned by toEntity.
     */
    public ParameterOption toEntity(ParameterOptionDto dto) {
        if (dto == null) {
            return null;
        }
        ParameterOption entity = new ParameterOption();
        entity.setId(dto.getId());
        updateEntityFromDto(dto, entity);
        return entity;
    }

    /**
     * Executes updateEntityFromDto.
     *
     * @param dto input argument consumed by updateEntityFromDto.
     * @param entity input argument consumed by updateEntityFromDto.
     */
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
