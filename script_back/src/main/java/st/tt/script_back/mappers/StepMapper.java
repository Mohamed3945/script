package st.tt.script_back.mappers;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import st.tt.script_back.dto.StepDetailDto;
import st.tt.script_back.dto.StepDto;
import st.tt.script_back.dto.StepParameterDto;
import st.tt.script_back.entities.Step;

/**
 * StepMapper class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Component
public class StepMapper {

    /**
     * Executes toDto.
     *
     * @param step input argument consumed by toDto.
     * @return computed StepDto result returned by toDto.
     */
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

    /**
     * Executes toDetailDto.
     *
     * @param step input argument consumed by toDetailDto.
     * @param parameters input argument consumed by toDetailDto.
     * @return computed StepDetailDto result returned by toDetailDto.
     */
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

    /**
     * Executes toDtoList.
     *
     * @param steps input argument consumed by toDtoList.
     * @return computed List<StepDto> result returned by toDtoList.
     */
    public List<StepDto> toDtoList(List<Step> steps) {
        if (steps == null) {
            return Collections.emptyList();
        }
        return steps.stream().map(this::toDto).toList();
    }

    /**
     * Executes toEntity.
     *
     * @param dto input argument consumed by toEntity.
     * @return computed Step result returned by toEntity.
     */
    public Step toEntity(StepDto dto) {
        if (dto == null) {
            return null;
        }
        Step entity = new Step();
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
    public void updateEntityFromDto(StepDto dto, Step entity) {
        entity.setCode(dto.getCode());
        entity.setStepKind(dto.getStepKind());
        entity.setOrderIndex(dto.getOrderIndex());
        entity.setName(dto.getName());
    }
}
