package st.tt.script_back.services;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.entities.Machine;
import st.tt.script_back.entities.Recipe;
import st.tt.script_back.entities.SoftMachineVersion;
import st.tt.script_back.entities.Step;
import st.tt.script_back.entities.StepEndpoint;
import st.tt.script_back.entities.StepEndpointCondition;
import st.tt.script_back.entities.StepParameter;
import st.tt.script_back.enums.ActivationState;
import st.tt.script_back.enums.XmlSection;
import st.tt.script_back.enums.StepKind;
import st.tt.script_back.repositories.MachineRepository;
import st.tt.script_back.repositories.RecipeRepository;
import st.tt.script_back.repositories.StepEndpointRepository;
import st.tt.script_back.repositories.StepParameterRepository;
import st.tt.script_back.repositories.StepRepository;

/**
 * Generates a Soft Machine-compatible XML file from a derived recipe.
 *
 * Architecture :
 *   - Toutes les données sont chargées en mémoire avant la génération
 *     (pas de lazy loading pendant la construction XML)
 *   - Les StepParameters sont indexés par stepId pour un accès O(1)
 *   - Les endpoints sont indexés par stepId pour un accès O(1)
 *   - La valeur XML d'un PARAM est résolue directement depuis
 *     ParameterOption.label (pas de mapping version nécessaire — validé par tests)
 *   - Les paramètres STEP_ATTRIBUTE deviennent des attributs du tag <STEP>
 *   - Les paramètres DISABLED ne sont pas exportés (sauf si requiredOnStep=true)
 */
@Service
@Transactional(readOnly = true)
public class RecipeXmlExportService {

    private static final String XML_DECLARATION    = "<?xml version=\"1.0\"?>";
    private static final String INDENT             = "";  // XML Soft Machine n'indente pas

    private final RecipeRepository          recipeRepository;
    private final StepRepository            stepRepository;
    private final StepParameterRepository   stepParameterRepository;
    private final StepEndpointRepository    stepEndpointRepository;
    private final MachineRepository         machineRepository;

    public RecipeXmlExportService(
            RecipeRepository        recipeRepository,
            StepRepository          stepRepository,
            StepParameterRepository stepParameterRepository,
            StepEndpointRepository  stepEndpointRepository,
            MachineRepository       machineRepository) {
        this.recipeRepository        = recipeRepository;
        this.stepRepository          = stepRepository;
        this.stepParameterRepository = stepParameterRepository;
        this.stepEndpointRepository  = stepEndpointRepository;
        this.machineRepository       = machineRepository;
    }

    // =========================================================================
    // Point d'entrée public
    // =========================================================================

    /**
     * Génère le XML Soft Machine pour une recette dérivée sur une machine cible.
     *
     * @param recipeId  id de la recette dérivée à exporter
     * @param machineId id de la machine cible (détermine SoftMachineVersion)
     * @return String XML prêt à être écrit dans un fichier .xml
     */
        public String export(Long recipeId, Long machineId) {

        // ── 1. Charger recette ────────────────────────────────────────────────
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Recipe not found: " + recipeId));

        // ── 2. Charger machine et sa version Soft ─────────────────────────────
        Machine machine = resolveMachine(machineId);

        SoftMachineVersion softVersion = machine.getSoftMachineVersion();

        // ── 3. Charger les steps ordonnés ─────────────────────────────────────
        List<Step> allSteps = stepRepository
                .findByRecipeIdOrderByOrderIndexAsc(recipeId);

        List<Long> allStepIds = allSteps.stream()
                .map(Step::getId)
                .toList();

        // ── 4. Charger tous les StepParameters en une requête ─────────────────
        List<StepParameter> allParams = stepParameterRepository
                .findByStepIdsWithDefinitionAndSelectedOption(allStepIds);

        Map<Long, List<StepParameter>> paramsByStepId = allParams.stream()
                .collect(Collectors.groupingBy(sp -> sp.getStep().getId()));

        // ── 5. Charger tous les endpoints en une requête ──────────────────────
        Map<Long, StepEndpoint> endpointByStepId = stepEndpointRepository
                .findByStepIdsWithConditions(allStepIds)
                .stream()
                .collect(Collectors.toMap(e -> e.getStep().getId(), e -> e));

        // ── 6. Construire le XML ──────────────────────────────────────────────
        return buildXml(recipe, softVersion, allSteps, paramsByStepId, endpointByStepId);
    }

