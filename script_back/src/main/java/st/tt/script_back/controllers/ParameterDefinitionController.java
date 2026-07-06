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
import st.tt.script_back.enums.StepType;
import st.tt.script_back.services.ParameterDefinitionService;

/**
 * ParameterDefinitionController class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@RestController
@RequestMapping("/api/parameter-definitions")
public class ParameterDefinitionController {

    private final ParameterDefinitionService parameterDefinitionService;

    /**
     * Executes ParameterDefinitionController.
     *
     * @param parameterDefinitionService input argument consumed by ParameterDefinitionController.
     */
    public ParameterDefinitionController(ParameterDefinitionService parameterDefinitionService) {
        this.parameterDefinitionService = parameterDefinitionService;
    }

    /**
     * Executes getParameterDefinitions.
     *
         * @param stepType input argument consumed by getParameterDefinitions.
     * @return computed List<ParameterDefinitionDto> result returned by getParameterDefinitions.
     */
    @GetMapping
    public List<ParameterDefinitionDto> getParameterDefinitions(
            @RequestParam(required = false) StepType stepType) {
        return parameterDefinitionService.getParameterDefinitions(stepType);
    }

    /**
     * Executes getParameterDefinition.
     *
     * @param id input argument consumed by getParameterDefinition.
     * @return computed ParameterDefinitionDto result returned by getParameterDefinition.
     */
    @GetMapping("/{id}")
    public ParameterDefinitionDto getParameterDefinition(@PathVariable Long id) {
        return parameterDefinitionService.getParameterDefinition(id);
    }

    /**
     * Executes createParameterDefinition.
     *
     * @param request input argument consumed by createParameterDefinition.
     * @return computed ParameterDefinitionDto result returned by createParameterDefinition.
     */
    @PostMapping
    public ParameterDefinitionDto createParameterDefinition(@RequestBody ParameterDefinitionDto request) {
        return parameterDefinitionService.createParameterDefinition(request);
    }

    /**
     * Executes updateParameterDefinition.
     *
     * @param id input argument consumed by updateParameterDefinition.
     * @param request input argument consumed by updateParameterDefinition.
     * @return computed ParameterDefinitionDto result returned by updateParameterDefinition.
     */
    @PutMapping("/{id}")
    public ParameterDefinitionDto updateParameterDefinition(
            @PathVariable Long id,
            @RequestBody ParameterDefinitionDto request) {
        return parameterDefinitionService.updateParameterDefinition(id, request);
    }

    /**
     * Executes deleteParameterDefinition.
     *
     * @param id input argument consumed by deleteParameterDefinition.
     */
    @DeleteMapping("/{id}")
    public void deleteParameterDefinition(@PathVariable Long id) {
        parameterDefinitionService.deleteParameterDefinition(id);
    }
}
