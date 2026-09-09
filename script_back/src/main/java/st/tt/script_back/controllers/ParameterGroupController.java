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

@RestController
@RequestMapping("/api/parameter-groups")
public class ParameterGroupController {

    private final ParameterGroupService parameterGroupService;

    public ParameterGroupController(ParameterGroupService parameterGroupService) {
        this.parameterGroupService = parameterGroupService;
    }

    @GetMapping
    public List<ParameterGroupDto> getGroups(@RequestParam ParameterScope stepType) {
        return parameterGroupService.getGroups(stepType);
    }

    @PostMapping
    public ParameterGroupDto createGroup(@RequestBody ParameterGroupDto request) {
        return parameterGroupService.createGroup(request);
    }

    @PostMapping("/reorder")
    public void reorderGroups(@RequestBody ParameterGroupReorderRequestDto request) {
        parameterGroupService.reorderGroups(request);
    }
}

