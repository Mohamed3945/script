package st.tt.script_back.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import st.tt.script_back.dto.ParameterOptionDto;
import st.tt.script_back.dto.RecipeDto;
import st.tt.script_back.dto.RecipeMatrixCellDto;
import st.tt.script_back.dto.RecipeMatrixDto;
import st.tt.script_back.dto.RecipeMatrixEndpointCellDto;
import st.tt.script_back.dto.RecipeMatrixRowDto;
import st.tt.script_back.dto.StepDto;
import st.tt.script_back.dto.StepParameterDto;
import st.tt.script_back.dto.StepParameterGridRowDto;
import st.tt.script_back.entities.ComputationFormula;
import st.tt.script_back.entities.FormulaReference;
import st.tt.script_back.entities.ParameterDefinition;
import st.tt.script_back.entities.ParameterGroup;
import st.tt.script_back.entities.ParameterOption;
import st.tt.script_back.entities.Recipe;
import st.tt.script_back.entities.Step;
import st.tt.script_back.entities.StepEndpoint;
import st.tt.script_back.entities.StepEndpointCondition;
import st.tt.script_back.entities.StepParameter;
import st.tt.script_back.enums.ActivationState;
import st.tt.script_back.enums.EndpointOperator;
import st.tt.script_back.enums.ParameterScope;
import st.tt.script_back.enums.ParameterValueType;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.enums.RecipeStatus;
import st.tt.script_back.enums.RecipeWaferMode;
import st.tt.script_back.enums.RecipeIapcMode;
import st.tt.script_back.enums.RecipeResumableMode;
import st.tt.script_back.enums.StepKind;
import st.tt.script_back.enums.ComputationStatus;
import st.tt.script_back.enums.XmlSection;
import st.tt.script_back.mappers.ParameterOptionMapper;
import st.tt.script_back.mappers.RecipeMapper;
import st.tt.script_back.mappers.StepMapper;
import st.tt.script_back.mappers.StepParameterMapper;
import st.tt.script_back.repositories.ComputationFormulaRepository;
import st.tt.script_back.repositories.ParameterOptionRepository;
import st.tt.script_back.repositories.RecipeRepository;
import st.tt.script_back.repositories.StepEndpointRepository;
import st.tt.script_back.repositories.StepParameterRepository;
import st.tt.script_back.repositories.StepRepository;

