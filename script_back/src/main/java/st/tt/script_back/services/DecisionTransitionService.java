package st.tt.script_back.services;

import org.springframework.stereotype.Service;

import st.tt.script_back.dto.DecisionNextRequestDto;
import st.tt.script_back.enums.NextTransitionType;
import st.tt.script_back.mappers.DecisionQuestionMapper;
import st.tt.script_back.mappers.DecisionResultProfileMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import st.tt.script_back.repositories.DecisionQuestionRepository;
import st.tt.script_back.repositories.DecisionTransitionRepository;
import st.tt.script_back.entities.DecisionTransition;
import st.tt.script_back.entities.DecisionQuestion;
import st.tt.script_back.dto.DecisionQuestionDto;
import st.tt.script_back.dto.DecisionNextResponseDto;
import st.tt.script_back.dto.DecisionResultProfileDto;
import st.tt.script_back.entities.DecisionResultProfile;

@Service
public class DecisionTransitionService {
    private final DecisionTransitionRepository decisionTransitionRepository;
    private final DecisionQuestionRepository decisionQuestionRepository;
    private final DecisionQuestionMapper decisionQuestionMapper;
    private final DecisionResultProfileMapper decisionResultProfileMapper;

    public DecisionTransitionService(
        DecisionTransitionRepository decisionTransitionRepository,
        DecisionQuestionRepository decisionQuestionRepository, 
        DecisionQuestionMapper decisionQuestionMapper, 
        DecisionResultProfileMapper decisionResultProfileMapper) {
        this.decisionTransitionRepository = decisionTransitionRepository;
        this.decisionQuestionRepository = decisionQuestionRepository;
        this.decisionQuestionMapper = decisionQuestionMapper;
        this.decisionResultProfileMapper = decisionResultProfileMapper;
    }

    @Transactional(readOnly = true )
    public DecisionNextResponseDto getNextTransition(DecisionNextRequestDto request) {
        if (request.getCurrentQuestionId()== null || request.getSelectedOptionId() == null){
            throw new IllegalArgumentException("Current question ID and selected option ID must not be provided");
        }
        DecisionTransition transition = decisionTransitionRepository.findByCurrentQuestionIdAndOptionId(request.getCurrentQuestionId(),request.getSelectedOptionId())
            .orElseThrow(() -> new EntityNotFoundException("Transition not found for question ID: " + request.getCurrentQuestionId() + " and option ID: " + request.getSelectedOptionId())
        );
    
        if (transition.getNextQuestion() != null && transition.getResultProfile() != null) {
            throw new IllegalStateException("Transition cannot have both next question and result profile");
        }

        if (transition.getNextQuestion() == null && transition.getResultProfile() == null) {
            throw new IllegalStateException("Transition must have either next question or result profile");
        }

        if (transition.getNextQuestion() != null) {
            Long nextQuestionId = transition.getNextQuestion().getId();

            DecisionQuestion nextQuestion = decisionQuestionRepository.findById(nextQuestionId)
                .orElseThrow(() -> new EntityNotFoundException("Next question not found with ID: " + nextQuestionId)
            );

            DecisionQuestionDto nextQuestionDto = decisionQuestionMapper.toQuestionDto(nextQuestion);

            return new DecisionNextResponseDto(
                NextTransitionType.QUESTION,
                nextQuestionDto,
                null
            );

        } 
        DecisionResultProfile resultProfile = transition.getResultProfile();
        DecisionResultProfileDto resultProfileDto = decisionResultProfileMapper.toResultProfileDto(resultProfile);

        return new DecisionNextResponseDto(
            NextTransitionType.RESULT,
            null,
            resultProfileDto
        );
    }
}
