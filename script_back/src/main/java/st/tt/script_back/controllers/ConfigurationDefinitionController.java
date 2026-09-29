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

import st.tt.script_back.dto.ConfigurationDefinitionDetailDto;
import st.tt.script_back.dto.ConfigurationDefinitionDto;
import st.tt.script_back.services.ConfigurationDefinitionService;

/** Administre les définitions de configuration utilisables par les recettes. */
@RestController
@RequestMapping("/api/configuration-definitions")
public class ConfigurationDefinitionController {

    private final ConfigurationDefinitionService configurationDefinitionService;

    /** @param configurationDefinitionService service métier des définitions */
    public ConfigurationDefinitionController(ConfigurationDefinitionService configurationDefinitionService) {
        this.configurationDefinitionService = configurationDefinitionService;
    }

    /** @return définitions de configuration disponibles */
    @GetMapping
    public List<ConfigurationDefinitionDto> getDefinitions() {
        return configurationDefinitionService.getConfigurationDefinitions();
    }

    /** @param id identifiant de la définition @return détail de la définition */
    @GetMapping("/{id}")
    public ConfigurationDefinitionDetailDto getDefinition(@PathVariable Long id) {
        return configurationDefinitionService.getConfigurationDefinitionDetail(id);
    }

    /** @param request définition à créer @return définition créée */
    @PostMapping
    public ConfigurationDefinitionDto createDefinition(@RequestBody ConfigurationDefinitionDto request) {
        return configurationDefinitionService.createConfigurationDefinition(request);
    }

    /** @param id identifiant de la définition @param request nouvelles données @return définition mise à jour */
    @PutMapping("/{id}")
    public ConfigurationDefinitionDto updateDefinition(@PathVariable Long id, @RequestBody ConfigurationDefinitionDto request) {
        return configurationDefinitionService.updateConfigurationDefinition(id, request);
    }

    /** @param id identifiant de la définition à supprimer */
    @DeleteMapping("/{id}")
    public void deleteDefinition(@PathVariable Long id) {
        configurationDefinitionService.deleteConfigurationDefinition(id);
    }
}
