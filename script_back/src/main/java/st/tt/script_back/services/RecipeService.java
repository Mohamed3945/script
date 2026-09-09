package st.tt.script_back.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.RecipeDto;
import st.tt.script_back.dto.DuplicateGoldenRecipeRequestDto;
import st.tt.script_back.entities.ComputationFormula;
import st.tt.script_back.entities.FormulaReference;
import st.tt.script_back.entities.DecisionResultProfile;
import st.tt.script_back.entities.Recipe;
import st.tt.script_back.entities.Step;
import st.tt.script_back.entities.StepEndpoint;
import st.tt.script_back.entities.StepEndpointCondition;
import st.tt.script_back.entities.StepParameter;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.enums.RecipeStatus;
import st.tt.script_back.repositories.ComputationFormulaRepository;
import st.tt.script_back.repositories.DecisionExecutionRepository;
import st.tt.script_back.mappers.RecipeMapper;
import st.tt.script_back.repositories.DecisionResultProfileRepository;
import st.tt.script_back.repositories.RecipeRepository;
import st.tt.script_back.repositories.StepEndpointRepository;
import st.tt.script_back.repositories.StepParameterRepository;
import st.tt.script_back.repositories.StepRepository;

/**
 * RecipeService class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final DecisionResultProfileRepository decisionResultProfileRepository;
    private final DecisionExecutionRepository decisionExecutionRepository;
    private final RecipeMapper recipeMapper;
    private final StepRepository stepRepository;
    private final StepParameterRepository stepParameterRepository;
    private final StepEndpointRepository stepEndpointRepository;
    private final ParameterActivationService parameterActivationService;
    private final ComputationEvaluationService computationEvaluationService;
    private final ComputationFormulaRepository computationFormulaRepository;

    /**
     * Executes RecipeService.
     *
     * @param recipeRepository input argument consumed by RecipeService.
     * @param decisionResultProfileRepository input argument consumed by RecipeService.
     * @param recipeMapper input argument consumed by RecipeService.
     */
    public RecipeService(
            RecipeRepository recipeRepository,
            DecisionResultProfileRepository decisionResultProfileRepository,
            DecisionExecutionRepository decisionExecutionRepository,
            RecipeMapper recipeMapper,
            StepRepository stepRepository,
            StepParameterRepository stepParameterRepository,
            StepEndpointRepository stepEndpointRepository,
            ParameterActivationService parameterActivationService,
            ComputationEvaluationService computationEvaluationService,
            ComputationFormulaRepository computationFormulaRepository) {
        this.recipeRepository = recipeRepository;
        this.decisionResultProfileRepository = decisionResultProfileRepository;
        this.decisionExecutionRepository = decisionExecutionRepository;
        this.recipeMapper = recipeMapper;
        this.stepRepository = stepRepository;
        this.stepParameterRepository = stepParameterRepository;
        this.stepEndpointRepository = stepEndpointRepository;
        this.parameterActivationService = parameterActivationService;
        this.computationEvaluationService = computationEvaluationService;
        this.computationFormulaRepository = computationFormulaRepository;
    }

    /**
     * Executes createRecipe.
     *
     * @param request input argument consumed by createRecipe.
     * @param resultProfileId input argument consumed by createRecipe.
     * @return computed RecipeDto result returned by createRecipe.
     */
    @Transactional
    public RecipeDto createRecipe(RecipeDto request, Long resultProfileId) {
        if (request == null) {
            throw new IllegalArgumentException("Recipe payload is required");
        }

        RecipeKind kind = request.getRecipeKind();
        if (kind == null) {
            throw new IllegalArgumentException("recipeKind is required");
        }
        if (kind == RecipeKind.IMPORTED) {
            throw new IllegalArgumentException("Recipe creation supports only GOLDEN or DERIVED");
        }

        Recipe recipe = recipeMapper.toEntity(request);
        recipe.setRecipeKind(kind);
        if (kind == RecipeKind.GOLDEN) {
            if (request.getParentRecipeId() != null) {
                throw new IllegalArgumentException("GOLDEN recipe must not have a parentRecipeId");
            }
            recipe.setParentRecipe(null);
            recipe.setVersion(request.getVersion() == null ? 1 : request.getVersion());
        } else {
            Recipe parent = resolveDerivedParent(request.getParentRecipeId(), resultProfileId);
            recipe.setParentRecipe(parent);
            recipe.setVersion(nextDerivedVersion(parent.getId(), request.getVersion()));
        }

        if (recipe.getStatus() == null) {
            recipe.setStatus(RecipeStatus.DRAFT);
        }

        Recipe saved = recipeRepository.save(recipe);
        return recipeMapper.toDto(saved);
    }

    @Transactional
    public RecipeDto duplicateGoldenRecipe(DuplicateGoldenRecipeRequestDto request) {
        validateDuplicateRequest(request);

        Recipe sourceGolden = recipeRepository.findByIdWithRequirements(request.getSourceGoldenRecipeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Golden recipe with id " + request.getSourceGoldenRecipeId() + " not found"));

        if (sourceGolden.getRecipeKind() != RecipeKind.GOLDEN) {
            throw new IllegalArgumentException("sourceGoldenRecipeId must reference a GOLDEN recipe");
        }

        String targetName = request.getTargetName().trim();
        if (sourceGolden.getName() != null && sourceGolden.getName().trim().equalsIgnoreCase(targetName)) {
            throw new IllegalArgumentException("targetName must be different from source golden name");
        }

        Recipe targetGolden = buildTargetGolden(sourceGolden, targetName, request.getCreatorId());
        Recipe savedTargetGolden = recipeRepository.save(targetGolden);

        List<Step> sourceSteps = stepRepository.findByRecipeIdOrderByOrderIndexAsc(sourceGolden.getId());
        Map<Long, Step> clonedStepBySourceStepId = cloneSteps(savedTargetGolden, sourceSteps);
        for (Step sourceStep : sourceSteps) {
            Step clonedStep = clonedStepBySourceStepId.get(sourceStep.getId());
            cloneStepParameters(sourceStep, clonedStep);
            cloneStepEndpoint(sourceStep, clonedStep);
        }

        if (request.isIncludeRequiredCapabilities()) {
            savedTargetGolden.getRequiredCapabilities().addAll(sourceGolden.getRequiredCapabilities());
        }
        if (request.isIncludeRequiredConfigurations()) {
            savedTargetGolden.getRequiredConfigurationDefinitions()
                    .addAll(sourceGolden.getRequiredConfigurationDefinitions());
        }
        if (request.isIncludeRequiredCapabilities() || request.isIncludeRequiredConfigurations()) {
            recipeRepository.save(savedTargetGolden);
        }

        if (request.isIncludeFormulas()) {
            cloneComputationFormulas(sourceGolden, savedTargetGolden);
        }

        parameterActivationService.recalculateRecipeActivationStates(savedTargetGolden.getId());
        computationEvaluationService.recomputeRecipeComputedParameters(savedTargetGolden.getId());

        Recipe reloadedTarget = recipeRepository.findById(savedTargetGolden.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Duplicated golden recipe with id " + savedTargetGolden.getId() + " not found"));
        return recipeMapper.toDto(reloadedTarget);
    }

    /**
     * Executes getRecipe.
     *
     * @param recipeId input argument consumed by getRecipe.
     * @return computed RecipeDto result returned by getRecipe.
     */
    @Transactional(readOnly = true)
    public RecipeDto getRecipe(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + recipeId + " not found"));
        return recipeMapper.toDto(recipe);
    }

    /**
     * Executes updateRecipe.
     *
     * @param recipeId input argument consumed by updateRecipe.
     * @param request input argument consumed by updateRecipe.
     * @return computed RecipeDto result returned by updateRecipe.
     */
    @Transactional
    public RecipeDto updateRecipe(Long recipeId, RecipeDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Recipe payload is required");
        }

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + recipeId + " not found"));

        if (request.getRecipeKind() != null && request.getRecipeKind() != recipe.getRecipeKind()) {
            throw new IllegalArgumentException("recipeKind cannot be changed");
        }

        Long existingParentRecipeId = recipe.getParentRecipe() != null ? recipe.getParentRecipe().getId() : null;
        if (request.getParentRecipeId() != null && !request.getParentRecipeId().equals(existingParentRecipeId)) {
            throw new IllegalArgumentException("parentRecipeId cannot be changed");
        }

        if (request.getVersion() != null && !request.getVersion().equals(recipe.getVersion())) {
            throw new IllegalArgumentException("version cannot be changed");
        }

        if (request.getCreatorId() != null && !request.getCreatorId().equals(recipe.getCreatorId())) {
            throw new IllegalArgumentException("creatorId cannot be changed");
        }

        recipeMapper.updateEntityFromDto(request, recipe);

        if (recipe.getRecipeKind() == RecipeKind.GOLDEN) {
            recipe.setParentRecipe(null);
        }

        Recipe saved = recipeRepository.save(recipe);
        return recipeMapper.toDto(saved);
    }

    /**
     * Executes deleteRecipe.
     *
     * @param recipeId input argument consumed by deleteRecipe.
     */
    @Transactional
    public void deleteRecipe(Long recipeId) {
        if (!recipeRepository.existsById(recipeId)) {
            throw new EntityNotFoundException("Recipe with id " + recipeId + " not found");
        }

        // A derived recipe can be linked by historical decision executions.
        // Clear that optional link before deleting the recipe entity.
        decisionExecutionRepository.clearCreatedDerivedRecipeReference(recipeId);

        recipeRepository.deleteById(recipeId);
    }

    /**
     * Executes listDerivedVersions.
     *
     * @param parentRecipeId input argument consumed by listDerivedVersions.
     * @return computed List<RecipeDto> result returned by listDerivedVersions.
     */
    @Transactional(readOnly = true)
    public List<RecipeDto> listDerivedVersions(Long parentRecipeId) {
        return recipeMapper.toDtoList(recipeRepository.findByParentRecipeIdOrderByVersionDesc(parentRecipeId));
    }

    private Recipe resolveDerivedParent(Long parentRecipeId, Long resultProfileId) {
        if (parentRecipeId != null) {
            return recipeRepository.findById(parentRecipeId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Parent recipe with id " + parentRecipeId + " not found"));
        }

        if (resultProfileId == null) {
            throw new IllegalArgumentException(
                    "DERIVED recipe requires parentRecipeId or resultProfileId from questionnaire");
        }

        DecisionResultProfile profile = decisionResultProfileRepository.findById(resultProfileId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "DecisionResultProfile with id " + resultProfileId + " not found"));

        if (profile.getGoldenRecipeId() == null) {
            throw new IllegalStateException(
                    "DecisionResultProfile " + resultProfileId + " has no goldenRecipeId");
        }

        return recipeRepository.findById(profile.getGoldenRecipeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Golden recipe with id " + profile.getGoldenRecipeId() + " not found"));
    }

    private Integer nextDerivedVersion(Long parentRecipeId, Integer requestedVersion) {
        if (requestedVersion != null) {
            return requestedVersion;
        }

        List<Recipe> versions = recipeRepository.findByParentRecipeIdOrderByVersionDesc(parentRecipeId);
        if (versions.isEmpty() || versions.get(0).getVersion() == null) {
            return 1;
        }
        return versions.get(0).getVersion() + 1;
    }

    private Recipe buildTargetGolden(Recipe sourceGolden, String targetName, Long creatorId) {
        Recipe target = new Recipe();
        target.setRecipeKind(RecipeKind.GOLDEN);
        target.setParentRecipe(null);
        target.setName(targetName);
        target.setDescription(sourceGolden.getDescription());
        target.setCreatorId(creatorId != null ? creatorId : sourceGolden.getCreatorId());
        target.setRevisorId(null);
        target.setProcessFamily(sourceGolden.getProcessFamily());
        target.setStatus(RecipeStatus.DRAFT);
        target.setVersion(1);
        target.setFrozen(false);
        target.setWafer(sourceGolden.getWafer());
        target.setIapc(sourceGolden.getIapc());
        target.setResumable(sourceGolden.getResumable());
        target.setChamberType(sourceGolden.getChamberType());
        target.setAccessDisplayGroups(sourceGolden.getAccessDisplayGroups());
        target.setAccessModifyGroups(sourceGolden.getAccessModifyGroups());
        target.setUdaFile(sourceGolden.getUdaFile());
        target.setType(sourceGolden.getType());
        target.setMaxTime(sourceGolden.getMaxTime());
        target.setTemplate(sourceGolden.getTemplate());
        return target;
    }

    private Map<Long, Step> cloneSteps(Recipe targetGolden, List<Step> sourceSteps) {
        Map<Long, Step> cloneBySourceStepId = new HashMap<>();
        for (Step sourceStep : sourceSteps) {
            Step clonedStep = new Step();
            clonedStep.setRecipe(targetGolden);
            clonedStep.setStepKind(sourceStep.getStepKind());
            clonedStep.setOrderIndex(sourceStep.getOrderIndex());
            clonedStep.setName(sourceStep.getName());
            clonedStep.setCode(sourceStep.getCode());

            Step savedStep = stepRepository.save(clonedStep);
            cloneBySourceStepId.put(sourceStep.getId(), savedStep);
        }
        return cloneBySourceStepId;
    }

    private void cloneStepParameters(Step sourceStep, Step clonedStep) {
        List<StepParameter> sourceParameters = stepParameterRepository
                .findByStepIdOrderByParentOrderScopeAscOrderIndexAsc(sourceStep.getId());

        Map<Long, StepParameter> clonedBySourceParameterId = new HashMap<>();
        List<StepParameter> pendingParameters = new ArrayList<>(sourceParameters);

        while (!pendingParameters.isEmpty()) {
            int pendingBefore = pendingParameters.size();

            for (int i = pendingParameters.size() - 1; i >= 0; i--) {
                StepParameter sourceParameter = pendingParameters.get(i);
                StepParameter sourceParent = sourceParameter.getParentStepParameter();
                if (sourceParent != null && !clonedBySourceParameterId.containsKey(sourceParent.getId())) {
                    continue;
                }

                StepParameter clonedParameter = new StepParameter();
                clonedParameter.setStep(clonedStep);
                clonedParameter.setDefinition(sourceParameter.getDefinition());
                clonedParameter.setParentStepParameter(
                        sourceParent == null ? null : clonedBySourceParameterId.get(sourceParent.getId()));
                clonedParameter.setOrderIndex(sourceParameter.getOrderIndex());
                clonedParameter.setLabelOverride(sourceParameter.getLabelOverride());
                clonedParameter.setValueJson(sourceParameter.getValueJson());
                clonedParameter.setSelectedOption(sourceParameter.getSelectedOption());
                clonedParameter.setActivationState(sourceParameter.getActivationState());
                clonedParameter.setLockedByGolden(sourceParameter.isLockedByGolden());
                clonedParameter.setUserModified(sourceParameter.isUserModified());
                clonedParameter.setComputationStatus(sourceParameter.getComputationStatus());
                clonedParameter.setComputedAt(sourceParameter.getComputedAt());

                StepParameter savedClonedParameter = stepParameterRepository.save(clonedParameter);
                clonedBySourceParameterId.put(sourceParameter.getId(), savedClonedParameter);
                pendingParameters.remove(i);
            }

            if (pendingParameters.size() == pendingBefore) {
                throw new IllegalStateException(
                        "Cannot clone step parameter hierarchy for step " + sourceStep.getId()
                                + ": parent relationship is inconsistent");
            }
        }
    }

    private void cloneStepEndpoint(Step sourceStep, Step clonedStep) {
        StepEndpoint sourceEndpoint = stepEndpointRepository.findByStepIdWithConditions(sourceStep.getId())
                .orElse(null);
        if (sourceEndpoint == null) {
            return;
        }

        StepEndpoint clonedEndpoint = new StepEndpoint();
        clonedEndpoint.setStep(clonedStep);
        clonedEndpoint.setClause(sourceEndpoint.getClause());
        clonedEndpoint.setLockedByGolden(sourceEndpoint.isLockedByGolden());

        StepEndpoint savedEndpoint = stepEndpointRepository.save(clonedEndpoint);
        List<StepEndpointCondition> sourceConditions = sourceEndpoint.getConditions() == null
                ? List.of()
                : sourceEndpoint.getConditions();

        for (StepEndpointCondition sourceCondition : sourceConditions) {
            StepEndpointCondition clonedCondition = new StepEndpointCondition();
            clonedCondition.setEndpoint(savedEndpoint);
            clonedCondition.setEndpointParameter(sourceCondition.getEndpointParameter());
            clonedCondition.setValueJson(sourceCondition.getValueJson());
            clonedCondition.setSelectedOption(sourceCondition.getSelectedOption());
            clonedCondition.setOperator(sourceCondition.getOperator());
            clonedCondition.setOrderIndex(sourceCondition.getOrderIndex());
            savedEndpoint.getConditions().add(clonedCondition);
        }

        stepEndpointRepository.save(savedEndpoint);
    }

    private void cloneComputationFormulas(Recipe sourceGolden, Recipe targetGolden) {
        List<ComputationFormula> sourceFormulas = computationFormulaRepository
                .findByRecipeIdWithReferences(sourceGolden.getId());

        for (ComputationFormula sourceFormula : sourceFormulas) {
            ComputationFormula clonedFormula = new ComputationFormula();
            clonedFormula.setRecipe(targetGolden);
            clonedFormula.setTargetStepCode(sourceFormula.getTargetStepCode());
            clonedFormula.setTargetDefinitionPath(sourceFormula.getTargetDefinitionPath());
            clonedFormula.setExpression(sourceFormula.getExpression());
            clonedFormula.setRoundingMode(sourceFormula.getRoundingMode());
            clonedFormula.setDecimals(sourceFormula.getDecimals());
            clonedFormula.setLabel(sourceFormula.getLabel());

            List<FormulaReference> sourceReferences = sourceFormula.getReferences() == null
                    ? List.of()
                    : sourceFormula.getReferences();

            for (FormulaReference sourceReference : sourceReferences) {
                FormulaReference clonedReference = new FormulaReference();
                clonedReference.setFormula(clonedFormula);
                clonedReference.setSlot(sourceReference.getSlot());
                clonedReference.setStepCode(sourceReference.getStepCode());
                clonedReference.setDefinitionPath(sourceReference.getDefinitionPath());
                clonedFormula.getReferences().add(clonedReference);
            }

            computationFormulaRepository.save(clonedFormula);
        }
    }

    private void validateDuplicateRequest(DuplicateGoldenRecipeRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Duplicate golden payload is required");
        }
        if (request.getSourceGoldenRecipeId() == null) {
            throw new IllegalArgumentException("sourceGoldenRecipeId is required");
        }
        if (request.getTargetName() == null || request.getTargetName().trim().isEmpty()) {
            throw new IllegalArgumentException("targetName is required");
        }
    }
}
