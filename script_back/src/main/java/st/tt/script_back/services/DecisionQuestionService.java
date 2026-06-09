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



@Service
public class DecisionQuestionService {
    
    private final DecisionQuestionRepository decisionQuestionRepository;
    private final DecisionQuestionMapper decisionQuestionMapper;

    public DecisionQuestionService(
        DecisionQuestionRepository decisionQuestionRepository, 
        DecisionQuestionMapper decisionQuestionMapper) 
{
        this.decisionQuestionRepository = decisionQuestionRepository;
        this.decisionQuestionMapper = decisionQuestionMapper;
    }

    @Transactional(readOnly = true)
    public DecisionQuestionDto getEntryPointQuestion() {

        DecisionQuestion question = decisionQuestionRepository.findEntryPointQuestionWithOptions()
                .orElseThrow(() -> new EntityNotFoundException("No entry point question found"));
           return decisionQuestionMapper.toQuestionDto (question);
   }
    @Transactional(readOnly = true)
    public DecisionQuestionDto getQuestionByCode(String code) {
        DecisionQuestion question = decisionQuestionRepository.findByCodeWithOptions(code)
                .orElseThrow(() -> new EntityNotFoundException("Question with code " + code + " not found"));

        return decisionQuestionMapper.toQuestionDto(question);
    }
    
    @Transactional(readOnly = true)
    public DecisionQuestionDto findById(Long questionID) {
        DecisionQuestion question = decisionQuestionRepository.findByIdWithOptions(questionID)
                .orElseThrow(() -> new EntityNotFoundException("Question with id " + questionID + " not found"));

        return decisionQuestionMapper.toQuestionDto(question);
    }

    @Transactional(readOnly = true)
    public List<DecisionOptionDto> getOptionForQuestion(Long questionID) {
      DecisionQuestion question = decisionQuestionRepository.findByIdWithOptions(questionID)
                .orElseThrow(() -> new EntityNotFoundException("Question with id " + questionID + " not found"));

        return decisionQuestionMapper.toOptionDtoList(question.getOptions());
    }

}
