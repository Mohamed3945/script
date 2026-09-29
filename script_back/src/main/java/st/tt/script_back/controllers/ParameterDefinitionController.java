package st.tt.script_back.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import st.tt.script_back.dto.ParameterDefinitionDto;
import st.tt.script_back.dto.ParameterDefinitionMoveRequestDto;
import st.tt.script_back.dto.ParameterDefinitionReorderRequestDto;
import st.tt.script_back.enums.ParameterScope;
import st.tt.script_back.services.ParameterDefinitionService;

/** Administre les définitions de paramètres et leur organisation dans les groupes. */
@RestController
@RequestMapping("/api/parameter-definitions")
public class ParameterDefinitionController {

    private final ParameterDefinitionService parameterDefinitionService;

    /** @param parameterDefinitionService service métier des définitions de paramètres */
    public ParameterDefinitionController(ParameterDefinitionService parameterDefinitionService) {
        this.parameterDefinitionService = parameterDefinitionService;
    }

    /** @param stepType filtre optionnel sur le périmètre d'étape @return définitions disponibles */
    @GetMapping
    public List<ParameterDefinitionDto> getParameterDefinitions(
            @RequestParam(required = false) ParameterScope stepType) {
        return parameterDefinitionService.getParameterDefinitions(stepType);
    }

    /** @param id identifiant de la définition @return définition demandée */
    @GetMapping("/{id}")
    public ParameterDefinitionDto getParameterDefinition(@PathVariable Long id) {
        return parameterDefinitionService.getParameterDefinition(id);
    }

    /** @param request définition à créer @return définition créée */
    @PostMapping
    public ParameterDefinitionDto createParameterDefinition(@RequestBody ParameterDefinitionDto request) {
        return parameterDefinitionService.createParameterDefinition(request);
    }

    /** @param id identifiant de la définition @param request nouvelles données @return définition mise à jour */
    @PutMapping("/{id}")
    public ParameterDefinitionDto updateParameterDefinition(
            @PathVariable Long id,
            @RequestBody ParameterDefinitionDto request) {
        return parameterDefinitionService.updateParameterDefinition(id, request);
    }

    /** @param id identifiant de la définition à supprimer */
    @DeleteMapping("/{id}")
    public void deleteParameterDefinition(@PathVariable Long id) {
        parameterDefinitionService.deleteParameterDefinition(id);
    }

    /** @param request ordre des définitions dans leur groupe */
    @PostMapping("/reorder")
    public void reorderDefinitions(@RequestBody ParameterDefinitionReorderRequestDto request) {
        parameterDefinitionService.reorderDefinitionsInGroup(request);
    }

    /** @param request définition et groupe de destination @return définition déplacée */
    @PostMapping("/move")
    public ParameterDefinitionDto moveDefinition(@RequestBody ParameterDefinitionMoveRequestDto request) {
        return parameterDefinitionService.moveDefinitionToGroup(request);
    }

    /** @param configDefId identifiant de la configuration @return définitions associées */
    @GetMapping("/by-configuration-definition/{configDefId}")
    public List<ParameterDefinitionDto> getByConfigurationDefinition(
            @PathVariable Long configDefId) {
        return parameterDefinitionService.findByConfigurationDefinitionId(configDefId);
    }
}


