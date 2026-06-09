package st.tt.script_back.mappers;

import st.tt.script_back.dto.DecisionOptionDto;
import st.tt.script_back.dto.DecisionQuestionDto;
import st.tt.script_back.entities.DecisionOption;
import st.tt.script_back.entities.DecisionQuestion;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Collections;

@Component
public class DecisionQuestionMapper {
    public DecisionOptionDto toOptionDto(DecisionOption option) {
        if (option == null) {
            return null;
        }
        return new DecisionOptionDto(
                option.getId(),
                option.getLabel(),
                option.getValue(),
                option.getOrderIndex()
        );
    }

    public List<DecisionOptionDto> toOptionDtoList(List<DecisionOption> options) {
        if (options == null) {
            return Collections.emptyList();
        }
        return options.stream()
                .map(this::toOptionDto)
                .toList();
    }

    public DecisionQuestionDto toQuestionDto(DecisionQuestion question) {
        if (question == null) {
            return null;
        }
        return new DecisionQuestionDto(
                question.getId(),
                question.getCode(),
                question.getLabel(),
                question.getOrderIndex(),
                question.isEntryPoint(),
                question.isActive(),
                question.getQuestionType().name(),
                toOptionDtoList(question.getOptions())
        );
    }
}