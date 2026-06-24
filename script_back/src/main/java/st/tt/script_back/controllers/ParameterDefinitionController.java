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

import st.tt.script_back.dto.ParameterDefinitionDto;
import st.tt.script_back.services.ParameterDefinitionService;

@RestController
@RequestMapping("/api/parameter-definitions")
public class ParameterDefinitionController {

    private final ParameterDefinitionService parameterDefinitionService;

    public ParameterDefinitionController(ParameterDefinitionService parameterDefinitionService) {
        this.parameterDefinitionService = parameterDefinitionService;
    }

    @GetMapping
    public List<ParameterDefinitionDto> getParameterDefinitions() {
        return parameterDefinitionService.getParameterDefinitions();
    }

    @GetMapping("/{id}")
    public ParameterDefinitionDto getParameterDefinition(@PathVariable Long id) {
        return parameterDefinitionService.getParameterDefinition(id);
    }

    @PostMapping
    public ParameterDefinitionDto createParameterDefinition(@RequestBody ParameterDefinitionDto request) {
        return parameterDefinitionService.createParameterDefinition(request);
    }

    @PutMapping("/{id}")
    public ParameterDefinitionDto updateParameterDefinition(
            @PathVariable Long id,
            @RequestBody ParameterDefinitionDto request) {
        return parameterDefinitionService.updateParameterDefinition(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteParameterDefinition(@PathVariable Long id) {
        parameterDefinitionService.deleteParameterDefinition(id);
    }
}