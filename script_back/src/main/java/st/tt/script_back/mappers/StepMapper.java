package st.tt.script_back.mappers;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import st.tt.script_back.dto.StepDetailDto;
import st.tt.script_back.dto.StepDto;
import st.tt.script_back.dto.StepParameterDto;
import st.tt.script_back.entities.Step;

@Component
public class StepMapper {

    public StepDto toDto(Step step) {
        if (step == null) {
            return null;
        }
        return new StepDto(
                step.getId(),
                step.getRecipe() != null ? step.getRecipe().getId() : null,
            step.getCode(),
                step.getStepKind(),
                step.getOrderIndex(),
                step.getName()
        );
    }

    public StepDetailDto toDetailDto(Step step, List<StepParameterDto> parameters) {
        if (step == null) {
            return null;
        }
        return new StepDetailDto(
                step.getId(),
                step.getRecipe() != null ? step.getRecipe().getId() : null,
                step.getStepKind(),
                step.getOrderIndex(),
                step.getName(),
                parameters == null ? Collections.emptyList() : parameters
        );
    }

    public List<StepDto> toDtoList(List<Step> steps) {
        if (steps == null) {
            return Collections.emptyList();
        }
        return steps.stream().map(this::toDto).toList();
    }

    public Step toEntity(StepDto dto) {
        if (dto == null) {
            return null;
        }
        Step entity = new Step();
        entity.setId(dto.getId());
        updateEntityFromDto(dto, entity);
        return entity;
    }

    public void updateEntityFromDto(StepDto dto, Step entity) {
        entity.setCode(dto.getCode());
        entity.setStepKind(dto.getStepKind());
        entity.setOrderIndex(dto.getOrderIndex());
        entity.setName(dto.getName());
    }
}
