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

import st.tt.script_back.dto.ChamberDetailDto;
import st.tt.script_back.dto.ChamberDto;
import st.tt.script_back.services.ChamberService;

@RestController
@RequestMapping("/api")
public class ChamberController {

    private final ChamberService chamberService;

    public ChamberController(ChamberService chamberService) {
        this.chamberService = chamberService;
    }

    @GetMapping("/machines/{machineId}/chambers")
    public List<ChamberDto> getMachineChambers(@PathVariable Long machineId) {
        return chamberService.getChambersByMachine(machineId);
    }

    @GetMapping("/chambers/{id}")
    public ChamberDetailDto getChamber(@PathVariable Long id) {
        return chamberService.getChamberDetail(id);
    }

    @PostMapping("/machines/{machineId}/chambers")
    public ChamberDto createChamber(@PathVariable Long machineId, @RequestBody ChamberDto request) {
        return chamberService.createChamber(machineId, request);
    }

    @PutMapping("/chambers/{id}")
    public ChamberDto updateChamber(@PathVariable Long id, @RequestBody ChamberDto request) {
        return chamberService.updateChamber(id, request);
    }

    @DeleteMapping("/chambers/{id}")
    public void deleteChamber(@PathVariable Long id) {
        chamberService.deleteChamber(id);
    }

    @PostMapping("/chambers/{chamberId}/capabilities/{capabilityId}")
    public void addCapabilityToChamber(@PathVariable Long chamberId, @PathVariable Long capabilityId) {
        chamberService.addCapabilityToChamber(chamberId, capabilityId);
    }

    @DeleteMapping("/chambers/{chamberId}/capabilities/{capabilityId}")
    public void removeCapabilityFromChamber(@PathVariable Long chamberId, @PathVariable Long capabilityId) {
        chamberService.removeCapabilityFromChamber(chamberId, capabilityId);
    }
}
