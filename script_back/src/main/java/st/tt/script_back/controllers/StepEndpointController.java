package st.tt.script_back.controllers;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import st.tt.script_back.dto.StepEndpointDto;
import st.tt.script_back.services.StepEndpointService;

/**
 * Gère l'endpoint d'arrêt d'une étape.
 *
 * Un endpoint est la condition de fin d'un step dans une recette Soft Machine.
 * Il peut être :
 *   - vide (time-based) → <ENDPOINT /> dans le XML
 *   - avec conditions   → <ENDPOINT Clause="AND|OR"><CONDITION .../></ENDPOINT>
 *
 * Routes :
 *   GET    /api/steps/{stepId}/endpoint  → lire l'endpoint (DTO vide si absent)
 *   PUT    /api/steps/{stepId}/endpoint  → créer ou remplacer l'endpoint
 *   DELETE /api/steps/{stepId}/endpoint  → supprimer l'endpoint (retour time-based)
 */
@RestController
@RequestMapping("/api/steps/{stepId}/endpoint")
public class StepEndpointController {

    private final StepEndpointService stepEndpointService;

    /** @param stepEndpointService service métier des endpoints d'étape */
    public StepEndpointController(StepEndpointService stepEndpointService) {
        this.stepEndpointService = stepEndpointService;
    }

    /** @param stepId étape concernée @return endpoint actuel, éventuellement vide */
    @GetMapping
    public StepEndpointDto getEndpoint(@PathVariable Long stepId) {
        return stepEndpointService.getEndpoint(stepId);
    }

    /** @param stepId étape concernée @param request endpoint à créer ou remplacer @return endpoint enregistré */
    @PutMapping
    public StepEndpointDto upsertEndpoint(
            @PathVariable Long stepId,
            @RequestBody StepEndpointDto request) {
        return stepEndpointService.upsertEndpoint(stepId, request);
    }

    /** @param stepId étape dont l'endpoint doit être supprimé */
    @DeleteMapping
    public void deleteEndpoint(@PathVariable Long stepId) {
        stepEndpointService.deleteEndpoint(stepId);
    }
}