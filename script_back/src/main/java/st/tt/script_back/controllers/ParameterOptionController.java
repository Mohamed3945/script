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

import st.tt.script_back.dto.ParameterOptionDto;
import st.tt.script_back.services.ParameterOptionService;

/** Gère les valeurs proposées pour les paramètres de type énuméré. */
@RestController
@RequestMapping("/api")
public class ParameterOptionController {

    private final ParameterOptionService parameterOptionService;

    /** @param parameterOptionService service métier des options */
    public ParameterOptionController(ParameterOptionService parameterOptionService) {
        this.parameterOptionService = parameterOptionService;
    }

    /** @param definitionId définition du paramètre @return options de la définition */
    @GetMapping("/parameter-definitions/{definitionId}/options")
    public List<ParameterOptionDto> getOptionsByDefinitionId(@PathVariable Long definitionId) {
        return parameterOptionService.getOptionsByDefinitionId(definitionId);
    }

    /** @param request option à créer @return option créée */
    @PostMapping("/parameter-options")
    public ParameterOptionDto createParameterOption(@RequestBody ParameterOptionDto request) {
        return parameterOptionService.createParameterOption(request);
    }

    /** @param id identifiant de l'option @param request nouvelles données @return option mise à jour */
    @PutMapping("/parameter-options/{id}")
    public ParameterOptionDto updateParameterOption(
            @PathVariable Long id,
            @RequestBody ParameterOptionDto request) {
        return parameterOptionService.updateParameterOption(id, request);
    }

    /** @param id identifiant de l'option à supprimer */
    @DeleteMapping("/parameter-options/{id}")
    public void deleteParameterOption(@PathVariable Long id) {
        parameterOptionService.deleteParameterOption(id);
    }
}
