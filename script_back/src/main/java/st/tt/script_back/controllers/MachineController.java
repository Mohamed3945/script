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

import st.tt.script_back.dto.MachineDetailDto;
import st.tt.script_back.dto.MachineDto;
import st.tt.script_back.services.MachineService;

@RestController
@RequestMapping("/api/machines")
public class MachineController {

    private final MachineService machineService;

    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    @GetMapping
    public List<MachineDto> getMachines() {
        return machineService.getMachines();
    }

    @GetMapping("/{id}")
    public MachineDetailDto getMachine(@PathVariable Long id) {
        return machineService.getMachineDetail(id);
    }

    @PostMapping
    public MachineDto createMachine(@RequestBody MachineDto request) {
        return machineService.createMachine(request);
    }

    @PutMapping("/{id}")
    public MachineDto updateMachine(@PathVariable Long id, @RequestBody MachineDto request) {
        return machineService.updateMachine(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteMachine(@PathVariable Long id) {
        machineService.deleteMachine(id);
    }
}
