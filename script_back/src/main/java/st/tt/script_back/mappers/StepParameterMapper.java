package st.tt.script_back.mappers;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import st.tt.script_back.dto.StepParameterDto;
import st.tt.script_back.entities.StepParameter;

/**
 * StepParameterMapper class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Component
public class StepParameterMapper {

    /**
     * Executes toDto.
     *
     * @param parameter input argument consumed by toDto.
     * @return computed StepParameterDto result returned by toDto.
     */
    public StepParameterDto toDto(StepParameter parameter) {
        if (parameter == null) {
            return null;
        }
        return new StepParameterDto(
                parameter.getId(),
                parameter.getStep() != null ? parameter.getStep().getId() : null,
                parameter.getDefinition() != null ? parameter.getDefinition().getId() : null,
                parameter.getDefinition() != null ? parameter.getDefinition().getName() : null,
                parameter.getDefinition() != null && parameter.getDefinition().getParameterGroupRef() != null
                        ? parameter.getDefinition().getParameterGroupRef().getName()
                        : null,
                parameter.getDefinition() != null
                        && parameter.getDefinition().getParameterGroupRef() != null
                        && parameter.getDefinition().getParameterGroupRef().getOrderIndex() != null
                                ? parameter.getDefinition().getParameterGroupRef().getOrderIndex()
                                : 0,
                parameter.getParentStepParameter() != null ? parameter.getParentStepParameter().getId() : null,
                parameter.getParentOrderScope(),
                parameter.getOrderIndex(),
                parameter.getLabelOverride(),
                parameter.getValueJson(),
                parameter.getSelectedOption() != null ? parameter.getSelectedOption().getId() : null,
                parameter.getSelectedOption() != null ? parameter.getSelectedOption().getLabel() : null,
                parameter.getActivationState(),
                parameter.isLockedByGolden(),
                parameter.isUserModified(),
                false
        );
    }

    /**
     * Executes toDtoList.
     *
     * @param parameters input argument consumed by toDtoList.
     * @return computed List<StepParameterDto> result returned by toDtoList.
     */
    public List<StepParameterDto> toDtoList(List<StepParameter> parameters) {
        if (parameters == null) {
            return Collections.emptyList();
        }
        return parameters.stream().map(this::toDto).toList();
    }

    /**
     * Executes toEntity.
     *
     * @param dto input argument consumed by toEntity.
     * @return computed StepParameter result returned by toEntity.
     */
    public StepParameter toEntity(StepParameterDto dto) {
        if (dto == null) {
            return null;
        }
        StepParameter entity = new StepParameter();
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
    public void updateEntityFromDto(StepParameterDto dto, StepParameter entity) {
        if (dto.getOrderIndex() != null) {
            entity.setOrderIndex(dto.getOrderIndex());
        }
        entity.setLabelOverride(dto.getLabelOverride());
        entity.setValueJson(dto.getValueJson());
        entity.setLockedByGolden(dto.isLockedByGolden());
    }
}