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

@RestController
@RequestMapping("/api/configuration-definitions")
public class ConfigurationDefinitionController {

    private final ConfigurationDefinitionService configurationDefinitionService;

    public ConfigurationDefinitionController(ConfigurationDefinitionService configurationDefinitionService) {
        this.configurationDefinitionService = configurationDefinitionService;
    }

    @GetMapping
    public List<ConfigurationDefinitionDto> getDefinitions() {
        return configurationDefinitionService.getConfigurationDefinitions();
    }

    @GetMapping("/{id}")
    public ConfigurationDefinitionDetailDto getDefinition(@PathVariable Long id) {
        return configurationDefinitionService.getConfigurationDefinitionDetail(id);
    }

    @PostMapping
    public ConfigurationDefinitionDto createDefinition(@RequestBody ConfigurationDefinitionDto request) {
        return configurationDefinitionService.createConfigurationDefinition(request);
    }

    @PutMapping("/{id}")
    public ConfigurationDefinitionDto updateDefinition(@PathVariable Long id, @RequestBody ConfigurationDefinitionDto request) {
        return configurationDefinitionService.updateConfigurationDefinition(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteDefinition(@PathVariable Long id) {
        configurationDefinitionService.deleteConfigurationDefinition(id);
    }
}