        private Machine resolveMachine(Long machineId) {
                if (machineId != null) {
                        return machineRepository.findById(machineId)
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                        "Machine not found: " + machineId));
                }

                return machineRepository.findFirstByOrderByIdAsc()
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "No machine found to resolve SoftMachineVersion for XML export"));
        }

    // =========================================================================
    // Construction XML
    // =========================================================================

    private String buildXml(
            Recipe                         recipe,
            SoftMachineVersion             softVersion,
            List<Step>                     allSteps,
            Map<Long, List<StepParameter>> paramsByStepId,
            Map<Long, StepEndpoint>        endpointByStepId) {

        StringBuilder sb = new StringBuilder(65536);
        sb.append(XML_DECLARATION);

        // <RECIPE Name="..." Format="1.0" xmlns="...">
        sb.append("<RECIPE");
        appendAttr(sb, "Name",   recipe.getName());
        appendAttr(sb, "Format", softVersion != null ? softVersion.getXmlFormat() : "1.0");
        appendAttr(sb, "xmlns",  softVersion != null ? softVersion.getXmlSchema()
                                                     : "x-schema:recipe_schema.xml");
        sb.append(">");

        // PASSPORT
        buildPassport(sb, recipe, softVersion);

        // PREREQUISITES — toujours vide dans les XML observés
        sb.append("<PREREQUISITES>");
        sb.append("<ENDPOINT />");
        sb.append("<PARAMS_PREREQUISITES><REGULAR /><GAS_PANEL /></PARAMS_PREREQUISITES>");
        sb.append("</PREREQUISITES>");

        // PRESTEP
        Optional<Step> prestepOpt = allSteps.stream()
                .filter(s -> s.getStepKind() == StepKind.PRESTEP)
                .findFirst();
        if (prestepOpt.isPresent()) {
            Step prestep = prestepOpt.get();
            buildPrestep(sb, prestep,
                    paramsByStepId.getOrDefault(prestep.getId(), List.of()),
                    endpointByStepId.get(prestep.getId()));
        } else {
            sb.append("<PRESTEP><ENDPOINT /><PARAMS_PRESTEP>")
              .append("<REGULAR /><GAS_PANEL /></PARAMS_PRESTEP>")
              .append("<TOLERANCES /></PRESTEP>");
        }

        // STEPS
        List<Step> regularSteps = allSteps.stream()
                .filter(s -> s.getStepKind() == StepKind.STEP)
                .sorted(Comparator.comparing(Step::getOrderIndex))
                .toList();

        sb.append("<STEPS>");
        for (Step step : regularSteps) {
            buildStep(sb, step,
                    paramsByStepId.getOrDefault(step.getId(), List.of()),
                    endpointByStepId.get(step.getId()));
        }
        sb.append("</STEPS>");

        // POSTSTEP — vide dans les XML observés
        sb.append("<POSTSTEP><ENDPOINT /><PARAMS_POSTSTEP>")
          .append("<REGULAR /><GAS_PANEL /></PARAMS_POSTSTEP>")
          .append("<TOLERANCES /></POSTSTEP>");

        sb.append("</RECIPE>");
        return sb.toString();
    }

    // ── PASSPORT ─────────────────────────────────────────────────────────────

    private void buildPassport(StringBuilder sb, Recipe recipe,
                                SoftMachineVersion softVersion) {
        sb.append("<PASSPORT");
        appendAttr(sb, "Description",
                recipe.getDescription() != null ? recipe.getDescription() : "");
        appendAttr(sb, "Creator",    String.valueOf(
                recipe.getCreatorId() != null ? recipe.getCreatorId() : ""));
        appendAttr(sb, "CreateTime", formatInstant(recipe.getCreateTime()));
        appendAttr(sb, "Revisor",    String.valueOf(
                recipe.getRevisorId() != null ? recipe.getRevisorId() : ""));
        appendAttr(sb, "ReviseTime", formatInstant(recipe.getReviseTime()));

        // ChamberType — depuis la recette, sinon valeur par défaut de la version
        String chamberType = recipe.getChamberType();
        if (chamberType == null && softVersion != null) {
            chamberType = softVersion.getDefaultChamberType();
        }
        appendAttr(sb, "ChamberType", chamberType != null ? chamberType : "CChRadRPCen");

        appendAttr(sb, "Version",            String.valueOf(recipe.getVersion()));
        appendAttr(sb, "AccessDisplayGroups",
                recipe.getAccessDisplayGroups() != null
                        ? recipe.getAccessDisplayGroups() : "All");
        appendAttr(sb, "AccessModifyGroups",
                recipe.getAccessModifyGroups() != null
                        ? recipe.getAccessModifyGroups() : "All");
        appendAttr(sb, "UDAFile",
                recipe.getUdaFile() != null ? recipe.getUdaFile() : "");
        appendAttr(sb, "Type",
                recipe.getType() != null ? recipe.getType() : "Process");
        appendAttr(sb, "Frozen",    recipe.isFrozen() ? "Yes" : "No");
        appendAttr(sb, "Wafer",
                recipe.getWafer() != null ? recipe.getWafer().toXmlValue() : "Present");
        appendAttr(sb, "Resumable",
                recipe.getResumable() != null
                        ? recipe.getResumable().toXmlValue() : "No");
        appendAttr(sb, "MaxTime",
                recipe.getMaxTime() != null
                        ? String.valueOf(recipe.getMaxTime()) : "300");
        appendAttr(sb, "iAPC",
                recipe.getIapc() != null ? recipe.getIapc().toXmlValue() : "Yes");

        String template = recipe.getTemplate();
        if (template == null && softVersion != null) {
            template = softVersion.getDefaultTemplate();
        }
        appendAttr(sb, "Template", template != null ? template : "Default");
        sb.append(" />");
    }

    // ── PRESTEP ──────────────────────────────────────────────────────────────

    private void buildPrestep(StringBuilder sb, Step prestep,
                               List<StepParameter> params,
                               StepEndpoint endpoint) {
        sb.append("<PRESTEP>");
        buildEndpointTag(sb, endpoint);
        sb.append("<PARAMS_PRESTEP>");
        buildParamSections(sb, params);
        sb.append("</PARAMS_PRESTEP>");
        sb.append("<TOLERANCES />");
        sb.append("</PRESTEP>");
    }

    // ── STEP ─────────────────────────────────────────────────────────────────

    private void buildStep(StringBuilder sb, Step step,
                            List<StepParameter> params,
                            StepEndpoint endpoint) {

        // Résoudre les attributs STEP_ATTRIBUTE depuis les StepParameters
        String chLocation = resolveStepAttribute(params, "CHLocation",
                step.getCode()); // fallback sur step.code
        String maxTime    = resolveStepAttribute(params, "MAX_TIME", "0");
        String mode       = resolveStepAttribute(params, "MODE",    "Time");

        sb.append("<STEP");
        appendAttr(sb, "Name",       step.getName());
        appendAttr(sb, "ChLocation", chLocation);
        appendAttr(sb, "MaxTime",    maxTime);
        appendAttr(sb, "Mode",       mode);
        sb.append(">");

        buildEndpointTag(sb, endpoint);

        // ENDP_DEVICE — toujours vide dans les XML observés
        sb.append("<ENDP_DEVICE Type=\"\" />");

        sb.append("<PARAMS_STEP>");
        // Exclure les STEP_ATTRIBUTE des sections REGULAR/GAS_PANEL
        List<StepParameter> nonAttributeParams = params.stream()
                .filter(sp -> sp.getDefinition() != null && !isStepAttribute(sp))
                .toList();
        buildParamSections(sb, nonAttributeParams);
        sb.append("</PARAMS_STEP>");

        sb.append("<TOLERANCES />");
        sb.append("<WH />");
        sb.append("<RUN_CONDITION />");
        sb.append("</STEP>");
    }

    // ── SECTIONS REGULAR / GAS_PANEL ─────────────────────────────────────────

    private void buildParamSections(StringBuilder sb, List<StepParameter> params) {
        List<StepParameter> regular = params.stream()
                .filter(sp -> sp.getDefinition() != null
                        && getXmlSection(sp) == XmlSection.REGULAR)
                .sorted(Comparator.comparingLong(StepParameter::getParentOrderScope)
                        .thenComparing(StepParameter::getOrderIndex))
                .toList();

        List<StepParameter> gasPanel = params.stream()
                .filter(sp -> sp.getDefinition() != null
                        && getXmlSection(sp) == XmlSection.GAS_PANEL)
                .sorted(Comparator.comparingInt(StepParameter::getOrderIndex))
                .toList();

        sb.append("<REGULAR>");
        for (StepParameter sp : regular) {
            buildParam(sb, sp);
        }
        sb.append("</REGULAR>");

        sb.append("<GAS_PANEL>");
        for (StepParameter sp : gasPanel) {
            buildParam(sb, sp);
        }
        sb.append("</GAS_PANEL>");
    }

    // ── PARAM tag ─────────────────────────────────────────────────────────────

    private void buildParam(StringBuilder sb, StepParameter sp) {
        String alias = sp.getDefinition().getAlias() != null
                ? sp.getDefinition().getAlias() : sp.getDefinition().getName();
        String name  = sp.getDefinition().getName() != null
                ? sp.getDefinition().getName() : alias;
        String value = resolveParamValue(sp);
        String unit  = sp.getDefinition().getUnit() != null
                ? sp.getDefinition().getUnit() : "";

        sb.append("<PARAM");
        appendAttr(sb, "Alias", alias);
        appendAttr(sb, "Name",  name);
        appendAttr(sb, "Value", value);
        appendAttr(sb, "Unit",  unit);
        sb.append(" />");
    }

    // ── ENDPOINT tag ──────────────────────────────────────────────────────────

    private void buildEndpointTag(StringBuilder sb, StepEndpoint endpoint) {
        if (endpoint == null || endpoint.getConditions() == null
                || endpoint.getConditions().isEmpty()) {
            sb.append("<ENDPOINT />");
            return;
        }

        sb.append("<ENDPOINT");
        if (endpoint.getClause() != null) {
            appendAttr(sb, "Clause", endpoint.getClause());
        }
        sb.append(">");

        List<StepEndpointCondition> sorted = endpoint.getConditions().stream()
                .sorted(Comparator.comparing(StepEndpointCondition::getOrderIndex))
                .toList();

        for (StepEndpointCondition cond : sorted) {
            buildCondition(sb, cond);
        }

        sb.append("</ENDPOINT>");
    }

    private void buildCondition(StringBuilder sb, StepEndpointCondition cond) {
        String alias = cond.getEndpointParameter() != null
                ? cond.getEndpointParameter().getAlias() : "";
        String name  = cond.getEndpointParameter() != null
                ? cond.getEndpointParameter().getName() : "";
        String value = resolveConditionValue(cond);
        String unit  = cond.getEndpointParameter() != null
                && cond.getEndpointParameter().getUnit() != null
                ? cond.getEndpointParameter().getUnit() : "";
        String op    = cond.getOperator() != null
                ? cond.getOperator().toXmlValue() : "EQ";

        sb.append("<CONDITION");
        appendAttr(sb, "Alias",     alias);
        appendAttr(sb, "Name",      name);
        appendAttr(sb, "Value",     value);
        appendAttr(sb, "Unit",      unit);
        appendAttr(sb, "Condition", op);
        sb.append(" />");
    }

    // =========================================================================
    // Résolution des valeurs
    // =========================================================================

    /**
     * Résout la valeur XML d'un StepParameter.
     * ENUM   → ParameterOption.label (source de vérité unique)
     * NUMBER/STRING/BOOLEAN → valueJson désérialisé
     */
    private String resolveParamValue(StepParameter sp) {
        if (sp.getSelectedOption() != null) {
            return sp.getSelectedOption().getLabel() != null
                    ? sp.getSelectedOption().getLabel() : "";
        }
        if (sp.getValueJson() != null) {
            return deserializeValueJson(sp.getValueJson());
        }
        return "";
    }

    /**
     * Résout la valeur d'une condition d'endpoint.
     * Même logique que resolveParamValue.
     */
    private String resolveConditionValue(StepEndpointCondition cond) {
        if (cond.getSelectedOption() != null) {
            return cond.getSelectedOption().getLabel() != null
                    ? cond.getSelectedOption().getLabel() : "";
        }
        if (cond.getValueJson() != null) {
            return deserializeValueJson(cond.getValueJson());
        }
        return "";
    }

    /**
     * Désérialise value_json vers une chaîne XML.
     * Retire les guillemets JSON autour des strings.
     * Convertit les booléens JSON en "Yes"/"No".
     */
    private String deserializeValueJson(String valueJson) {
        if (valueJson == null || valueJson.isBlank()) {
            return "";
        }
        String trimmed = valueJson.trim();

        // String JSON : "valeur" → valeur
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            return trimmed.substring(1, trimmed.length() - 1);
        }

        // Booléen JSON → Yes/No (convention Soft Machine)
        if ("true".equals(trimmed))  return "Yes";
        if ("false".equals(trimmed)) return "No";

        // Nombre → directement
        return trimmed;
    }

    /**
     * Résout la valeur d'un StepParameter de type STEP_ATTRIBUTE.
     * Recherche le paramètre par alias dans la liste du step.
     */
    private String resolveStepAttribute(List<StepParameter> params,
                                         String alias,
                                         String fallback) {
        return params.stream()
                .filter(sp -> sp.getDefinition() != null
                        && alias.equals(sp.getDefinition().getAlias())
                        && sp.getActivationState() != ActivationState.DISABLED)
                .findFirst()
                .map(this::resolveParamValue)
                .filter(v -> !v.isBlank())
                .orElse(fallback);
    }

    // =========================================================================
    // Prédicats et helpers
    // =========================================================================

    /**
     * Détermine si un StepParameter doit être inclus dans le XML.
     * - Paramètre obligatoire (requiredOnStep=true) → toujours exporté
     * - Paramètre optionnel                         → exporté seulement si ENABLED
     */
    private boolean shouldExport(StepParameter sp) {
        if (sp.getDefinition() == null) {
            return false;
        }
        if (sp.getDefinition().isRequiredOnStep()) {
            return true;
        }
        return sp.getActivationState() == ActivationState.ENABLED;
    }

    /**
     * Un paramètre STEP_ATTRIBUTE devient un attribut du tag <STEP>,
     * pas un <PARAM> dans <REGULAR> ou <GAS_PANEL>.
     */
    private boolean isStepAttribute(StepParameter sp) {
        return sp.getDefinition() != null
                && sp.getDefinition().getXmlSection() == XmlSection.STEP_ATTRIBUTE;
    }

    /**
     * Retourne la section XML du paramètre.
     * Fallback sur REGULAR si non renseigné.
     */
    private XmlSection getXmlSection(StepParameter sp) {
        if (sp.getDefinition() == null
                || sp.getDefinition().getXmlSection() == null) {
            return XmlSection.REGULAR;
        }
        return sp.getDefinition().getXmlSection();
    }

    // =========================================================================
    // Utilitaires XML
    // =========================================================================

    /**
     * Ajoute un attribut XML avec échappement des caractères spéciaux.
     * Format : Name="Value"
     */
    private void appendAttr(StringBuilder sb, String name, String value) {
        sb.append(' ').append(name).append("=\"")
          .append(escapeXml(value != null ? value : ""))
          .append('"');
    }

    /**
     * Échappe les caractères XML réservés dans une valeur d'attribut.
     */
    private String escapeXml(String value) {
        if (value == null) return "";
        return value
                .replace("&",  "&amp;")
                .replace("\"", "&quot;")
                .replace("<",  "&lt;")
                .replace(">",  "&gt;")
                .replace("'",  "&apos;");
    }

    /**
     * Formate un Instant pour le PASSPORT Soft Machine.
     * Format observé dans les XML : "dd/MM/yyyy HH:mm:ss.SSS"
     */
    private String formatInstant(java.time.Instant instant) {
        if (instant == null) return "";
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter
                .ofPattern("dd/MM/yyyy HH:mm:ss.SSS")
                .withZone(java.time.ZoneId.systemDefault());
        return fmt.format(instant);
    }
}