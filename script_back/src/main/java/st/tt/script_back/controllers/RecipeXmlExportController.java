package st.tt.script_back.controllers;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import st.tt.script_back.dto.RecipeCustomizationSummaryDto;
import st.tt.script_back.services.RecipeCustomizationSummaryService;
import st.tt.script_back.services.RecipeXmlExportService;

/**
 * Endpoint d'export XML Soft Machine.
 *
 * GET /api/recipes/{recipeId}/export/xml?machineId={machineId}
 *   → Télécharge un fichier .xml prêt à importer dans Soft Machine
 *
 * Le nom du fichier suit la convention Soft Machine observée dans les exemples :
 *   {MACHINE_CODE}_{RECIPE_NAME}.xml
 *   ex : TCENT30_Golden_ISSG_NC.xml
 */
@RestController
@RequestMapping("/api/recipes")
public class RecipeXmlExportController {

    private final RecipeXmlExportService recipeXmlExportService;
    private final RecipeCustomizationSummaryService recipeCustomizationSummaryService;
    private final int maxUntouchedModifiableSpForExport;

    public RecipeXmlExportController(
            RecipeXmlExportService recipeXmlExportService,
            RecipeCustomizationSummaryService recipeCustomizationSummaryService,
            @Value("${script.export.max-untouched-modifiable-sp:-1}") int maxUntouchedModifiableSpForExport) {
        this.recipeXmlExportService = recipeXmlExportService;
        this.recipeCustomizationSummaryService = recipeCustomizationSummaryService;
        this.maxUntouchedModifiableSpForExport = maxUntouchedModifiableSpForExport;
    }

    @GetMapping("/{recipeId}/export/xml")
    public ResponseEntity<byte[]> exportXml(
            @PathVariable Long recipeId,
            @RequestParam(required = false) Long machineId) {

        RecipeCustomizationSummaryDto customizationSummary =
            recipeCustomizationSummaryService.computeForRecipe(recipeId, 150);

        // V2 strict mode (opt-in):
        // - By default (-1), export is NOT blocked.
        // - Set property to 0 to require zero untouched modifiable SP.
        // - Set property to N (>0) to allow up to N untouched.
        int untouchedCount = customizationSummary.getStats().getUntouchedCount();
        if (maxUntouchedModifiableSpForExport >= 0
            && "DERIVED".equalsIgnoreCase(customizationSummary.getWorkspaceType())
            && untouchedCount > maxUntouchedModifiableSpForExport) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "XML export blocked: " + untouchedCount
                    + " untouched modifiable SP detected (allowed: "
                    + maxUntouchedModifiableSpForExport + ").");
        }

        String xmlContent = recipeXmlExportService.export(recipeId, machineId);
        byte[] bytes      = xmlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        String filename = machineId != null
            ? "recipe_" + recipeId + "_machine_" + machineId + ".xml"
            : "recipe_" + recipeId + ".xml";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(filename).build());
        headers.setContentLength(bytes.length);
        headers.set("X-Customization-Rate", String.valueOf(customizationSummary.getStats().getCustomizationRate()));
        headers.set("X-Untouched-Modifiable-SP", String.valueOf(untouchedCount));

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }
}