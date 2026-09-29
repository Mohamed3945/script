package st.tt.script_back.controllers;
import st.tt.script_back.services.DecisionQuestionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import st.tt.script_back.dto.DecisionQuestionDto;
import st.tt.script_back.dto.DecisionOptionDto;
import java.util.List;

/** Expose les questions et options du parcours de décision. */
@RestController
public class DecisionQuestionController {
    private final DecisionQuestionService decisionQuestionService;

    /** @param decisionQuestionService service de consultation des questions */
    public DecisionQuestionController(DecisionQuestionService decisionQuestionService) {
        this.decisionQuestionService = decisionQuestionService;
    }

    /** @return question initiale du parcours de décision */
    @GetMapping("/api/questions/entry-point")
    public DecisionQuestionDto getEntryPointQuestion() {
        return decisionQuestionService.getEntryPointQuestion();
    }

    /** @param code code fonctionnel de la question @return question correspondante */
    @GetMapping("/api/questions/code/{code}")
    public DecisionQuestionDto getQuestionByCode(@PathVariable String code) {
        return decisionQuestionService.getQuestionByCode(code);
    }

    /** @param id identifiant de la question @return options proposées pour la question */
    @GetMapping("/api/questions/{id}/options")
    public List<DecisionOptionDto> getOptionsForQuestion(@PathVariable Long id) {
        return decisionQuestionService.getOptionForQuestion(id);
    }

    /** @param id identifiant de la question @return question correspondante */
    @GetMapping("/api/questions/{id}")
    public DecisionQuestionDto getQuestionById(@PathVariable Long id) {
        return decisionQuestionService.findById(id);
    }
}
