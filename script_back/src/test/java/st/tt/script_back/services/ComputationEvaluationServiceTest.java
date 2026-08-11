package st.tt.script_back.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import st.tt.script_back.entities.ComputationFormula;
import st.tt.script_back.entities.FormulaReference;
import st.tt.script_back.entities.ParameterDefinition;
import st.tt.script_back.entities.Recipe;
import st.tt.script_back.entities.Step;
import st.tt.script_back.entities.StepParameter;
import st.tt.script_back.enums.ComputationStatus;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.repositories.ComputationFormulaRepository;
import st.tt.script_back.repositories.RecipeRepository;
import st.tt.script_back.repositories.StepParameterRepository;

@ExtendWith(MockitoExtension.class)
class ComputationEvaluationServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private ComputationFormulaRepository computationFormulaRepository;

    @Mock
    private StepParameterRepository stepParameterRepository;

    @InjectMocks
    private ComputationEvaluationService computationEvaluationService;

    @Test
    void recompute_shouldEvaluateInDependencyOrder_whenComputedTargetsDependOnComputedSources() {
        Recipe golden = recipe(100L, RecipeKind.GOLDEN, null);
        when(recipeRepository.findById(100L)).thenReturn(Optional.of(golden));

        ComputationFormula firstFormula = formula(
                1L,
                golden,
                "HEAT1",
                "1",
                "{1} + 1",
                List.of(reference(1, "HEAT1", "2")));

        ComputationFormula secondFormula = formula(
                2L,
                golden,
                "HEAT1",
                "3",
                "{1} * 2",
                List.of(reference(1, "HEAT1", "1")));

        when(computationFormulaRepository.findByRecipeIdWithReferences(100L))
                .thenReturn(List.of(secondFormula, firstFormula));

        StepParameter source = parameter(10L, "HEAT1", 2L, null, "4");
        StepParameter firstTarget = parameter(11L, "HEAT1", 1L, null, null);
        StepParameter secondTarget = parameter(12L, "HEAT1", 3L, null, null);

        List<StepParameter> recipeParameters = List.of(source, firstTarget, secondTarget);
        when(stepParameterRepository.findByRecipeIdWithStepAndDefinition(100L)).thenReturn(recipeParameters);

        computationEvaluationService.recomputeRecipeComputedParameters(100L);

        assertEquals("5", firstTarget.getValueJson());
        assertEquals(ComputationStatus.OK, firstTarget.getComputationStatus());

        assertEquals("10", secondTarget.getValueJson());
        assertEquals(ComputationStatus.OK, secondTarget.getComputationStatus());

        verify(stepParameterRepository).saveAll(eq(recipeParameters));
    }

    @Test
    void recompute_shouldMarkDivZero_whenDivisionByZeroOccurs() {
        Recipe golden = recipe(200L, RecipeKind.GOLDEN, null);
        when(recipeRepository.findById(200L)).thenReturn(Optional.of(golden));

        ComputationFormula formula = formula(
                3L,
                golden,
                "HEAT1",
                "6",
                "{1} / {2}",
                List.of(
                        reference(1, "HEAT1", "4"),
                        reference(2, "HEAT1", "5")));

        when(computationFormulaRepository.findByRecipeIdWithReferences(200L)).thenReturn(List.of(formula));

        StepParameter numerator = parameter(20L, "HEAT1", 4L, null, "10");
        StepParameter denominator = parameter(21L, "HEAT1", 5L, null, "0");
        StepParameter target = parameter(22L, "HEAT1", 6L, null, "99");

        List<StepParameter> recipeParameters = List.of(numerator, denominator, target);
        when(stepParameterRepository.findByRecipeIdWithStepAndDefinition(200L)).thenReturn(recipeParameters);

        computationEvaluationService.recomputeRecipeComputedParameters(200L);

        assertNull(target.getValueJson());
        assertEquals(ComputationStatus.DIV_ZERO, target.getComputationStatus());
        verify(stepParameterRepository).saveAll(eq(recipeParameters));
    }

    @Test
    void recompute_shouldMarkMissingInput_whenReferenceCannotBeResolvedInRecipeContext() {
        Recipe golden = recipe(300L, RecipeKind.GOLDEN, null);
        when(recipeRepository.findById(300L)).thenReturn(Optional.of(golden));

        ComputationFormula formula = formula(
                4L,
                golden,
                "HEAT1",
                "8",
                "{1} + 2",
                List.of(reference(1, "HEAT1", "999")));

        when(computationFormulaRepository.findByRecipeIdWithReferences(300L)).thenReturn(List.of(formula));

        StepParameter target = parameter(30L, "HEAT1", 8L, null, "111");
        List<StepParameter> recipeParameters = List.of(target);
        when(stepParameterRepository.findByRecipeIdWithStepAndDefinition(300L)).thenReturn(recipeParameters);

        computationEvaluationService.recomputeRecipeComputedParameters(300L);

        assertNull(target.getValueJson());
        assertEquals(ComputationStatus.MISSING_INPUT, target.getComputationStatus());
        verify(stepParameterRepository).saveAll(eq(recipeParameters));
    }

    @Test
    void recompute_shouldUseGoldenFormulasButDerivedValues_whenRecipeContextIsDerived() {
        Recipe golden = recipe(400L, RecipeKind.GOLDEN, null);
        Recipe derived = recipe(401L, RecipeKind.DERIVED, golden);

        when(recipeRepository.findById(401L)).thenReturn(Optional.of(derived));
        when(recipeRepository.findById(400L)).thenReturn(Optional.of(golden));

        ComputationFormula formula = formula(
                5L,
                golden,
                "HEAT1",
                "1",
                "{1} / 3",
                List.of(reference(1, "HEAT1", "2")));

        when(computationFormulaRepository.findByRecipeIdWithReferences(400L)).thenReturn(List.of(formula));

        StepParameter derivedSource = parameter(40L, "HEAT1", 2L, null, "6");
        StepParameter derivedTarget = parameter(41L, "HEAT1", 1L, null, null);

        List<StepParameter> derivedParameters = List.of(derivedSource, derivedTarget);
        when(stepParameterRepository.findByRecipeIdWithStepAndDefinition(401L)).thenReturn(derivedParameters);

        computationEvaluationService.recomputeRecipeComputedParameters(401L);

        assertEquals("2", derivedTarget.getValueJson());
        assertEquals(ComputationStatus.OK, derivedTarget.getComputationStatus());
        verify(stepParameterRepository).saveAll(eq(derivedParameters));
    }

    @Test
    void recompute_shouldSkipSave_whenNoFormulaExistsOnGolden() {
        Recipe golden = recipe(500L, RecipeKind.GOLDEN, null);
        when(recipeRepository.findById(500L)).thenReturn(Optional.of(golden));
        when(computationFormulaRepository.findByRecipeIdWithReferences(500L)).thenReturn(List.of());

        computationEvaluationService.recomputeRecipeComputedParameters(500L);

        verify(stepParameterRepository, never()).findByRecipeIdWithStepAndDefinition(500L);
        verify(stepParameterRepository, never()).saveAll(anyList());
    }

    private Recipe recipe(Long id, RecipeKind kind, Recipe parentRecipe) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setRecipeKind(kind);
        recipe.setParentRecipe(parentRecipe);
        return recipe;
    }

    private FormulaReference reference(int slot, String stepCode, String definitionPath) {
        FormulaReference reference = new FormulaReference();
        reference.setSlot(slot);
        reference.setStepCode(stepCode);
        reference.setDefinitionPath(definitionPath);
        return reference;
    }

    private ComputationFormula formula(
            Long id,
            Recipe recipe,
            String targetStepCode,
            String targetDefinitionPath,
            String expression,
            List<FormulaReference> references) {
        ComputationFormula formula = new ComputationFormula();
        formula.setId(id);
        formula.setRecipe(recipe);
        formula.setTargetStepCode(targetStepCode);
        formula.setTargetDefinitionPath(targetDefinitionPath);
        formula.setExpression(expression);
        formula.setReferences(references);
        return formula;
    }

    private StepParameter parameter(
            Long id,
            String stepCode,
            Long definitionId,
            StepParameter parent,
            String valueJson) {
        Step step = new Step();
        step.setId(id != null ? id + 1000 : null);
        step.setCode(stepCode);

        ParameterDefinition definition = new ParameterDefinition();
        definition.setId(definitionId);

        StepParameter parameter = new StepParameter();
        parameter.setId(id);
        parameter.setStep(step);
        parameter.setDefinition(definition);
        parameter.setParentStepParameter(parent);
        parameter.setValueJson(valueJson);
        return parameter;
    }
}
