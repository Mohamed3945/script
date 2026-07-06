package st.tt.script_back.mappers;

import st.tt.script_back.dto.DecisionOptionDto;
import st.tt.script_back.dto.DecisionQuestionDto;
import st.tt.script_back.entities.DecisionOption;
import st.tt.script_back.entities.DecisionQuestion;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Collections;

/**
 * DecisionQuestionMapper class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Component
public class DecisionQuestionMapper {
    /**
     * Executes toOptionDto.
     *
     * @param option input argument consumed by toOptionDto.
     * @return computed DecisionOptionDto result returned by toOptionDto.
     */
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

    /**
     * Executes toOptionDtoList.
     *
     * @param options input argument consumed by toOptionDtoList.
     * @return computed List<DecisionOptionDto> result returned by toOptionDtoList.
     */
    public List<DecisionOptionDto> toOptionDtoList(List<DecisionOption> options) {
        if (options == null) {
            return Collections.emptyList();
        }
        return options.stream()
                .map(this::toOptionDto)
                .toList();
    }

    /**
     * Executes toQuestionDto.
     *
     * @param question input argument consumed by toQuestionDto.
     * @return computed DecisionQuestionDto result returned by toQuestionDto.
     */
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
