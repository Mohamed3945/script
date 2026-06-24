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

@RestController
@RequestMapping("/api")
public class ParameterOptionController {

    private final ParameterOptionService parameterOptionService;

    public ParameterOptionController(ParameterOptionService parameterOptionService) {
        this.parameterOptionService = parameterOptionService;
    }

    @GetMapping("/parameter-definitions/{definitionId}/options")
    public List<ParameterOptionDto> getOptionsByDefinitionId(@PathVariable Long definitionId) {
        return parameterOptionService.getOptionsByDefinitionId(definitionId);
    }

    @PostMapping("/parameter-options")
    public ParameterOptionDto createParameterOption(@RequestBody ParameterOptionDto request) {
        return parameterOptionService.createParameterOption(request);
    }

    @PutMapping("/parameter-options/{id}")
    public ParameterOptionDto updateParameterOption(
            @PathVariable Long id,
            @RequestBody ParameterOptionDto request) {
        return parameterOptionService.updateParameterOption(id, request);
    }

    @DeleteMapping("/parameter-options/{id}")
    public void deleteParameterOption(@PathVariable Long id) {
        parameterOptionService.deleteParameterOption(id);
    }
}