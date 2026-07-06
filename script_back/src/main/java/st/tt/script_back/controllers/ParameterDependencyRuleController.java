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
import st.tt.script_back.dto.ParameterDependencyRuleViewDto;
import st.tt.script_back.services.ParameterDependencyRuleQueryService;
import st.tt.script_back.services.ParameterDependencyRuleService;

/**
 * ParameterDependencyRuleController class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@RestController
@RequestMapping("/api/parameter-dependency-rules")
public class ParameterDependencyRuleController {

    private final ParameterDependencyRuleService parameterDependencyRuleService;
    private final ParameterDependencyRuleQueryService parameterDependencyRuleQueryService;

    /**
     * Executes ParameterDependencyRuleController.
     *
     * @param parameterDependencyRuleService input argument consumed by ParameterDependencyRuleController.
     * @param parameterDependencyRuleQueryService input argument consumed by ParameterDependencyRuleController.
     */
    public ParameterDependencyRuleController(
            ParameterDependencyRuleService parameterDependencyRuleService,
            ParameterDependencyRuleQueryService parameterDependencyRuleQueryService) {
        this.parameterDependencyRuleService = parameterDependencyRuleService;
        this.parameterDependencyRuleQueryService = parameterDependencyRuleQueryService;
    }

    /**
     * Executes getParameterDependencyRules.
     * @return computed List<ParameterDependencyRuleViewDto> result returned by getParameterDependencyRules.
     */
    @GetMapping
    public List<ParameterDependencyRuleViewDto> getParameterDependencyRules() {
        return parameterDependencyRuleQueryService.getRules();
    }

    /**
     * Executes getParameterDependencyRule.
     *
     * @param id input argument consumed by getParameterDependencyRule.
     * @return computed ParameterDependencyRuleViewDto result returned by getParameterDependencyRule.
     */
    @GetMapping("/{id}")
    public ParameterDependencyRuleViewDto getParameterDependencyRule(@PathVariable Long id) {
        return parameterDependencyRuleQueryService.getRule(id);
    }

    /**
     * Executes createParameterDependencyRule.
     *
     * @param request input argument consumed by createParameterDependencyRule.
     * @return computed ParameterDependencyRuleDto result returned by createParameterDependencyRule.
     */
    @PostMapping
    public ParameterDependencyRuleDto createParameterDependencyRule(@RequestBody ParameterDependencyRuleDto request) {
        return parameterDependencyRuleService.createParameterDependencyRule(request);
    }

    /**
     * Executes updateParameterDependencyRule.
     *
     * @param id input argument consumed by updateParameterDependencyRule.
     * @param request input argument consumed by updateParameterDependencyRule.
     * @return computed ParameterDependencyRuleDto result returned by updateParameterDependencyRule.
     */
    @PutMapping("/{id}")
    public ParameterDependencyRuleDto updateParameterDependencyRule(
            @PathVariable Long id,
            @RequestBody ParameterDependencyRuleDto request) {
        return parameterDependencyRuleService.updateParameterDependencyRule(id, request);
    }

    /**
     * Executes deleteParameterDependencyRule.
     *
     * @param id input argument consumed by deleteParameterDependencyRule.
     */
    @DeleteMapping("/{id}")
    public void deleteParameterDependencyRule(@PathVariable Long id) {
        parameterDependencyRuleService.deleteParameterDependencyRule(id);
    }
}
