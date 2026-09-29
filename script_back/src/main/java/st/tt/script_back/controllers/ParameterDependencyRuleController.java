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

/** Administre les règles qui activent ou désactivent des paramètres selon d'autres paramètres. */
@RestController
@RequestMapping("/api/parameter-dependency-rules")
public class ParameterDependencyRuleController {

    private final ParameterDependencyRuleService parameterDependencyRuleService;
    private final ParameterDependencyRuleQueryService parameterDependencyRuleQueryService;

    /**
     * @param parameterDependencyRuleService service de création et modification
     * @param parameterDependencyRuleQueryService service de consultation enrichie
     */
    public ParameterDependencyRuleController(
            ParameterDependencyRuleService parameterDependencyRuleService,
            ParameterDependencyRuleQueryService parameterDependencyRuleQueryService) {
        this.parameterDependencyRuleService = parameterDependencyRuleService;
        this.parameterDependencyRuleQueryService = parameterDependencyRuleQueryService;
    }

    /** @return règles enrichies pour l'affichage et l'analyse des dépendances */
    @GetMapping
    public List<ParameterDependencyRuleViewDto> getParameterDependencyRules() {
        return parameterDependencyRuleQueryService.getRules();
    }

    /** @param id identifiant de la règle @return règle enrichie demandée */
    @GetMapping("/{id}")
    public ParameterDependencyRuleViewDto getParameterDependencyRule(@PathVariable Long id) {
        return parameterDependencyRuleQueryService.getRule(id);
    }

    /** @param request règle à créer @return règle créée */
    @PostMapping
    public ParameterDependencyRuleDto createParameterDependencyRule(@RequestBody ParameterDependencyRuleDto request) {
        return parameterDependencyRuleService.createParameterDependencyRule(request);
    }

    /** @param id identifiant de la règle @param request nouvelles données @return règle mise à jour */
    @PutMapping("/{id}")
    public ParameterDependencyRuleDto updateParameterDependencyRule(
            @PathVariable Long id,
            @RequestBody ParameterDependencyRuleDto request) {
        return parameterDependencyRuleService.updateParameterDependencyRule(id, request);
    }

    /** @param id identifiant de la règle à supprimer */
    @DeleteMapping("/{id}")
    public void deleteParameterDependencyRule(@PathVariable Long id) {
        parameterDependencyRuleService.deleteParameterDependencyRule(id);
    }
}
