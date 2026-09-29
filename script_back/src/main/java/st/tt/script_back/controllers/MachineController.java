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

/** Gère le catalogue des machines et leur administration. */
@RestController
@RequestMapping("/api/machines")
public class MachineController {

    private final MachineService machineService;

    /** @param machineService service métier des machines */
    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    /** @return toutes les machines disponibles */
    @GetMapping
    public List<MachineDto> getMachines() {
        return machineService.getMachines();
    }

    /** @param id identifiant de la machine @return détail de la machine */
    @GetMapping("/{id}")
    public MachineDetailDto getMachine(@PathVariable Long id) {
        return machineService.getMachineDetail(id);
    }

    /** @param request données de la machine @return machine créée */
    @PostMapping
    public MachineDto createMachine(@RequestBody MachineDto request) {
        return machineService.createMachine(request);
    }

    /** @param id identifiant de la machine @param request nouvelles données @return machine mise à jour */
    @PutMapping("/{id}")
    public MachineDto updateMachine(@PathVariable Long id, @RequestBody MachineDto request) {
        return machineService.updateMachine(id, request);
    }

    /** @param id identifiant de la machine à supprimer */
    @DeleteMapping("/{id}")
    public void deleteMachine(@PathVariable Long id) {
        machineService.deleteMachine(id);
    }
}
