package st.tt.script_back.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import st.tt.script_back.dto.ParameterGroupDto;
import st.tt.script_back.dto.ParameterGroupReorderRequestDto;
import st.tt.script_back.enums.ParameterScope;
import st.tt.script_back.services.ParameterGroupService;

/** Gère les groupes qui organisent les paramètres par type d'étape. */
@RestController
@RequestMapping("/api/parameter-groups")
public class ParameterGroupController {

    private final ParameterGroupService parameterGroupService;

    /** @param parameterGroupService service métier des groupes */
    public ParameterGroupController(ParameterGroupService parameterGroupService) {
        this.parameterGroupService = parameterGroupService;
    }

    /** @param stepType périmètre d'étape concerné @return groupes dans leur ordre d'affichage */
    @GetMapping
    public List<ParameterGroupDto> getGroups(@RequestParam ParameterScope stepType) {
        return parameterGroupService.getGroups(stepType);
    }

    /** @param request données du groupe à créer @return groupe créé */
    @PostMapping
    public ParameterGroupDto createGroup(@RequestBody ParameterGroupDto request) {
        return parameterGroupService.createGroup(request);
    }

    /** @param request nouvel ordre des groupes */
    @PostMapping("/reorder")
    public void reorderGroups(@RequestBody ParameterGroupReorderRequestDto request) {
        parameterGroupService.reorderGroups(request);
    }
}

