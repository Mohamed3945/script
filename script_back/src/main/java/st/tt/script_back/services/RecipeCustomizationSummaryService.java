package st.tt.script_back.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import st.tt.script_back.dto.RecipeCustomizationStatsDto;
import st.tt.script_back.dto.RecipeCustomizationSummaryDto;
import st.tt.script_back.dto.RecipeDto;
import st.tt.script_back.dto.RecipeMatrixCellDto;
import st.tt.script_back.dto.RecipeMatrixColumnDto;
import st.tt.script_back.dto.RecipeMatrixDto;
import st.tt.script_back.dto.RecipeMatrixRowDto;
import st.tt.script_back.dto.RecipeUntouchedModifiableItemDto;
import st.tt.script_back.enums.ActivationState;
import st.tt.script_back.enums.RecipeKind;

/**
 * Calcule un bilan de customisation pour les recettes derivees.
 */
@Service
public class RecipeCustomizationSummaryService {

    private final RecipeQueryService recipeQueryService;

    public RecipeCustomizationSummaryService(RecipeQueryService recipeQueryService) {
        this.recipeQueryService = recipeQueryService;
    }

    @Transactional(readOnly = true)
    public RecipeCustomizationSummaryDto computeForRecipe(Long recipeId, Integer untouchedLimit) {
        RecipeDto recipe = recipeQueryService.getRecipe(recipeId);

        RecipeCustomizationSummaryDto empty = buildEmptySummary(recipe);
        if (recipe.getRecipeKind() != RecipeKind.DERIVED) {
            return empty;
        }

        RecipeMatrixDto matrix = recipeQueryService.getRecipeMatrix(recipeId);
        if (matrix == null || matrix.getRows() == null || matrix.getRows().isEmpty()) {
            return empty;
        }

        Map<Long, RecipeMatrixColumnDto> stepById = new HashMap<>();
        for (RecipeMatrixColumnDto column : matrix.getColumns()) {
            stepById.put(column.getStepId(), column);
        }

        int modifiableCount = 0;
        int touchedCount = 0;
        int untouchedCount = 0;
        int computedExcludedCount = 0;

        List<RecipeUntouchedModifiableItemDto> untouchedItems = new ArrayList<>();

        for (RecipeMatrixRowDto row : matrix.getRows()) {
            if (row.getCells() == null || row.getCells().isEmpty()) {
                continue;
            }

            for (RecipeMatrixCellDto cell : row.getCells()) {
                if (cell == null) {
                    continue;
                }

                // Rule requested: computed cells never count in customization statistics.
                if (cell.isComputed()) {
                    computedExcludedCount += 1;
                    continue;
                }

                if (!isModifiable(cell)) {
                    continue;
                }

                modifiableCount += 1;

                if (cell.isUserModified()) {
                    touchedCount += 1;
                    continue;
                }

                untouchedCount += 1;

                RecipeMatrixColumnDto step = stepById.get(cell.getStepId());
                untouchedItems.add(new RecipeUntouchedModifiableItemDto(
                        cell.getStepId(),
                        step != null ? step.getStepCode() : null,
                        step != null ? step.getStepName() : null,
                        step != null ? step.getStepKind() : null,
                        step != null ? step.getOrderIndex() : null,
                        row.getDefinitionId(),
                        row.getParameterName(),
                        row.getParameterGroup(),
                        row.getParameterGroupOrder(),
                        row.getValueType(),
                        cell.getDisplayValue()));
            }
        }

        untouchedItems.sort(
                Comparator.comparing(RecipeUntouchedModifiableItemDto::getStepOrderIndex,
                        Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(RecipeUntouchedModifiableItemDto::getParameterGroupOrder,
                                Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(RecipeUntouchedModifiableItemDto::getParameterName,
                                Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(RecipeUntouchedModifiableItemDto::getStepCode,
                                Comparator.nullsLast(String::compareToIgnoreCase)));

        int safeLimit = untouchedLimit == null ? 100 : Math.max(1, untouchedLimit);
        List<RecipeUntouchedModifiableItemDto> limitedUntouchedItems = untouchedItems.stream()
                .limit(safeLimit)
                .toList();

        double customizationRate = modifiableCount == 0
                ? 0d
                : ((double) touchedCount / (double) modifiableCount);

        RecipeCustomizationStatsDto stats = new RecipeCustomizationStatsDto(
                modifiableCount,
                touchedCount,
                untouchedCount,
                computedExcludedCount,
                customizationRate);

        return new RecipeCustomizationSummaryDto(
                recipeId,
                recipe.getParentRecipeId(),
                recipe.getRecipeKind().name(),
                stats,
                limitedUntouchedItems);
    }

    private RecipeCustomizationSummaryDto buildEmptySummary(RecipeDto recipe) {
        return new RecipeCustomizationSummaryDto(
                recipe.getId(),
                recipe.getParentRecipeId(),
                recipe.getRecipeKind() != null ? recipe.getRecipeKind().name() : "UNKNOWN",
                new RecipeCustomizationStatsDto(0, 0, 0, 0, 0d),
                List.of());
    }

    private boolean isModifiable(RecipeMatrixCellDto cell) {
        if (!cell.isEditable()) {
            return false;
        }

        if (cell.isLockedByGolden()) {
            return false;
        }

        // V2 rule: DISABLED cells are not counted as modifiable.
        return cell.getActivationState() == ActivationState.ENABLED;
    }
}
