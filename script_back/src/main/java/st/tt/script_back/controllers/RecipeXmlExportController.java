package st.tt.script_back.controllers;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    public RecipeXmlExportController(RecipeXmlExportService recipeXmlExportService) {
        this.recipeXmlExportService = recipeXmlExportService;
    }

    @GetMapping("/{recipeId}/export/xml")
    public ResponseEntity<byte[]> exportXml(
            @PathVariable Long recipeId,
            @RequestParam(required = false) Long machineId) {

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

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }
}