package st.tt.script_back.mappers;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import st.tt.script_back.dto.ComputationFormulaDto;
import st.tt.script_back.dto.FormulaReferenceDto;
import st.tt.script_back.entities.ComputationFormula;
import st.tt.script_back.entities.FormulaReference;

@Component
public class ComputationFormulaMapper {

    public ComputationFormulaDto toDto(ComputationFormula formula) {
        if (formula == null) {
            return null;
        }

        return new ComputationFormulaDto(
                formula.getId(),
                formula.getRecipe() != null ? formula.getRecipe().getId() : null,
                formula.getTargetStepCode(),
                formula.getTargetDefinitionPath(),
                formula.getExpression(),
                formula.getRoundingMode(),
                formula.getDecimals(),
                formula.getLabel(),
                toReferenceDtoList(formula.getReferences())
        );
    }

    public List<ComputationFormulaDto> toDtoList(List<ComputationFormula> formulas) {
        if (formulas == null) {
            return Collections.emptyList();
        }
        return formulas.stream().map(this::toDto).toList();
    }

    public ComputationFormula toEntity(ComputationFormulaDto dto) {
        if (dto == null) {
            return null;
        }
        ComputationFormula entity = new ComputationFormula();
        entity.setId(dto.getId());
        updateEntityFromDto(dto, entity);
        return entity;
    }

    public void updateEntityFromDto(ComputationFormulaDto dto, ComputationFormula entity) {
        entity.setTargetStepCode(dto.getTargetStepCode());
        entity.setTargetDefinitionPath(dto.getTargetDefinitionPath());
        entity.setExpression(dto.getExpression());
        entity.setRoundingMode(dto.getRoundingMode());
        entity.setDecimals(dto.getDecimals());
        entity.setLabel(dto.getLabel());
    }

    public FormulaReference toReferenceEntity(FormulaReferenceDto dto) {
        if (dto == null) {
            return null;
        }
        FormulaReference entity = new FormulaReference();
        entity.setId(dto.getId());
        entity.setSlot(dto.getSlot());
        entity.setStepCode(dto.getStepCode());
        entity.setDefinitionPath(dto.getDefinitionPath());
        return entity;
    }

    public FormulaReferenceDto toReferenceDto(FormulaReference reference) {
        if (reference == null) {
            return null;
        }
        return new FormulaReferenceDto(
                reference.getId(),
                reference.getSlot(),
                reference.getStepCode(),
                reference.getDefinitionPath()
        );
    }

    public List<FormulaReferenceDto> toReferenceDtoList(List<FormulaReference> references) {
        if (references == null) {
            return Collections.emptyList();
        }
        return references.stream().map(this::toReferenceDto).toList();
    }
}