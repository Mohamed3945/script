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

import st.tt.script_back.dto.ComputationFormulaDto;
import st.tt.script_back.services.ComputationFormulaService;

/** Gère les formules utilisées pour calculer les paramètres d'une recette. */
@RestController
@RequestMapping("/api")
public class ComputationFormulaController {

    private final ComputationFormulaService computationFormulaService;

    /** @param computationFormulaService service métier des formules */
    public ComputationFormulaController(ComputationFormulaService computationFormulaService) {
        this.computationFormulaService = computationFormulaService;
    }

    /** @param recipeId recette cible @return formules de la recette */
    @GetMapping("/recipes/{recipeId}/computation-formulas")
    public List<ComputationFormulaDto> getRecipeComputationFormulas(@PathVariable Long recipeId) {
        return computationFormulaService.getFormulasByRecipe(recipeId);
    }

    /** @param id identifiant de la formule @return formule demandée */
    @GetMapping("/computation-formulas/{id}")
    public ComputationFormulaDto getComputationFormula(@PathVariable Long id) {
        return computationFormulaService.getFormula(id);
    }

    /** @param recipeId recette cible @param request formule à créer @return formule créée */
    @PostMapping("/recipes/{recipeId}/computation-formulas")
    public ComputationFormulaDto createComputationFormula(
            @PathVariable Long recipeId,
            @RequestBody ComputationFormulaDto request) {
        return computationFormulaService.createFormula(recipeId, request);
    }

    /** @param id identifiant de la formule @param request nouvelles données @return formule mise à jour */
    @PutMapping("/computation-formulas/{id}")
    public ComputationFormulaDto updateComputationFormula(
            @PathVariable Long id,
            @RequestBody ComputationFormulaDto request) {
        return computationFormulaService.updateFormula(id, request);
    }

    /** @param id identifiant de la formule à supprimer */
    @DeleteMapping("/computation-formulas/{id}")
    public void deleteComputationFormula(@PathVariable Long id) {
        computationFormulaService.deleteFormula(id);
    }
}