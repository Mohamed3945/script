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

/**
 * ParameterOptionController class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@RestController
@RequestMapping("/api")
public class ParameterOptionController {

    private final ParameterOptionService parameterOptionService;

    /**
     * Executes ParameterOptionController.
     *
     * @param parameterOptionService input argument consumed by ParameterOptionController.
     */
    public ParameterOptionController(ParameterOptionService parameterOptionService) {
        this.parameterOptionService = parameterOptionService;
    }

    /**
     * Executes getOptionsByDefinitionId.
     *
     * @param definitionId input argument consumed by getOptionsByDefinitionId.
     * @return computed List<ParameterOptionDto> result returned by getOptionsByDefinitionId.
     */
    @GetMapping("/parameter-definitions/{definitionId}/options")
    public List<ParameterOptionDto> getOptionsByDefinitionId(@PathVariable Long definitionId) {
        return parameterOptionService.getOptionsByDefinitionId(definitionId);
    }

    /**
     * Executes createParameterOption.
     *
     * @param request input argument consumed by createParameterOption.
     * @return computed ParameterOptionDto result returned by createParameterOption.
     */
    @PostMapping("/parameter-options")
    public ParameterOptionDto createParameterOption(@RequestBody ParameterOptionDto request) {
        return parameterOptionService.createParameterOption(request);
    }

    /**
     * Executes updateParameterOption.
     *
     * @param id input argument consumed by updateParameterOption.
     * @param request input argument consumed by updateParameterOption.
     * @return computed ParameterOptionDto result returned by updateParameterOption.
     */
    @PutMapping("/parameter-options/{id}")
    public ParameterOptionDto updateParameterOption(
            @PathVariable Long id,
            @RequestBody ParameterOptionDto request) {
        return parameterOptionService.updateParameterOption(id, request);
    }

    /**
     * Executes deleteParameterOption.
     *
     * @param id input argument consumed by deleteParameterOption.
     */
    @DeleteMapping("/parameter-options/{id}")
    public void deleteParameterOption(@PathVariable Long id) {
        parameterOptionService.deleteParameterOption(id);
    }
}
