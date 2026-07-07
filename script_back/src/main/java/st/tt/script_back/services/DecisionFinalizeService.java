package st.tt.script_back.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.DecisionExecutionAnswerDto;
import st.tt.script_back.dto.DecisionFinalizeRequestDto;
import st.tt.script_back.dto.DecisionFinalizeResponseDto;
import st.tt.script_back.entities.DecisionExecution;
import st.tt.script_back.entities.DecisionExecutionAnswer;
import st.tt.script_back.entities.DecisionOption;
import st.tt.script_back.entities.DecisionQuestion;
import st.tt.script_back.entities.DecisionResultProfile;
import st.tt.script_back.entities.Recipe;
import st.tt.script_back.entities.Step;
import st.tt.script_back.entities.StepParameter;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.enums.RecipeStatus;
import st.tt.script_back.repositories.DecisionExecutionRepository;
import st.tt.script_back.repositories.DecisionOptionRepository;
import st.tt.script_back.repositories.DecisionQuestionRepository;
import st.tt.script_back.repositories.DecisionResultProfileRepository;
import st.tt.script_back.repositories.RecipeRepository;
import st.tt.script_back.repositories.StepParameterRepository;
import st.tt.script_back.repositories.StepRepository;

@Service
public class DecisionFinalizeService {

    private final DecisionResultProfileRepository decisionResultProfileRepository;
    private final DecisionQuestionRepository decisionQuestionRepository;
    private final DecisionOptionRepository decisionOptionRepository;
    private final DecisionExecutionRepository decisionExecutionRepository;
    private final RecipeRepository recipeRepository;
    private final StepRepository stepRepository;
    private final StepParameterRepository stepParameterRepository;
    private final ParameterActivationService parameterActivationService;

    public DecisionFinalizeService(
            DecisionResultProfileRepository decisionResultProfileRepository,
            DecisionQuestionRepository decisionQuestionRepository,
            DecisionOptionRepository decisionOptionRepository,
            DecisionExecutionRepository decisionExecutionRepository,
            RecipeRepository recipeRepository,
            StepRepository stepRepository,
            StepParameterRepository stepParameterRepository,
            ParameterActivationService parameterActivationService) {
        this.decisionResultProfileRepository = decisionResultProfileRepository;
        this.decisionQuestionRepository = decisionQuestionRepository;
        this.decisionOptionRepository = decisionOptionRepository;
        this.decisionExecutionRepository = decisionExecutionRepository;
        this.recipeRepository = recipeRepository;
        this.stepRepository = stepRepository;
        this.stepParameterRepository = stepParameterRepository;
        this.parameterActivationService = parameterActivationService;
    }

    @Transactional
    public DecisionFinalizeResponseDto finalizeDecisionAndCreateDerivedRecipe(DecisionFinalizeRequestDto request) {
        validateRequest(request);

        DecisionResultProfile resultProfile = decisionResultProfileRepository.findById(request.getResultProfileId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "DecisionResultProfile with id " + request.getResultProfileId() + " not found"));

        if (resultProfile.getGoldenRecipeId() == null) {
            throw new IllegalStateException(
                    "DecisionResultProfile " + resultProfile.getId() + " has no goldenRecipeId");
        }

        if (!resultProfile.getGoldenRecipeId().equals(request.getValidatedGoldenRecipeId())) {
            throw new IllegalArgumentException(
                    "Validated golden recipe does not match the proposed golden recipe from result profile");
        }

        Recipe goldenRecipe = recipeRepository.findById(request.getValidatedGoldenRecipeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Golden recipe with id " + request.getValidatedGoldenRecipeId() + " not found"));

        if (goldenRecipe.getRecipeKind() != RecipeKind.GOLDEN) {
            throw new IllegalArgumentException(
                    "Validated recipe " + goldenRecipe.getId() + " is not a GOLDEN recipe");
        }

        DecisionExecution execution = new DecisionExecution();
        execution.setResultProfile(resultProfile);
        execution.setValidatedGoldenRecipeId(request.getValidatedGoldenRecipeId());
        execution.setSelectedMachineId(request.getSelectedMachineId());
        execution.setCreatorId(request.getCreatorId());

        List<DecisionExecutionAnswer> answers = toDecisionExecutionAnswers(execution, request.getAnswers());
        execution.setAnswers(answers);

        DecisionExecution savedExecution = decisionExecutionRepository.save(execution);

        Recipe derivedRecipe = createDerivedRecipeFromGolden(goldenRecipe, request.getCreatorId());

        savedExecution.setCreatedDerivedRecipeId(derivedRecipe.getId());
        decisionExecutionRepository.save(savedExecution);

        return new DecisionFinalizeResponseDto(savedExecution.getId(), derivedRecipe.getId());
    }

