package st.tt.script_back.controllers;
import st.tt.script_back.services.DecisionQuestionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import st.tt.script_back.dto.DecisionQuestionDto;
import st.tt.script_back.dto.DecisionOptionDto;
import java.util.List;

@RestController
public class DecisionQuestionController {
    private final DecisionQuestionService decisionQuestionService;

    public DecisionQuestionController(DecisionQuestionService decisionQuestionService) {
        this.decisionQuestionService = decisionQuestionService;
    }

    @GetMapping("/api/questions/entry-point")
    public DecisionQuestionDto getEntryPointQuestion() {
        return decisionQuestionService.getEntryPointQuestion();
    }

    @GetMapping("/api/questions/code/{code}")
    public DecisionQuestionDto getQuestionByCode(@PathVariable String code) {
        return decisionQuestionService.getQuestionByCode(code);
    }

    @GetMapping("/api/questions/{id}/options")
    public List<DecisionOptionDto> getOptionsForQuestion(@PathVariable Long id) {
        return decisionQuestionService.getOptionForQuestion(id);
    }

    @GetMapping("/api/questions/{id}")
    public DecisionQuestionDto getQuestionById(@PathVariable Long id) {
        return decisionQuestionService.findById(id);
    }
}