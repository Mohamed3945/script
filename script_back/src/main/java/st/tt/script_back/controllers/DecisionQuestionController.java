package st.tt.script_back.controllers;
import st.tt.script_back.services.DecisionQuestionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import st.tt.script_back.dto.DecisionQuestionDto;
import st.tt.script_back.dto.DecisionOptionDto;
import java.util.List;

/**
 * DecisionQuestionController class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@RestController
public class DecisionQuestionController {
    private final DecisionQuestionService decisionQuestionService;

    /**
     * Executes DecisionQuestionController.
     *
     * @param decisionQuestionService input argument consumed by DecisionQuestionController.
     */
    public DecisionQuestionController(DecisionQuestionService decisionQuestionService) {
        this.decisionQuestionService = decisionQuestionService;
    }

    /**
     * Executes getEntryPointQuestion.
     * @return computed DecisionQuestionDto result returned by getEntryPointQuestion.
     */
    @GetMapping("/api/questions/entry-point")
    public DecisionQuestionDto getEntryPointQuestion() {
        return decisionQuestionService.getEntryPointQuestion();
    }

    /**
     * Executes getQuestionByCode.
     *
     * @param code input argument consumed by getQuestionByCode.
     * @return computed DecisionQuestionDto result returned by getQuestionByCode.
     */
    @GetMapping("/api/questions/code/{code}")
    public DecisionQuestionDto getQuestionByCode(@PathVariable String code) {
        return decisionQuestionService.getQuestionByCode(code);
    }

    /**
     * Executes getOptionsForQuestion.
     *
     * @param id input argument consumed by getOptionsForQuestion.
     * @return computed List<DecisionOptionDto> result returned by getOptionsForQuestion.
     */
    @GetMapping("/api/questions/{id}/options")
    public List<DecisionOptionDto> getOptionsForQuestion(@PathVariable Long id) {
        return decisionQuestionService.getOptionForQuestion(id);
    }

    /**
     * Executes getQuestionById.
     *
     * @param id input argument consumed by getQuestionById.
     * @return computed DecisionQuestionDto result returned by getQuestionById.
     */
    @GetMapping("/api/questions/{id}")
    public DecisionQuestionDto getQuestionById(@PathVariable Long id) {
        return decisionQuestionService.findById(id);
    }
}