    private List<DecisionExecutionAnswer> toDecisionExecutionAnswers(
            DecisionExecution execution,
            List<DecisionExecutionAnswerDto> answerDtos) {
        List<DecisionExecutionAnswer> answers = new ArrayList<>();
        Set<Long> seenQuestionIds = new HashSet<>();

        for (DecisionExecutionAnswerDto answerDto : answerDtos) {
            if (answerDto == null) {
                throw new IllegalArgumentException("answers cannot contain null entries");
            }
            if (answerDto.getQuestionId() == null) {
                throw new IllegalArgumentException("answer.questionId is required");
            }
            if (answerDto.getOptionId() == null) {
                throw new IllegalArgumentException("answer.optionId is required");
            }

            if (!seenQuestionIds.add(answerDto.getQuestionId())) {
                throw new IllegalArgumentException("Each question can only be answered once");
            }

            DecisionQuestion question = decisionQuestionRepository.findById(answerDto.getQuestionId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "DecisionQuestion with id " + answerDto.getQuestionId() + " not found"));

            DecisionOption option = decisionOptionRepository.findById(answerDto.getOptionId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "DecisionOption with id " + answerDto.getOptionId() + " not found"));

            Long optionQuestionId = option.getDecisionQuestion() != null ? option.getDecisionQuestion().getId() : null;
            if (optionQuestionId == null || !optionQuestionId.equals(question.getId())) {
                throw new IllegalArgumentException(
                        "Option " + option.getId() + " does not belong to question " + question.getId());
            }

