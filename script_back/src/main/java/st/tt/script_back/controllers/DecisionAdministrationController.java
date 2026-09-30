package st.tt.script_back.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import st.tt.script_back.dto.DecisionOptionAdminRequestDto;
import st.tt.script_back.dto.DecisionOptionDto;
import st.tt.script_back.dto.DecisionQuestionAdminRequestDto;
import st.tt.script_back.dto.DecisionQuestionDto;
import st.tt.script_back.dto.DecisionResultProfileDto;
import st.tt.script_back.dto.DecisionResultProfileAdminRequestDto;
import st.tt.script_back.dto.DecisionTransitionAdminDto;
import st.tt.script_back.dto.DecisionTransitionAdminRequestDto;
import st.tt.script_back.services.DecisionAdministrationService;

@RestController
@RequestMapping("/api/admin")
public class DecisionAdministrationController {

    private final DecisionAdministrationService administrationService;

    public DecisionAdministrationController(DecisionAdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    @GetMapping("/questions")
    public List<DecisionQuestionDto> getQuestions() {
        return administrationService.getQuestions();
    }

    @PostMapping("/questions")
    public DecisionQuestionDto createQuestion(@RequestBody DecisionQuestionAdminRequestDto request) {
        return administrationService.createQuestion(request);
    }

    @PutMapping("/questions/{id}")
    public DecisionQuestionDto updateQuestion(@PathVariable Long id, @RequestBody DecisionQuestionAdminRequestDto request) {
        return administrationService.updateQuestion(id, request);
    }

    @DeleteMapping("/questions/{id}")
    public void deleteQuestion(@PathVariable Long id) {
        administrationService.deleteQuestion(id);
    }

    @PostMapping("/questions/{questionId}/options")
    public DecisionOptionDto createOption(@PathVariable Long questionId, @RequestBody DecisionOptionAdminRequestDto request) {
        return administrationService.createOption(questionId, request);
    }

    @PutMapping("/options/{id}")
    public DecisionOptionDto updateOption(@PathVariable Long id, @RequestBody DecisionOptionAdminRequestDto request) {
        return administrationService.updateOption(id, request);
    }

    @DeleteMapping("/options/{id}")
    public void deleteOption(@PathVariable Long id) {
        administrationService.deleteOption(id);
    }

    @GetMapping("/transitions")
    public List<DecisionTransitionAdminDto> getTransitions() {
        return administrationService.getTransitions();
    }

    @PostMapping("/transitions")
    public DecisionTransitionAdminDto createTransition(@RequestBody DecisionTransitionAdminRequestDto request) {
        return administrationService.createTransition(request);
    }

    @PutMapping("/transitions/{id}")
    public DecisionTransitionAdminDto updateTransition(@PathVariable Long id, @RequestBody DecisionTransitionAdminRequestDto request) {
        return administrationService.updateTransition(id, request);
    }

    @DeleteMapping("/transitions/{id}")
    public void deleteTransition(@PathVariable Long id) {
        administrationService.deleteTransition(id);
    }

    @GetMapping("/result-profiles")
    public List<DecisionResultProfileDto> getResultProfiles() {
        return administrationService.getResultProfiles();
    }

    @PostMapping("/result-profiles")
    public DecisionResultProfileDto createResultProfile(@RequestBody DecisionResultProfileAdminRequestDto request) {
        return administrationService.createResultProfile(request);
    }

    @PutMapping("/result-profiles/{id}")
    public DecisionResultProfileDto updateResultProfile(
            @PathVariable Long id,
            @RequestBody DecisionResultProfileAdminRequestDto request) {
        return administrationService.updateResultProfile(id, request);
    }

    @DeleteMapping("/result-profiles/{id}")
    public void deleteResultProfile(@PathVariable Long id) {
        administrationService.deleteResultProfile(id);
    }
}