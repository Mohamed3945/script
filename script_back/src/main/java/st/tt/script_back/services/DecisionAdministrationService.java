package st.tt.script_back.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.DecisionOptionAdminRequestDto;
import st.tt.script_back.dto.DecisionOptionDto;
import st.tt.script_back.dto.DecisionQuestionAdminRequestDto;
import st.tt.script_back.dto.DecisionQuestionDto;
import st.tt.script_back.dto.DecisionResultProfileAdminRequestDto;
import st.tt.script_back.dto.DecisionResultProfileDto;
import st.tt.script_back.dto.DecisionTransitionAdminDto;
import st.tt.script_back.dto.DecisionTransitionAdminRequestDto;
import st.tt.script_back.entities.DecisionOption;
import st.tt.script_back.entities.DecisionQuestion;
import st.tt.script_back.entities.DecisionResultProfile;
import st.tt.script_back.entities.DecisionTransition;
import st.tt.script_back.entities.Recipe;
import st.tt.script_back.enums.QuestionType;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.mappers.DecisionQuestionMapper;
import st.tt.script_back.mappers.DecisionResultProfileMapper;
import st.tt.script_back.repositories.DecisionOptionRepository;
import st.tt.script_back.repositories.DecisionExecutionAnswerRepository;
import st.tt.script_back.repositories.DecisionExecutionRepository;
import st.tt.script_back.repositories.DecisionQuestionRepository;
import st.tt.script_back.repositories.DecisionResultProfileRepository;
import st.tt.script_back.repositories.DecisionTransitionRepository;
import st.tt.script_back.repositories.RecipeRepository;

@Service
public class DecisionAdministrationService {

    private final DecisionQuestionRepository questionRepository;
    private final DecisionExecutionAnswerRepository executionAnswerRepository;
    private final DecisionExecutionRepository executionRepository;
    private final DecisionOptionRepository optionRepository;
    private final DecisionTransitionRepository transitionRepository;
    private final DecisionResultProfileRepository resultProfileRepository;
    private final DecisionQuestionMapper questionMapper;
    private final DecisionResultProfileMapper resultProfileMapper;
    private final RecipeRepository recipeRepository;

    public DecisionAdministrationService(
            DecisionQuestionRepository questionRepository,
            DecisionExecutionAnswerRepository executionAnswerRepository,
            DecisionExecutionRepository executionRepository,
            DecisionOptionRepository optionRepository,
            DecisionTransitionRepository transitionRepository,
            DecisionResultProfileRepository resultProfileRepository,
            DecisionQuestionMapper questionMapper,
            DecisionResultProfileMapper resultProfileMapper,
            RecipeRepository recipeRepository) {
        this.questionRepository = questionRepository;
        this.executionAnswerRepository = executionAnswerRepository;
        this.executionRepository = executionRepository;
        this.optionRepository = optionRepository;
        this.transitionRepository = transitionRepository;
        this.resultProfileRepository = resultProfileRepository;
        this.questionMapper = questionMapper;
        this.resultProfileMapper = resultProfileMapper;
        this.recipeRepository = recipeRepository;
    }

    @Transactional(readOnly = true)
    public List<DecisionQuestionDto> getQuestions() {
        return questionRepository.findAllByOrderByOrderIndexAscIdAsc().stream()
                .map(questionMapper::toQuestionDto)
                .toList();
    }

    @Transactional
    public DecisionQuestionDto createQuestion(DecisionQuestionAdminRequestDto request) {
        validateQuestion(request, null);
        DecisionQuestion question = new DecisionQuestion();
        applyQuestion(request, question);
        return questionMapper.toQuestionDto(questionRepository.save(question));
    }

    @Transactional
    public DecisionQuestionDto updateQuestion(Long id, DecisionQuestionAdminRequestDto request) {
        DecisionQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Question with id " + id + " not found"));
        validateQuestion(request, id);
        applyQuestion(request, question);
        return questionMapper.toQuestionDto(questionRepository.save(question));
    }

