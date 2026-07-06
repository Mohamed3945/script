package st.tt.script_back.services;

import st.tt.script_back.entities.DecisionQuestion;
import st.tt.script_back.repositories.DecisionQuestionRepository;
import org.springframework.stereotype.Service;
import st.tt.script_back.dto.DecisionQuestionDto;
import st.tt.script_back.dto.DecisionOptionDto;
import st.tt.script_back.mappers.DecisionQuestionMapper;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;


import java.util.List;



/**
 * DecisionQuestionService class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Service
public class DecisionQuestionService {
    
    private final DecisionQuestionRepository decisionQuestionRepository;
    private final DecisionQuestionMapper decisionQuestionMapper;

    /**
     * Executes DecisionQuestionService.
     *
     * @param decisionQuestionRepository input argument consumed by DecisionQuestionService.
     * @param decisionQuestionMapper input argument consumed by DecisionQuestionService.
     */
    public DecisionQuestionService(
        DecisionQuestionRepository decisionQuestionRepository, 
        DecisionQuestionMapper decisionQuestionMapper) 
{
        this.decisionQuestionRepository = decisionQuestionRepository;
        this.decisionQuestionMapper = decisionQuestionMapper;
    }

    /**
     * Executes getEntryPointQuestion.
     * @return computed DecisionQuestionDto result returned by getEntryPointQuestion.
     */
    @Transactional(readOnly = true)
    public DecisionQuestionDto getEntryPointQuestion() {

        DecisionQuestion question = decisionQuestionRepository.findEntryPointQuestionWithOptions()
                .orElseThrow(() -> new EntityNotFoundException("No entry point question found"));
           return decisionQuestionMapper.toQuestionDto (question);
   }
    /**
     * Executes getQuestionByCode.
     *
     * @param code input argument consumed by getQuestionByCode.
     * @return computed DecisionQuestionDto result returned by getQuestionByCode.
     */
    @Transactional(readOnly = true)
    public DecisionQuestionDto getQuestionByCode(String code) {
        DecisionQuestion question = decisionQuestionRepository.findByCodeWithOptions(code)
                .orElseThrow(() -> new EntityNotFoundException("Question with code " + code + " not found"));

        return decisionQuestionMapper.toQuestionDto(question);
    }
    
    /**
     * Executes findById.
     *
     * @param questionID input argument consumed by findById.
     * @return computed DecisionQuestionDto result returned by findById.
     */
    @Transactional(readOnly = true)
    public DecisionQuestionDto findById(Long questionID) {
        DecisionQuestion question = decisionQuestionRepository.findByIdWithOptions(questionID)
                .orElseThrow(() -> new EntityNotFoundException("Question with id " + questionID + " not found"));

        return decisionQuestionMapper.toQuestionDto(question);
    }

    /**
     * Executes getOptionForQuestion.
     *
     * @param questionID input argument consumed by getOptionForQuestion.
     * @return computed List<DecisionOptionDto> result returned by getOptionForQuestion.
     */
    @Transactional(readOnly = true)
    public List<DecisionOptionDto> getOptionForQuestion(Long questionID) {
      DecisionQuestion question = decisionQuestionRepository.findByIdWithOptions(questionID)
                .orElseThrow(() -> new EntityNotFoundException("Question with id " + questionID + " not found"));

        return decisionQuestionMapper.toOptionDtoList(question.getOptions());
    }

}