@ExtendWith(MockitoExtension.class)
class RecipeQueryServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private StepRepository stepRepository;

    @Mock
    private StepEndpointRepository stepEndpointRepository;

    @Mock
    private StepParameterRepository stepParameterRepository;

    @Mock
    private ComputationFormulaRepository computationFormulaRepository;

    @Mock
    private ParameterOptionRepository parameterOptionRepository;

    @Mock
    private RecipeMapper recipeMapper;

    @Mock
    private ParameterOptionMapper parameterOptionMapper;

    @Mock
    private StepMapper stepMapper;

    @Mock
    private StepParameterMapper stepParameterMapper;

    @InjectMocks
    private RecipeQueryService recipeQueryService;

    private Recipe goldenRecipe;
    private Recipe derivedRecipe;
    private Step sourceStep;
    private Step targetStep;
    private Step secondaryStep;
    private ParameterGroup groupOne;
    private ParameterGroup groupTwo;
    private ParameterDefinition sourceDefinition;
    private ParameterDefinition targetDefinition;
    private ParameterDefinition secondaryDefinition;
    private ParameterDefinition enumDefinition;
    private ParameterOption enumOptionLow;
    private ParameterOption enumOptionHigh;
    private StepParameter sourceParameter;
    private StepParameter targetParameter;
    private StepParameter secondaryParameter;
    private StepParameter enumParameter;

    @BeforeEach
    void setUp() {
        goldenRecipe = recipe(1L, RecipeKind.GOLDEN, null, "Golden");
        derivedRecipe = recipe(2L, RecipeKind.DERIVED, goldenRecipe, "Derived");

        sourceStep = step(11L, derivedRecipe, StepKind.PRESTEP, 0, "SRC", "Source step");
        targetStep = step(12L, derivedRecipe, StepKind.STEP, 1, "TGT1", "Target step 1");
        secondaryStep = step(13L, derivedRecipe, StepKind.STEP, 2, "TGT2", "Target step 2");

        groupOne = group(101L, "Group 1", 1);
        groupTwo = group(102L, "Group 2", 2);

        sourceDefinition = definition(201L, "Source value", "SRC", ParameterValueType.NUMBER, null, 0);
        targetDefinition = definition(202L, "Target value", "TGT", ParameterValueType.NUMBER, groupOne, 1);
        secondaryDefinition = definition(203L, "Secondary value", "SEC", ParameterValueType.NUMBER, groupTwo, 1);
        enumDefinition = definition(204L, "Mode", "MODE", ParameterValueType.ENUM, groupOne, 2);

        enumOptionLow = option(301L, enumDefinition, "LOW");
        enumOptionHigh = option(302L, enumDefinition, "HIGH");

        sourceParameter = parameter(
                401L,
                sourceStep,
                sourceDefinition,
                null,
                null,
                null,
                "5",
            null,
                ActivationState.ENABLED,
                false,
                true,
                null,
                Instant.parse("2026-08-20T00:00:00Z"));

        targetParameter = parameter(
            402L,
            targetStep,
            targetDefinition,
            null,
            null,
            null,
            null,
            null,
            ActivationState.ENABLED,
            false,
            false,
            ComputationStatus.OK,
            Instant.parse("2026-08-20T00:00:01Z"));

        secondaryParameter = parameter(
                403L,
                secondaryStep,
                secondaryDefinition,
                null,
                null,
                null,
                null,
                null,
                ActivationState.ENABLED,
                false,
                false,
                ComputationStatus.OK,
                Instant.parse("2026-08-20T00:00:02Z"));

        enumParameter = parameter(
                404L,
                targetStep,
                enumDefinition,
                null,
                null,
                null,
            null,
            enumOptionLow,
                ActivationState.ENABLED,
                false,
                false,
                null,
                Instant.parse("2026-08-20T00:00:03Z"));
    }

    @Test
    void getRecipes_shouldUseRecipeKindFilter() {
        List<Recipe> recipes = List.of(goldenRecipe, derivedRecipe);
        when(recipeRepository.findByRecipeKindOrderByReviseTimeDesc(RecipeKind.GOLDEN)).thenReturn(recipes);
        when(recipeMapper.toDtoList(recipes)).thenReturn(List.of(recipeDto(1L), recipeDto(2L)));

        List<RecipeDto> result = recipeQueryService.getRecipes(RecipeKind.GOLDEN, null);

        assertEquals(2, result.size());
        verify(recipeRepository).findByRecipeKindOrderByReviseTimeDesc(RecipeKind.GOLDEN);
        verify(recipeRepository, never()).findByRecipeKindNotOrderByReviseTimeDesc(RecipeKind.GOLDEN);
    }

    @Test
    void getRecipes_shouldUseGoldenFlagWhenTrue() {
        List<Recipe> recipes = List.of(goldenRecipe);
        when(recipeRepository.findByRecipeKindOrderByReviseTimeDesc(RecipeKind.GOLDEN)).thenReturn(recipes);
        when(recipeMapper.toDtoList(recipes)).thenReturn(List.of(recipeDto(1L)));

        List<RecipeDto> result = recipeQueryService.getRecipes(null, true);

        assertEquals(1, result.size());
        verify(recipeRepository).findByRecipeKindOrderByReviseTimeDesc(RecipeKind.GOLDEN);
        verify(recipeRepository, never()).findByRecipeKindNotOrderByReviseTimeDesc(RecipeKind.GOLDEN);
    }

    @Test
    void getRecipes_shouldExcludeGoldenWhenFlagIsFalse() {
        List<Recipe> recipes = List.of(derivedRecipe);
        when(recipeRepository.findByRecipeKindNotOrderByReviseTimeDesc(RecipeKind.GOLDEN)).thenReturn(recipes);
        when(recipeMapper.toDtoList(recipes)).thenReturn(List.of(recipeDto(2L)));

        List<RecipeDto> result = recipeQueryService.getRecipes(null, false);

        assertEquals(1, result.size());
        verify(recipeRepository).findByRecipeKindNotOrderByReviseTimeDesc(RecipeKind.GOLDEN);
    }

    @Test
    void getRecipes_shouldUseDefaultSortWhenNoFilterIsProvided() {
        List<Recipe> recipes = List.of(derivedRecipe, goldenRecipe);
        org.springframework.data.domain.Sort expectedSort = org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Direction.DESC,
            "reviseTime");
        when(recipeRepository.findAll(expectedSort)).thenReturn(recipes);
        when(recipeMapper.toDtoList(recipes)).thenReturn(List.of(recipeDto(2L), recipeDto(1L)));

        List<RecipeDto> result = recipeQueryService.getRecipes(null, null);

        assertEquals(2, result.size());
        verify(recipeRepository).findAll(expectedSort);
    }

    @Test
    void getRecipe_shouldReturnMappedRecipe() {
        when(recipeRepository.findById(derivedRecipe.getId())).thenReturn(Optional.of(derivedRecipe));
        RecipeDto dto = recipeDto(derivedRecipe.getId());
        when(recipeMapper.toDto(derivedRecipe)).thenReturn(dto);

        RecipeDto result = recipeQueryService.getRecipe(derivedRecipe.getId());

        assertEquals(derivedRecipe.getId(), result.getId());
        verify(recipeMapper).toDto(derivedRecipe);
    }

    @Test
    void getRecipe_shouldThrowWhenRecipeIsMissing() {
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> recipeQueryService.getRecipe(999L));
    }

    @Test
    void getStep_shouldReturnMappedStep() {
        when(stepRepository.findById(targetStep.getId())).thenReturn(Optional.of(targetStep));
        StepDto dto = stepDto(targetStep.getId());
        when(stepMapper.toDto(targetStep)).thenReturn(dto);

        StepDto result = recipeQueryService.getStep(targetStep.getId());

        assertEquals(targetStep.getId(), result.getId());
        verify(stepMapper).toDto(targetStep);
    }

    @Test
    void getStep_shouldThrowWhenStepIsMissing() {
        when(stepRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> recipeQueryService.getStep(999L));
    }

    @Test
    void getRecipeSteps_shouldReturnMappedStepsAndApplyStepKindFilter() {
        when(recipeRepository.existsById(derivedRecipe.getId())).thenReturn(true);
        List<Step> filteredSteps = List.of(sourceStep);
        when(stepRepository.findByRecipeIdAndStepKindOrderByOrderIndexAsc(derivedRecipe.getId(), StepKind.PRESTEP))
                .thenReturn(filteredSteps);
        when(stepMapper.toDtoList(filteredSteps)).thenReturn(List.of(stepDto(sourceStep.getId())));

        List<StepDto> result = recipeQueryService.getRecipeSteps(derivedRecipe.getId(), StepKind.PRESTEP);

        assertEquals(1, result.size());
        assertEquals(sourceStep.getId(), result.get(0).getId());
    }

    @Test
    void getRecipeSteps_shouldThrowWhenRecipeIsMissing() {
        when(recipeRepository.existsById(999L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> recipeQueryService.getRecipeSteps(999L, null));
    }

    @Test
    void getStepParameters_shouldMarkComputedParameters() {
        when(stepRepository.existsById(targetStep.getId())).thenReturn(true);
        when(stepParameterRepository.findByStepIdWithDefinitionAndSelectedOption(targetStep.getId()))
                .thenReturn(List.of(targetParameter, enumParameter));
        stubGoldenFormulaTargets();

        when(stepParameterMapper.toDto(targetParameter)).thenReturn(stepParameterDto(targetParameter.getId()));
        when(stepParameterMapper.toDto(enumParameter)).thenReturn(stepParameterDto(enumParameter.getId()));

        List<StepParameterDto> result = recipeQueryService.getStepParameters(targetStep.getId());

        assertEquals(2, result.size());
        assertTrue(result.get(0).isComputed());
        assertFalse(result.get(1).isComputed());
    }

    @Test
    void getStepParameter_shouldResolveComputedFlagThroughGoldenAncestor() {
        when(stepParameterRepository.findById(targetParameter.getId())).thenReturn(Optional.of(targetParameter));
        stubRecipeHierarchyForDerivedContext();
        stubGoldenFormulaTargets();
        when(stepParameterMapper.toDto(targetParameter)).thenReturn(stepParameterDto(targetParameter.getId()));

        StepParameterDto result = recipeQueryService.getStepParameter(targetParameter.getId());

        assertTrue(result.isComputed());
        assertEquals(targetParameter.getId(), result.getId());
    }

    @Test
    void getComputedDependents_shouldReturnDirectAndTransitiveDependents() {
        when(stepParameterRepository.findById(sourceParameter.getId())).thenReturn(Optional.of(sourceParameter));
        stubRecipeHierarchyForDerivedContext();
        when(computationFormulaRepository.findByRecipeIdWithReferences(goldenRecipe.getId()))
                .thenReturn(List.of(
                        formula(1001L, goldenRecipe, targetStep.getCode(), String.valueOf(targetDefinition.getId()),
                                sourceStep.getCode(), String.valueOf(sourceDefinition.getId())),
                        formula(1002L, goldenRecipe, secondaryStep.getCode(), String.valueOf(secondaryDefinition.getId()),
                                targetStep.getCode(), String.valueOf(targetDefinition.getId()))));
        when(stepParameterRepository.findByRecipeIdWithStepAndDefinition(derivedRecipe.getId()))
                .thenReturn(List.of(sourceParameter, targetParameter, secondaryParameter));

        List<Long> result = recipeQueryService.getComputedDependentsByStepParameterId(sourceParameter.getId());

        assertEquals(List.of(targetParameter.getId(), secondaryParameter.getId()), result);
    }

    @Test
    void getComputedDependents_shouldReturnEmptyWhenNoFormulaExists() {
        when(stepParameterRepository.findById(sourceParameter.getId())).thenReturn(Optional.of(sourceParameter));
        stubRecipeHierarchyForDerivedContext();
        when(computationFormulaRepository.findByRecipeIdWithReferences(goldenRecipe.getId())).thenReturn(List.of());

        List<Long> result = recipeQueryService.getComputedDependentsByStepParameterId(sourceParameter.getId());

        assertTrue(result.isEmpty());
        verify(stepParameterRepository, never()).findByRecipeIdWithStepAndDefinition(derivedRecipe.getId());
    }

    @Test
    void getRecipeStepParameterGrid_shouldSortAndProjectRows() {
        when(recipeRepository.existsById(derivedRecipe.getId())).thenReturn(true);
        when(stepRepository.findByRecipeIdOrderByOrderIndexAsc(derivedRecipe.getId()))
                .thenReturn(List.of(sourceStep, targetStep, secondaryStep));

        StepParameter nestedSecondary = parameter(
                secondaryParameter.getId(),
                secondaryStep,
                secondaryDefinition,
                sourceParameter,
                401L,
                null,
                null,
            null,
                ActivationState.ENABLED,
                false,
                false,
                ComputationStatus.OK,
                secondaryParameter.getComputedAt());

        when(stepParameterRepository.findByStepIdsWithDefinitionAndSelectedOption(List.of(sourceStep.getId(), targetStep.getId(), secondaryStep.getId())))
                .thenReturn(List.of(nestedSecondary, enumParameter, targetParameter, sourceParameter));

        List<StepParameterGridRowDto> result = recipeQueryService.getRecipeStepParameterGrid(derivedRecipe.getId());

        assertEquals(4, result.size());
        assertEquals(targetDefinition.getId(), result.get(0).getParameterDefinitionId());
        assertEquals(enumDefinition.getId(), result.get(1).getParameterDefinitionId());
        assertEquals(secondaryDefinition.getId(), result.get(2).getParameterDefinitionId());
        assertEquals(sourceDefinition.getId(), result.get(3).getParameterDefinitionId());
        assertEquals("201/203", result.get(2).getParameterDefinitionPath());
        assertEquals(enumOptionLow.getId(), result.get(1).getSelectedOptionId());
    }

    @Test
    void getRecipeMatrix_shouldBuildColumnsRowsAndEndpointSummaries() {
        when(recipeRepository.existsById(derivedRecipe.getId())).thenReturn(true);
        stubRecipeHierarchyForDerivedContext();
        when(stepRepository.findByRecipeIdOrderByOrderIndexAsc(derivedRecipe.getId()))
                .thenReturn(List.of(sourceStep, targetStep, secondaryStep));
        when(stepParameterRepository.findByStepIdsWithDefinitionAndSelectedOption(List.of(sourceStep.getId(), targetStep.getId(), secondaryStep.getId())))
                .thenReturn(List.of(sourceParameter, targetParameter, secondaryParameter, enumParameter));
        stubGoldenFormulaTargets();
        when(stepEndpointRepository.findByStepIdsWithConditions(List.of(sourceStep.getId(), targetStep.getId(), secondaryStep.getId())))
                .thenReturn(List.of(endpointWithSingleSelectedOption(sourceStep), endpointWithMultipleConditions(secondaryStep)));
        when(parameterOptionRepository.findByDefinitionIdOrderByOrderIndexAsc(enumDefinition.getId()))
                .thenReturn(List.of(enumOptionLow, enumOptionHigh));
        when(parameterOptionMapper.toDtoList(List.of(enumOptionLow, enumOptionHigh)))
                .thenReturn(List.of(optionDto(enumOptionLow.getId(), enumOptionLow.getLabel()), optionDto(enumOptionHigh.getId(), enumOptionHigh.getLabel())));

        RecipeMatrixDto result = recipeQueryService.getRecipeMatrix(derivedRecipe.getId());

        assertEquals(derivedRecipe.getId(), result.getRecipeId());
        assertEquals(List.of(sourceStep.getCode(), targetStep.getCode(), secondaryStep.getCode()),
                result.getColumns().stream().map(column -> column.getStepCode()).toList());

        RecipeMatrixRowDto firstRow = result.getRows().get(0);
        assertEquals(targetDefinition.getId(), firstRow.getDefinitionId());
        RecipeMatrixCellDto targetCell = firstRow.getCells().get(1);
        assertTrue(targetCell.isComputed());
        assertTrue(targetCell.isComputedFromModified());
        assertTrue(targetCell.isEditable());

        RecipeMatrixCellDto missingEnumCell = result.getRows().stream()
                .filter(row -> row.getDefinitionId().equals(enumDefinition.getId()))
                .findFirst()
                .orElseThrow()
                .getCells().get(2);
        assertEquals("-", missingEnumCell.getDisplayValue());
        assertEquals(2, missingEnumCell.getAvailableOptions().size());

        RecipeMatrixEndpointCellDto sourceEndpoint = result.getEndpointRow().get(0);
        assertEquals(1, sourceEndpoint.getConditionCount());
        assertEquals("Temperature EQ HOT", sourceEndpoint.getSummaryLabel());

        RecipeMatrixEndpointCellDto targetEndpoint = result.getEndpointRow().get(1);
        assertEquals("Time", targetEndpoint.getSummaryLabel());

        RecipeMatrixEndpointCellDto secondaryEndpoint = result.getEndpointRow().get(2);
        assertEquals(2, secondaryEndpoint.getConditionCount());
        assertEquals("2 conditions", secondaryEndpoint.getSummaryLabel());
    }

    private void stubRecipeHierarchyForDerivedContext() {
        when(recipeRepository.findById(derivedRecipe.getId())).thenReturn(Optional.of(derivedRecipe));
        when(recipeRepository.findById(goldenRecipe.getId())).thenReturn(Optional.of(goldenRecipe));
    }

    private void stubGoldenFormulaTargets() {
        when(recipeRepository.findById(derivedRecipe.getId())).thenReturn(Optional.of(derivedRecipe));
        when(recipeRepository.findById(goldenRecipe.getId())).thenReturn(Optional.of(goldenRecipe));
        when(computationFormulaRepository.findByRecipeIdWithReferences(goldenRecipe.getId()))
                .thenReturn(List.of(
                        formula(1001L, goldenRecipe, targetStep.getCode(), String.valueOf(targetDefinition.getId()),
                                sourceStep.getCode(), String.valueOf(sourceDefinition.getId())),
                        formula(1002L, goldenRecipe, secondaryStep.getCode(), String.valueOf(secondaryDefinition.getId()),
                                targetStep.getCode(), String.valueOf(targetDefinition.getId()))));
    }

    private Recipe recipe(Long id, RecipeKind kind, Recipe parent, String name) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setRecipeKind(kind);
        recipe.setParentRecipe(parent);
        recipe.setName(name);
        recipe.setStatus(RecipeStatus.DRAFT);
        recipe.setVersion(1);
        recipe.setFrozen(false);
        recipe.setWafer(RecipeWaferMode.PRESENT);
        recipe.setIapc(RecipeIapcMode.NO);
        recipe.setResumable(RecipeResumableMode.NO);
        recipe.setAccessDisplayGroups("ALL");
        recipe.setAccessModifyGroups("ALL");
        return recipe;
    }

    private Step step(Long id, Recipe recipe, StepKind stepKind, Integer orderIndex, String code, String name) {
        Step step = new Step();
        step.setId(id);
        step.setRecipe(recipe);
        step.setStepKind(stepKind);
        step.setOrderIndex(orderIndex);
        step.setCode(code);
        step.setName(name);
        return step;
    }

    private ParameterGroup group(Long id, String name, Integer orderIndex) {
        ParameterGroup group = new ParameterGroup();
        group.setId(id);
        group.setName(name);
        group.setOrderIndex(orderIndex);
        return group;
    }

    private ParameterDefinition definition(
            Long id,
            String name,
            String alias,
            ParameterValueType valueType,
            ParameterGroup group,
            Integer orderIndexInGroup) {
        ParameterDefinition definition = new ParameterDefinition();
        definition.setId(id);
        definition.setName(name);
        definition.setAlias(alias);
        definition.setCode(alias);
        definition.setValueType(valueType);
        definition.setParameterGroupRef(group);
        definition.setOrderIndexInGroup(orderIndexInGroup);
        definition.setStepType(ParameterScope.STEP);
        definition.setXmlSection(XmlSection.REGULAR);
        return definition;
    }

    private ParameterOption option(Long id, ParameterDefinition definition, String label) {
        ParameterOption option = new ParameterOption();
        option.setId(id);
        option.setDefinition(definition);
        option.setLabel(label);
        option.setCode(label);
        option.setOrderIndex(0);
        return option;
    }

    private StepParameter parameter(
            Long id,
            Step step,
            ParameterDefinition definition,
            StepParameter parent,
            Long parentOrderScope,
            String labelOverride,
            String valueJson,
            ParameterOption selectedOption,
            ActivationState activationState,
            boolean lockedByGolden,
            boolean userModified,
            ComputationStatus computationStatus,
            Instant computedAt) {
        StepParameter parameter = new StepParameter();
        parameter.setId(id);
        parameter.setStep(step);
        parameter.setDefinition(definition);
        parameter.setParentStepParameter(parent);
        parameter.setParentOrderScope(parentOrderScope == null ? 0L : parentOrderScope);
        parameter.setOrderIndex(0);
        parameter.setLabelOverride(labelOverride);
        parameter.setValueJson(valueJson);
        parameter.setSelectedOption(selectedOption);
        parameter.setActivationState(activationState);
        parameter.setLockedByGolden(lockedByGolden);
        parameter.setUserModified(userModified);
        parameter.setComputationStatus(computationStatus);
        parameter.setComputedAt(computedAt);
        return parameter;
    }

    private RecipeDto recipeDto(Long id) {
        RecipeDto dto = new RecipeDto();
        dto.setId(id);
        return dto;
    }

    private StepDto stepDto(Long id) {
        StepDto dto = new StepDto();
        dto.setId(id);
        return dto;
    }

    private StepParameterDto stepParameterDto(Long id) {
        StepParameterDto dto = new StepParameterDto();
        dto.setId(id);
        return dto;
    }

    private ParameterOptionDto optionDto(Long id, String label) {
        ParameterOptionDto dto = new ParameterOptionDto();
        dto.setId(id);
        dto.setLabel(label);
        return dto;
    }

    private ComputationFormula formula(
            Long id,
            Recipe recipe,
            String targetStepCode,
            String targetDefinitionPath,
            String sourceStepCode,
            String sourceDefinitionPath) {
        ComputationFormula formula = new ComputationFormula();
        formula.setId(id);
        formula.setRecipe(recipe);
        formula.setTargetStepCode(targetStepCode);
        formula.setTargetDefinitionPath(targetDefinitionPath);
        formula.setExpression("{1}");
        FormulaReference reference = new FormulaReference();
        reference.setSlot(1);
        reference.setStepCode(sourceStepCode);
        reference.setDefinitionPath(sourceDefinitionPath);
        formula.setReferences(List.of(reference));
        return formula;
    }

    private StepEndpoint endpointWithSingleSelectedOption(Step step) {
        ParameterDefinition endpointDefinition = new ParameterDefinition();
        endpointDefinition.setId(501L);
        endpointDefinition.setName("Temperature");
        endpointDefinition.setAlias("Temperature");
        endpointDefinition.setCode("TEMP");
        endpointDefinition.setStepType(ParameterScope.ENDPOINT);
        endpointDefinition.setValueType(ParameterValueType.ENUM);
        endpointDefinition.setXmlSection(XmlSection.REGULAR);

        ParameterOption selectedOption = new ParameterOption();
        selectedOption.setId(601L);
        selectedOption.setDefinition(endpointDefinition);
        selectedOption.setLabel("HOT");
        selectedOption.setCode("HOT");

        StepEndpointCondition condition = new StepEndpointCondition();
        condition.setId(701L);
        condition.setEndpointParameter(endpointDefinition);
        condition.setSelectedOption(selectedOption);
        condition.setOperator(EndpointOperator.EQ);
        condition.setOrderIndex(1);

        StepEndpoint endpoint = new StepEndpoint();
        endpoint.setId(801L);
        endpoint.setStep(step);
        endpoint.setClause("AND");
        endpoint.setLockedByGolden(false);
        endpoint.setConditions(List.of(condition));
        return endpoint;
    }

    private StepEndpoint endpointWithMultipleConditions(Step step) {
        ParameterDefinition endpointDefinition = new ParameterDefinition();
        endpointDefinition.setId(502L);
        endpointDefinition.setName("Pressure");
        endpointDefinition.setAlias("Pressure");
        endpointDefinition.setCode("PRES");
        endpointDefinition.setStepType(ParameterScope.ENDPOINT);
        endpointDefinition.setValueType(ParameterValueType.NUMBER);
        endpointDefinition.setXmlSection(XmlSection.REGULAR);

        StepEndpointCondition firstCondition = new StepEndpointCondition();
        firstCondition.setId(702L);
        firstCondition.setEndpointParameter(endpointDefinition);
        firstCondition.setValueJson("10");
        firstCondition.setOperator(EndpointOperator.GTE);
        firstCondition.setOrderIndex(2);

        StepEndpointCondition secondCondition = new StepEndpointCondition();
        secondCondition.setId(703L);
        secondCondition.setEndpointParameter(endpointDefinition);
        secondCondition.setValueJson("5");
        secondCondition.setOperator(EndpointOperator.LTE);
        secondCondition.setOrderIndex(1);

        StepEndpoint endpoint = new StepEndpoint();
        endpoint.setId(802L);
        endpoint.setStep(step);
        endpoint.setClause("OR");
        endpoint.setLockedByGolden(true);
        endpoint.setConditions(List.of(firstCondition, secondCondition));
        return endpoint;
    }
}