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

import st.tt.script_back.dto.ChamberCapabilityDto;
import st.tt.script_back.services.ChamberCapabilityService;

/** Administre les capacités pouvant être exigées par une recette ou une chambre. */
@RestController
@RequestMapping("/api/chamber-capabilities")
public class ChamberCapabilityController {

    private final ChamberCapabilityService chamberCapabilityService;

    /** @param chamberCapabilityService service métier des capacités */
    public ChamberCapabilityController(ChamberCapabilityService chamberCapabilityService) {
        this.chamberCapabilityService = chamberCapabilityService;
    }

    /** @return liste des capacités */
    @GetMapping
    public List<ChamberCapabilityDto> getCapabilities() {
        return chamberCapabilityService.getCapabilities();
    }

    /** @param id identifiant de la capacité @return capacité demandée */
    @GetMapping("/{id}")
    public ChamberCapabilityDto getCapability(@PathVariable Long id) {
        return chamberCapabilityService.getCapability(id);
    }

    /** @param request définition de la capacité @return capacité créée */
    @PostMapping
    public ChamberCapabilityDto createCapability(@RequestBody ChamberCapabilityDto request) {
        return chamberCapabilityService.createCapability(request);
    }

    /** @param id identifiant de la capacité @param request nouvelles données @return capacité mise à jour */
    @PutMapping("/{id}")
    public ChamberCapabilityDto updateCapability(@PathVariable Long id, @RequestBody ChamberCapabilityDto request) {
        return chamberCapabilityService.updateCapability(id, request);
    }

    /** @param id identifiant de la capacité à supprimer */
    @DeleteMapping("/{id}")
    public void deleteCapability(@PathVariable Long id) {
        chamberCapabilityService.deleteCapability(id);
    }
}
