package st.tt.script_back.mappers;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import st.tt.script_back.dto.StepParameterDto;
import st.tt.script_back.entities.StepParameter;

@Component
public class StepParameterMapper {

    public StepParameterDto toDto(StepParameter parameter) {
        if (parameter == null) {
            return null;
        }
        return new StepParameterDto(
                parameter.getId(),
                parameter.getStep() != null ? parameter.getStep().getId() : null,
                parameter.getDefinition() != null ? parameter.getDefinition().getId() : null,
                parameter.getParentStepParameter() != null ? parameter.getParentStepParameter().getId() : null,
                parameter.getParentOrderScope(),
                parameter.getOrderIndex(),
                parameter.getLabelOverride(),
                parameter.getValueJson(),
                parameter.getSelectedOption() != null ? parameter.getSelectedOption().getId() : null,
                parameter.getActivationState(),
                parameter.isLockedByGolden()
        );
    }

    public List<StepParameterDto> toDtoList(List<StepParameter> parameters) {
        if (parameters == null) {
            return Collections.emptyList();
        }
        return parameters.stream().map(this::toDto).toList();
    }

    public StepParameter toEntity(StepParameterDto dto) {
        if (dto == null) {
            return null;
        }
        StepParameter entity = new StepParameter();
        entity.setId(dto.getId());
        updateEntityFromDto(dto, entity);
        return entity;
    }

    public void updateEntityFromDto(StepParameterDto dto, StepParameter entity) {
        entity.setOrderIndex(dto.getOrderIndex());
        entity.setLabelOverride(dto.getLabelOverride());
        entity.setValueJson(dto.getValueJson());
        entity.setLockedByGolden(dto.isLockedByGolden());
    }
}