    @Transactional
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new EntityNotFoundException("Question with id " + id + " not found");
        }
        if (executionAnswerRepository.existsByQuestionId(id)) {
            throw new IllegalStateException("Question is used by saved decision executions and cannot be deleted");
        }
        transitionRepository.deleteByCurrentQuestionIdOrNextQuestionId(id, id);
        questionRepository.deleteById(id);
    }

    @Transactional
    public DecisionOptionDto createOption(Long questionId, DecisionOptionAdminRequestDto request) {
        DecisionQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question with id " + questionId + " not found"));
        validateOption(request);
        DecisionOption option = new DecisionOption();
        option.setDecisionQuestion(question);
        applyOption(request, option);
        return questionMapper.toOptionDto(optionRepository.save(option));
    }

    @Transactional
    public DecisionOptionDto updateOption(Long id, DecisionOptionAdminRequestDto request) {
        DecisionOption option = optionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Option with id " + id + " not found"));
        validateOption(request);
        applyOption(request, option);
        return questionMapper.toOptionDto(optionRepository.save(option));
    }

    @Transactional
    public void deleteOption(Long id) {
        if (!optionRepository.existsById(id)) {
            throw new EntityNotFoundException("Option with id " + id + " not found");
        }
        if (executionAnswerRepository.existsByOptionId(id)) {
            throw new IllegalStateException("Option is used by saved decision executions and cannot be deleted");
        }
        transitionRepository.deleteByOptionId(id);
        optionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<DecisionTransitionAdminDto> getTransitions() {
        return transitionRepository.findAllByOrderByCurrentQuestionOrderIndexAscOptionOrderIndexAsc().stream()
                .map(this::toTransitionDto)
                .toList();
    }

    @Transactional
    public DecisionTransitionAdminDto createTransition(DecisionTransitionAdminRequestDto request) {
        DecisionTransition transition = new DecisionTransition();
        applyTransition(request, transition);
        return toTransitionDto(transitionRepository.save(transition));
    }

    @Transactional
    public DecisionTransitionAdminDto updateTransition(Long id, DecisionTransitionAdminRequestDto request) {
        DecisionTransition transition = transitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transition with id " + id + " not found"));
        applyTransition(request, transition);
        return toTransitionDto(transitionRepository.save(transition));
    }

    @Transactional
    public void deleteTransition(Long id) {
        if (!transitionRepository.existsById(id)) {
            throw new EntityNotFoundException("Transition with id " + id + " not found");
        }
        transitionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<DecisionResultProfileDto> getResultProfiles() {
        return resultProfileRepository.findAllByOrderByCodeAsc().stream()
                .map(resultProfileMapper::toResultProfileDto)
                .toList();
    }

    @Transactional
    public DecisionResultProfileDto createResultProfile(DecisionResultProfileAdminRequestDto request) {
        validateResultProfile(request, null);
        DecisionResultProfile profile = new DecisionResultProfile();
        applyResultProfile(request, profile);
        return resultProfileMapper.toResultProfileDto(resultProfileRepository.save(profile));
    }

    @Transactional
    public DecisionResultProfileDto updateResultProfile(Long id, DecisionResultProfileAdminRequestDto request) {
        DecisionResultProfile profile = resultProfileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Result profile with id " + id + " not found"));
        validateResultProfile(request, id);
        applyResultProfile(request, profile);
        return resultProfileMapper.toResultProfileDto(resultProfileRepository.save(profile));
    }

    @Transactional
    public void deleteResultProfile(Long id) {
        if (!resultProfileRepository.existsById(id)) {
            throw new EntityNotFoundException("Result profile with id " + id + " not found");
        }
        if (executionRepository.existsByResultProfileId(id)) {
            throw new IllegalStateException("Result profile is used by saved decision executions and cannot be deleted");
        }
        transitionRepository.deleteByResultProfileId(id);
        resultProfileRepository.deleteById(id);
    }

    private void validateQuestion(DecisionQuestionAdminRequestDto request, Long id) {
        if (request == null || request.getCode() == null || request.getCode().isBlank()
                || request.getLabel() == null || request.getLabel().isBlank()) {
            throw new IllegalArgumentException("Question code and label are required");
        }
        if (questionRepository.existsByCode(request.getCode().trim())
                && (id == null || questionRepository.findById(id)
                        .map(question -> !question.getCode().equals(request.getCode().trim())).orElse(true))) {
            throw new IllegalArgumentException("Question code already exists: " + request.getCode());
        }
        try {
            QuestionType.valueOf(request.getQuestionType());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Unsupported question type: " + request.getQuestionType());
        }
    }

    private void applyQuestion(DecisionQuestionAdminRequestDto request, DecisionQuestion question) {
        if (request.isEntryPoint()) {
            questionRepository.findByEntryPointTrue()
                    .filter(existing -> !existing.getId().equals(question.getId()))
                    .ifPresent(existing -> {
                        existing.setEntryPoint(false);
                        questionRepository.save(existing);
                    });
        }
        question.setCode(request.getCode().trim());
        question.setLabel(request.getLabel().trim());
        question.setQuestionType(QuestionType.valueOf(request.getQuestionType()));
        question.setEntryPoint(request.isEntryPoint());
        question.setOrderIndex(request.getOrderIndex());
        question.setActive(request.isActive());
    }

    private void validateOption(DecisionOptionAdminRequestDto request) {
        if (request == null || request.getLabel() == null || request.getLabel().isBlank()
                || request.getValue() == null || request.getValue().isBlank()) {
            throw new IllegalArgumentException("Option label and value are required");
        }
    }

    private void applyOption(DecisionOptionAdminRequestDto request, DecisionOption option) {
        option.setLabel(request.getLabel().trim());
        option.setValue(request.getValue().trim());
        option.setOrderIndex(request.getOrderIndex());
    }

    private void validateResultProfile(DecisionResultProfileAdminRequestDto request, Long id) {
        if (request == null || request.getCode() == null || request.getCode().isBlank()) {
            throw new IllegalArgumentException("Result profile code is required");
        }
        String code = request.getCode().trim();
        if (resultProfileRepository.existsByCode(code)
                && (id == null || resultProfileRepository.findById(id)
                        .map(profile -> !profile.getCode().equals(code)).orElse(true))) {
            throw new IllegalArgumentException("Result profile code already exists: " + code);
        }
        if (request.getGoldenRecipeId() != null) {
            Recipe recipe = recipeRepository.findById(request.getGoldenRecipeId())
                    .orElseThrow(() -> new EntityNotFoundException("Golden recipe not found"));
            if (recipe.getRecipeKind() != RecipeKind.GOLDEN) {
                throw new IllegalArgumentException("Result profile must reference a golden recipe");
            }
        }
    }

    private void applyResultProfile(DecisionResultProfileAdminRequestDto request, DecisionResultProfile profile) {
        profile.setCode(request.getCode().trim());
        profile.setDescription(request.getDescription() == null || request.getDescription().isBlank()
                ? null : request.getDescription().trim());
        profile.setActive(request.isActive());
        profile.setGoldenRecipeId(request.getGoldenRecipeId());
        // Existing profile rows use machine ID 1 as their legacy default; keep new saves consistent.
        profile.setMachineId(1L);
    }

    private void applyTransition(DecisionTransitionAdminRequestDto request, DecisionTransition transition) {
        if (request == null || request.getCurrentQuestionId() == null || request.getOptionId() == null) {
            throw new IllegalArgumentException("Current question and option are required");
        }
        if ((request.getNextQuestionId() == null) == (request.getResultProfileId() == null)) {
            throw new IllegalArgumentException("A transition must target exactly one question or result profile");
        }
        DecisionQuestion currentQuestion = questionRepository.findById(request.getCurrentQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("Current question not found"));
        DecisionOption option = optionRepository.findById(request.getOptionId())
                .orElseThrow(() -> new EntityNotFoundException("Option not found"));
        if (!option.getDecisionQuestion().getId().equals(currentQuestion.getId())) {
            throw new IllegalArgumentException("The selected option does not belong to the current question");
        }
        transitionRepository.findByCurrentQuestionIdAndOptionId(currentQuestion.getId(), option.getId())
                .filter(existing -> !existing.getId().equals(transition.getId()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("A transition already exists for this question and response");
                });
        transition.setCurrentQuestion(currentQuestion);
        transition.setOption(option);
        transition.setNextQuestion(request.getNextQuestionId() == null ? null : questionRepository.findById(request.getNextQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("Next question not found")));
        transition.setResultProfile(request.getResultProfileId() == null ? null : resultProfileRepository.findById(request.getResultProfileId())
                .orElseThrow(() -> new EntityNotFoundException("Result profile not found")));
    }

    private DecisionTransitionAdminDto toTransitionDto(DecisionTransition transition) {
        DecisionQuestion currentQuestion = transition.getCurrentQuestion();
        DecisionQuestion nextQuestion = transition.getNextQuestion();
        DecisionResultProfile resultProfile = transition.getResultProfile();
        return new DecisionTransitionAdminDto(
                transition.getId(), currentQuestion.getId(), currentQuestion.getCode(), currentQuestion.getLabel(),
                transition.getOption().getId(), transition.getOption().getLabel(),
                nextQuestion == null ? null : nextQuestion.getId(), nextQuestion == null ? null : nextQuestion.getCode(),
                resultProfile == null ? null : resultProfile.getId(), resultProfile == null ? null : resultProfile.getCode());
    }
}