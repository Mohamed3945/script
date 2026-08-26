package st.tt.script_back.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import st.tt.script_back.dto.RecipeCustomizationStatsDto;
import st.tt.script_back.dto.RecipeCustomizationSummaryDto;
import st.tt.script_back.dto.RecipeUntouchedModifiableItemDto;
import st.tt.script_back.services.RecipeCustomizationSummaryService;
import st.tt.script_back.services.RecipeXmlExportService;

@ExtendWith(MockitoExtension.class)
class RecipeXmlExportControllerTest {

    @Mock
    private RecipeXmlExportService recipeXmlExportService;

    @Mock
    private RecipeCustomizationSummaryService recipeCustomizationSummaryService;

    @Test
    void exportXml_shouldReturnXmlPayloadAndHeaders_whenStrictModeIsDisabled() {
        RecipeXmlExportController controller = new RecipeXmlExportController(
                recipeXmlExportService,
                recipeCustomizationSummaryService,
                -1);

        when(recipeCustomizationSummaryService.computeForRecipe(46L, 150)).thenReturn(summary("DERIVED", 83, 0.21d));
        when(recipeXmlExportService.export(46L, 3L)).thenReturn("<recipe id=\"46\"/>");

        ResponseEntity<byte[]> response = controller.exportXml(46L, 3L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("application/xml", response.getHeaders().getContentType().toString());
        assertEquals("83", response.getHeaders().getFirst("X-Untouched-Modifiable-SP"));
        assertEquals("0.21", response.getHeaders().getFirst("X-Customization-Rate"));
        assertEquals("<recipe id=\"46\"/>", new String(response.getBody(), StandardCharsets.UTF_8));

        HttpHeaders headers = response.getHeaders();
        assertEquals("attachment; filename=\"recipe_46_machine_3.xml\"", headers.getFirst(HttpHeaders.CONTENT_DISPOSITION));
        verify(recipeXmlExportService).export(46L, 3L);
    }

    @Test
    void exportXml_shouldBlockDerivedExport_whenStrictModeEnabledAndUntouchedExceedsLimit() {
        RecipeXmlExportController controller = new RecipeXmlExportController(
                recipeXmlExportService,
                recipeCustomizationSummaryService,
                0);

        when(recipeCustomizationSummaryService.computeForRecipe(46L, 150)).thenReturn(summary("DERIVED", 5, 0.92d));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> controller.exportXml(46L, 3L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("XML export blocked: 5 untouched modifiable SP detected (allowed: 0).", ex.getReason());
    }

    @Test
    void exportXml_shouldAllowGoldenEvenWhenStrictEnabled() {
        RecipeXmlExportController controller = new RecipeXmlExportController(
                recipeXmlExportService,
                recipeCustomizationSummaryService,
                0);

        when(recipeCustomizationSummaryService.computeForRecipe(1L, 150)).thenReturn(summary("GOLDEN", 12, 0.00d));
        when(recipeXmlExportService.export(1L, null)).thenReturn("<golden/>");

        ResponseEntity<byte[]> response = controller.exportXml(1L, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attachment; filename=\"recipe_1.xml\"", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals("<golden/>", new String(response.getBody(), StandardCharsets.UTF_8));
    }

    @Test
    void exportXml_shouldAllowDerivedWhenUntouchedWithinConfiguredLimit() {
        RecipeXmlExportController controller = new RecipeXmlExportController(
                recipeXmlExportService,
                recipeCustomizationSummaryService,
                10);

        when(recipeCustomizationSummaryService.computeForRecipe(46L, 150)).thenReturn(summary("DERIVED", 8, 0.67d));
        when(recipeXmlExportService.export(46L, 3L)).thenReturn("<ok/>");

        ResponseEntity<byte[]> response = controller.exportXml(46L, 3L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("8", response.getHeaders().getFirst("X-Untouched-Modifiable-SP"));
    }

    private RecipeCustomizationSummaryDto summary(String workspaceType, int untouchedCount, double customizationRate) {
        RecipeCustomizationStatsDto stats = new RecipeCustomizationStatsDto(
                100,
                (int) Math.round(customizationRate * 100),
                untouchedCount,
                20,
                customizationRate);

        return new RecipeCustomizationSummaryDto(
                46L,
                12L,
                workspaceType,
                stats,
                List.<RecipeUntouchedModifiableItemDto>of());
    }
}