            DecisionExecutionAnswer answer = new DecisionExecutionAnswer();
            answer.setDecisionExecution(execution);
            answer.setQuestion(question);
            answer.setOption(option);
            answer.setOrderIndex(answerDto.getOrderIndex() == null ? 0 : answerDto.getOrderIndex());
            answers.add(answer);
        }

        return answers;
    }

    private Recipe createDerivedRecipeFromGolden(Recipe goldenRecipe, Long creatorId) {
        Recipe derivedRecipe = new Recipe();
        derivedRecipe.setRecipeKind(RecipeKind.DERIVED);
        derivedRecipe.setParentRecipe(goldenRecipe);
        derivedRecipe.setName(goldenRecipe.getName() + " - Derived");
        derivedRecipe.setDescription(goldenRecipe.getDescription());
        derivedRecipe.setCreatorId(creatorId);
        derivedRecipe.setRevisorId(null);
        derivedRecipe.setProcessFamily(goldenRecipe.getProcessFamily());
        derivedRecipe.setStatus(RecipeStatus.DRAFT);
        derivedRecipe.setVersion(nextDerivedVersion(goldenRecipe.getId()));
        derivedRecipe.setFrozen(false);

        Recipe savedDerivedRecipe = recipeRepository.save(derivedRecipe);

        List<Step> goldenSteps = stepRepository.findByRecipeIdOrderByOrderIndexAsc(goldenRecipe.getId());
        Map<Long, Step> stepCloneByOriginalStepId = cloneSteps(savedDerivedRecipe, goldenSteps);

        for (Step goldenStep : goldenSteps) {
            Step clonedStep = stepCloneByOriginalStepId.get(goldenStep.getId());
            cloneStepParameters(goldenStep, clonedStep);
        }

        // Defensive reset: derived recipes must start with no user-modified markers.
        stepParameterRepository.resetUserModifiedByRecipeId(savedDerivedRecipe.getId());

        parameterActivationService.recalculateRecipeActivationStates(savedDerivedRecipe.getId());

        return savedDerivedRecipe;
    }

    private Map<Long, Step> cloneSteps(Recipe savedDerivedRecipe, List<Step> goldenSteps) {
        Map<Long, Step> stepCloneByOriginalStepId = new HashMap<>();
        for (Step goldenStep : goldenSteps) {
            Step clonedStep = new Step();
            clonedStep.setRecipe(savedDerivedRecipe);
            clonedStep.setStepKind(goldenStep.getStepKind());
            clonedStep.setOrderIndex(goldenStep.getOrderIndex());
            clonedStep.setName(goldenStep.getName());
            clonedStep.setCode(goldenStep.getCode());

            Step savedStep = stepRepository.save(clonedStep);
            stepCloneByOriginalStepId.put(goldenStep.getId(), savedStep);
        }
        return stepCloneByOriginalStepId;
    }

    private void cloneStepParameters(Step goldenStep, Step clonedStep) {
        List<StepParameter> goldenParameters = stepParameterRepository
                .findByStepIdOrderByParentOrderScopeAscOrderIndexAsc(goldenStep.getId());

        Map<Long, StepParameter> parameterCloneByOriginalParameterId = new HashMap<>();
        List<StepParameter> pendingParameters = new ArrayList<>(goldenParameters);

        while (!pendingParameters.isEmpty()) {
            int pendingBefore = pendingParameters.size();

            for (int i = pendingParameters.size() - 1; i >= 0; i--) {
                StepParameter goldenParameter = pendingParameters.get(i);

                StepParameter goldenParent = goldenParameter.getParentStepParameter();
                if (goldenParent != null && !parameterCloneByOriginalParameterId.containsKey(goldenParent.getId())) {
                    continue;
                }

                StepParameter clonedParameter = new StepParameter();
                clonedParameter.setStep(clonedStep);
                clonedParameter.setDefinition(goldenParameter.getDefinition());
                clonedParameter.setParentStepParameter(
                        goldenParent == null ? null : parameterCloneByOriginalParameterId.get(goldenParent.getId()));
                clonedParameter.setOrderIndex(goldenParameter.getOrderIndex());
                clonedParameter.setLabelOverride(goldenParameter.getLabelOverride());
                clonedParameter.setValueJson(goldenParameter.getValueJson());
                clonedParameter.setSelectedOption(goldenParameter.getSelectedOption());
                clonedParameter.setActivationState(goldenParameter.getActivationState());
                clonedParameter.setLockedByGolden(goldenParameter.isLockedByGolden());
                clonedParameter.setUserModified(false);

                StepParameter savedClonedParameter = stepParameterRepository.save(clonedParameter);
                parameterCloneByOriginalParameterId.put(goldenParameter.getId(), savedClonedParameter);
                pendingParameters.remove(i);
            }

            if (pendingParameters.size() == pendingBefore) {
                throw new IllegalStateException(
                        "Cannot clone step parameter hierarchy for step " + goldenStep.getId()
                                + ": parent relationship is inconsistent");
            }
        }
    }

    private Integer nextDerivedVersion(Long parentRecipeId) {
        List<Recipe> versions = recipeRepository.findByParentRecipeIdOrderByVersionDesc(parentRecipeId);
        if (versions.isEmpty() || versions.get(0).getVersion() == null) {
            return 1;
        }
        return versions.get(0).getVersion() + 1;
    }

    private void validateRequest(DecisionFinalizeRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("DecisionFinalizeRequest payload is required");
        }
        if (request.getResultProfileId() == null) {
            throw new IllegalArgumentException("resultProfileId is required");
        }
        if (request.getValidatedGoldenRecipeId() == null) {
            throw new IllegalArgumentException("validatedGoldenRecipeId is required");
        }
        if (request.getSelectedMachineId() == null) {
            throw new IllegalArgumentException("selectedMachineId is required");
        }
        if (request.getCreatorId() == null) {
            throw new IllegalArgumentException("creatorId is required");
        }
        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new IllegalArgumentException("answers are required");
        }
    }
}
