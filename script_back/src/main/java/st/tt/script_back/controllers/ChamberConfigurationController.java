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

import st.tt.script_back.dto.ChamberConfigurationDto;
import st.tt.script_back.services.ChamberConfigurationService;

/** Gère les configurations déclarées pour les chambres. */
@RestController
@RequestMapping("/api")
public class ChamberConfigurationController {

    private final ChamberConfigurationService chamberConfigurationService;

    /** @param chamberConfigurationService service métier des configurations de chambre */
    public ChamberConfigurationController(ChamberConfigurationService chamberConfigurationService) {
        this.chamberConfigurationService = chamberConfigurationService;
    }

    /** @param chamberId chambre cible @return configurations de la chambre */
    @GetMapping("/chambers/{chamberId}/configurations")
    public List<ChamberConfigurationDto> getChamberConfigurations(@PathVariable Long chamberId) {
        return chamberConfigurationService.getByChamber(chamberId);
    }

    /** @param id identifiant de l'association @return configuration demandée */
    @GetMapping("/chamber-configurations/{id}")
    public ChamberConfigurationDto getChamberConfiguration(@PathVariable Long id) {
        return chamberConfigurationService.getById(id);
    }

    /** @param chamberId chambre cible @param request configuration à créer @return association créée */
    @PostMapping("/chambers/{chamberId}/configurations")
    public ChamberConfigurationDto createChamberConfiguration(
            @PathVariable Long chamberId,
            @RequestBody ChamberConfigurationDto request) {
        return chamberConfigurationService.createForChamber(chamberId, request);
    }

    /** @param id identifiant de l'association @param request nouvelles données @return association mise à jour */
    @PutMapping("/chamber-configurations/{id}")
    public ChamberConfigurationDto updateChamberConfiguration(
            @PathVariable Long id,
            @RequestBody ChamberConfigurationDto request) {
        return chamberConfigurationService.update(id, request);
    }

    /** @param id identifiant de l'association à supprimer */
    @DeleteMapping("/chamber-configurations/{id}")
    public void deleteChamberConfiguration(@PathVariable Long id) {
        chamberConfigurationService.delete(id);
    }
}
