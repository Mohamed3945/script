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

/** Gère les chambres d'une machine et les capacités qui leur sont associées. */
@RestController
@RequestMapping("/api")
public class ChamberController {

    private final ChamberService chamberService;

    /** @param chamberService service métier des chambres */
    public ChamberController(ChamberService chamberService) {
        this.chamberService = chamberService;
    }

    /** @param machineId machine parente @return chambres rattachées à la machine */
    @GetMapping("/machines/{machineId}/chambers")
    public List<ChamberDto> getMachineChambers(@PathVariable Long machineId) {
        return chamberService.getChambersByMachine(machineId);
    }

    /** @param id identifiant de la chambre @return détail de la chambre */
    @GetMapping("/chambers/{id}")
    public ChamberDetailDto getChamber(@PathVariable Long id) {
        return chamberService.getChamberDetail(id);
    }

    /** @param machineId machine parente @param request données de la chambre @return chambre créée */
    @PostMapping("/machines/{machineId}/chambers")
    public ChamberDto createChamber(@PathVariable Long machineId, @RequestBody ChamberDto request) {
        return chamberService.createChamber(machineId, request);
    }

    /** @param id identifiant de la chambre @param request nouvelles données @return chambre mise à jour */
    @PutMapping("/chambers/{id}")
    public ChamberDto updateChamber(@PathVariable Long id, @RequestBody ChamberDto request) {
        return chamberService.updateChamber(id, request);
    }

    /** @param id identifiant de la chambre à supprimer */
    @DeleteMapping("/chambers/{id}")
    public void deleteChamber(@PathVariable Long id) {
        chamberService.deleteChamber(id);
    }

    /** @param chamberId chambre cible @param capabilityId capacité à associer */
    @PostMapping("/chambers/{chamberId}/capabilities/{capabilityId}")
    public void addCapabilityToChamber(@PathVariable Long chamberId, @PathVariable Long capabilityId) {
        chamberService.addCapabilityToChamber(chamberId, capabilityId);
    }

    /** @param chamberId chambre cible @param capabilityId capacité à dissocier */
    @DeleteMapping("/chambers/{chamberId}/capabilities/{capabilityId}")
    public void removeCapabilityFromChamber(@PathVariable Long chamberId, @PathVariable Long capabilityId) {
        chamberService.removeCapabilityFromChamber(chamberId, capabilityId);
    }
}
