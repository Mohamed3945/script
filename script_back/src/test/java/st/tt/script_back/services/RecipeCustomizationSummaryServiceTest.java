package st.tt.script_back.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import st.tt.script_back.dto.RecipeCustomizationSummaryDto;
import st.tt.script_back.dto.RecipeDto;
import st.tt.script_back.dto.RecipeMatrixCellDto;
import st.tt.script_back.dto.RecipeMatrixColumnDto;
import st.tt.script_back.dto.RecipeMatrixDto;
import st.tt.script_back.dto.RecipeMatrixRowDto;
import st.tt.script_back.enums.ActivationState;
import st.tt.script_back.enums.ParameterValueType;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.enums.StepKind;

@ExtendWith(MockitoExtension.class)
class RecipeCustomizationSummaryServiceTest {

    @Mock
    private RecipeQueryService recipeQueryService;

    @InjectMocks
    private RecipeCustomizationSummaryService service;

    @Test
    void computeForRecipe_shouldReturnEmptySummaryForGoldenAndSkipMatrixLookup() {
        RecipeDto recipe = new RecipeDto();
        recipe.setId(10L);
        recipe.setRecipeKind(RecipeKind.GOLDEN);
        recipe.setParentRecipeId(null);

        when(recipeQueryService.getRecipe(10L)).thenReturn(recipe);

        RecipeCustomizationSummaryDto result = service.computeForRecipe(10L, 100);

        assertEquals(0, result.getStats().getModifiableCount());
        assertEquals(0, result.getStats().getTouchedCount());
        assertEquals(0, result.getStats().getUntouchedCount());
        assertEquals(0, result.getUntouchedModifiableItems().size());
        verify(recipeQueryService, never()).getRecipeMatrix(10L);
    }

    @Test
    void computeForRecipe_shouldComputeStatsAndExcludeComputedAndDisabled() {
        RecipeDto recipe = new RecipeDto();
        recipe.setId(46L);
        recipe.setRecipeKind(RecipeKind.DERIVED);
        recipe.setParentRecipeId(12L);

        RecipeMatrixColumnDto stepA = new RecipeMatrixColumnDto(101L, "STEP_01", "Step one", StepKind.STEP, 1);
        RecipeMatrixColumnDto stepB = new RecipeMatrixColumnDto(102L, "STEP_02", "Step two", StepKind.STEP, 2);

        RecipeMatrixCellDto touched = cell(101L, 1001L, true, false, ActivationState.ENABLED, true, false);
        RecipeMatrixCellDto untouched = cell(102L, 1002L, true, false, ActivationState.ENABLED, false, false);
        RecipeMatrixCellDto computed = cell(101L, 1003L, true, false, ActivationState.ENABLED, false, true);
        RecipeMatrixCellDto disabled = cell(102L, 1004L, true, false, ActivationState.DISABLED, false, false);
        RecipeMatrixCellDto locked = cell(102L, 1005L, true, true, ActivationState.ENABLED, false, false);

        RecipeMatrixRowDto row = new RecipeMatrixRowDto(
                501L,
                "Pressure",
                "P",
                "Process",
                2,
                ParameterValueType.NUMBER,
                List.of(touched, untouched, computed, disabled, locked));

        RecipeMatrixDto matrix = new RecipeMatrixDto(46L, List.of(stepA, stepB), List.of(row), List.of());

        when(recipeQueryService.getRecipe(46L)).thenReturn(recipe);
        when(recipeQueryService.getRecipeMatrix(46L)).thenReturn(matrix);

        RecipeCustomizationSummaryDto result = service.computeForRecipe(46L, 100);

        assertEquals(2, result.getStats().getModifiableCount());
        assertEquals(1, result.getStats().getTouchedCount());
        assertEquals(1, result.getStats().getUntouchedCount());
        assertEquals(1, result.getStats().getComputedExcludedCount());
        assertEquals(0.5d, result.getStats().getCustomizationRate());
        assertEquals(1, result.getUntouchedModifiableItems().size());
        assertEquals("STEP_02", result.getUntouchedModifiableItems().get(0).getStepCode());
        assertEquals("Pressure", result.getUntouchedModifiableItems().get(0).getParameterName());
    }

