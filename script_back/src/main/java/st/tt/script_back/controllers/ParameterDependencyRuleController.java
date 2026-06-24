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

import st.tt.script_back.dto.ParameterDependencyRuleDto;
import st.tt.script_back.services.ParameterDependencyRuleService;

@RestController
@RequestMapping("/api/parameter-dependency-rules")
public class ParameterDependencyRuleController {

    private final ParameterDependencyRuleService parameterDependencyRuleService;

    public ParameterDependencyRuleController(ParameterDependencyRuleService parameterDependencyRuleService) {
        this.parameterDependencyRuleService = parameterDependencyRuleService;
    }

    @GetMapping
    public List<ParameterDependencyRuleDto> getParameterDependencyRules() {
        return parameterDependencyRuleService.getParameterDependencyRules();
    }

    @GetMapping("/{id}")
    public ParameterDependencyRuleDto getParameterDependencyRule(@PathVariable Long id) {
        return parameterDependencyRuleService.getParameterDependencyRule(id);
    }

    @PostMapping
    public ParameterDependencyRuleDto createParameterDependencyRule(@RequestBody ParameterDependencyRuleDto request) {
        return parameterDependencyRuleService.createParameterDependencyRule(request);
    }

    @PutMapping("/{id}")
    public ParameterDependencyRuleDto updateParameterDependencyRule(
            @PathVariable Long id,
            @RequestBody ParameterDependencyRuleDto request) {
        return parameterDependencyRuleService.updateParameterDependencyRule(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteParameterDependencyRule(@PathVariable Long id) {
        parameterDependencyRuleService.deleteParameterDependencyRule(id);
    }
}