    @Test
    void computeForRecipe_shouldApplySafeLimitAndKeepAtLeastOneItem() {
        RecipeDto recipe = new RecipeDto();
        recipe.setId(47L);
        recipe.setRecipeKind(RecipeKind.DERIVED);

        RecipeMatrixColumnDto stepA = new RecipeMatrixColumnDto(101L, "STEP_01", "Step one", StepKind.STEP, 1);

        RecipeMatrixCellDto untouchedA = cell(101L, 2001L, true, false, ActivationState.ENABLED, false, false);
        RecipeMatrixCellDto untouchedB = cell(101L, 2002L, true, false, ActivationState.ENABLED, false, false);

        RecipeMatrixRowDto rowA = new RecipeMatrixRowDto(
                601L,
                "Alpha",
                "A",
                "Group A",
                1,
                ParameterValueType.NUMBER,
                List.of(untouchedA));

        RecipeMatrixRowDto rowB = new RecipeMatrixRowDto(
                602L,
                "Beta",
                "B",
                "Group B",
                2,
                ParameterValueType.NUMBER,
                List.of(untouchedB));

        RecipeMatrixDto matrix = new RecipeMatrixDto(47L, List.of(stepA), List.of(rowB, rowA), List.of());

        when(recipeQueryService.getRecipe(47L)).thenReturn(recipe);
        when(recipeQueryService.getRecipeMatrix(47L)).thenReturn(matrix);

        RecipeCustomizationSummaryDto result = service.computeForRecipe(47L, 0);

        assertEquals(2, result.getStats().getUntouchedCount());
        assertEquals(1, result.getUntouchedModifiableItems().size());
        assertNotNull(result.getUntouchedModifiableItems().get(0).getParameterName());
    }

    @Test
    void computeForRecipe_shouldSortUntouchedItemsByStepOrderThenGroupThenName() {
        RecipeDto recipe = new RecipeDto();
        recipe.setId(48L);
        recipe.setRecipeKind(RecipeKind.DERIVED);

        RecipeMatrixColumnDto stepB = new RecipeMatrixColumnDto(202L, "STEP_02", "Step two", StepKind.STEP, 2);
        RecipeMatrixColumnDto stepA = new RecipeMatrixColumnDto(201L, "STEP_01", "Step one", StepKind.STEP, 1);

        RecipeMatrixCellDto cellStepB = cell(202L, 3001L, true, false, ActivationState.ENABLED, false, false);
        RecipeMatrixCellDto cellStepA2 = cell(201L, 3002L, true, false, ActivationState.ENABLED, false, false);
        RecipeMatrixCellDto cellStepA1 = cell(201L, 3003L, true, false, ActivationState.ENABLED, false, false);

        RecipeMatrixRowDto rowB = new RecipeMatrixRowDto(
                701L,
                "ZZ-Param",
                "Z",
                "G2",
                2,
                ParameterValueType.NUMBER,
                List.of(cellStepB));

        RecipeMatrixRowDto rowA2 = new RecipeMatrixRowDto(
                702L,
                "B-Param",
                "B",
                "G1",
                1,
                ParameterValueType.NUMBER,
                List.of(cellStepA2));

        RecipeMatrixRowDto rowA1 = new RecipeMatrixRowDto(
                703L,
                "A-Param",
                "A",
                "G1",
                1,
                ParameterValueType.NUMBER,
                List.of(cellStepA1));

        RecipeMatrixDto matrix = new RecipeMatrixDto(48L, List.of(stepB, stepA), List.of(rowB, rowA2, rowA1), List.of());

        when(recipeQueryService.getRecipe(48L)).thenReturn(recipe);
        when(recipeQueryService.getRecipeMatrix(48L)).thenReturn(matrix);

        RecipeCustomizationSummaryDto result = service.computeForRecipe(48L, 100);

        assertEquals(3, result.getUntouchedModifiableItems().size());
        assertEquals("STEP_01", result.getUntouchedModifiableItems().get(0).getStepCode());
        assertEquals("A-Param", result.getUntouchedModifiableItems().get(0).getParameterName());
        assertEquals("STEP_01", result.getUntouchedModifiableItems().get(1).getStepCode());
        assertEquals("B-Param", result.getUntouchedModifiableItems().get(1).getParameterName());
        assertEquals("STEP_02", result.getUntouchedModifiableItems().get(2).getStepCode());
    }

    private RecipeMatrixCellDto cell(
            Long stepId,
            Long stepParameterId,
            boolean editable,
            boolean locked,
            ActivationState activationState,
            boolean userModified,
            boolean computed) {
        return new RecipeMatrixCellDto(
                stepId,
                stepParameterId,
                999L,
                ParameterValueType.NUMBER,
                "42",
                "42",
                null,
                null,
                List.of(),
                activationState,
                locked,
                editable,
                userModified,
                null,
                null,
                false,
                computed);
    }
}
