package st.tt.script_back.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.CompatibleChamberDto;
import st.tt.script_back.dto.CompatibleMachineDto;
import st.tt.script_back.dto.RecipeCompatibilityResultDto;
import st.tt.script_back.dto.RecipeCompatibleMachineSearchRequestDto;
import st.tt.script_back.dto.RecipeConfigurationConstraintDto;
import st.tt.script_back.entities.Chamber;
import st.tt.script_back.entities.ChamberCapability;
import st.tt.script_back.entities.ChamberConfiguration;
import st.tt.script_back.entities.Recipe;
import st.tt.script_back.entities.StepParameter;
import st.tt.script_back.enums.ActivationState;
import st.tt.script_back.enums.ParameterValueType;
import st.tt.script_back.enums.StepKind;
import st.tt.script_back.repositories.ChamberConfigurationRepository;
import st.tt.script_back.repositories.ChamberRepository;
import st.tt.script_back.repositories.RecipeRepository;
import st.tt.script_back.repositories.StepParameterRepository;

@Service
public class RecipeCompatibilityService {

    private final RecipeRepository recipeRepository;
    private final ChamberRepository chamberRepository;
    private final ChamberConfigurationRepository chamberConfigurationRepository;
    private final StepParameterRepository stepParameterRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RecipeCompatibilityService(
            RecipeRepository recipeRepository,
            ChamberRepository chamberRepository,
            ChamberConfigurationRepository chamberConfigurationRepository,
            StepParameterRepository stepParameterRepository) {
        this.recipeRepository = recipeRepository;
        this.chamberRepository = chamberRepository;
        this.chamberConfigurationRepository = chamberConfigurationRepository;
        this.stepParameterRepository = stepParameterRepository;
    }

    @Transactional(readOnly = true)
    public RecipeCompatibilityResultDto findCompatibleMachines(RecipeCompatibleMachineSearchRequestDto request) {
        if (request == null || request.getRecipeId() == null) {
            throw new IllegalArgumentException("recipeId is required");
        }

        Recipe recipe = recipeRepository.findByIdWithRequirements(request.getRecipeId())
                    .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + request.getRecipeId() + " not found"));
        Recipe capabilitySource = recipe;
        if (request.getCapabilitySourceRecipeId() != null
                && !request.getCapabilitySourceRecipeId().equals(request.getRecipeId())) {
            capabilitySource = recipeRepository
                    .findByIdWithRequirements(request.getCapabilitySourceRecipeId())
                    .orElse(recipe); // fallback silencieux sur la recette principale
        }

        final Recipe capabilityRecipe = capabilitySource; // effectivement final pour le lambda

        List<Chamber> allChambers = chamberRepository.findAllWithMachineAndCapabilities();
        if (allChambers.isEmpty()) {
            return new RecipeCompatibilityResultDto(recipe.getId(), 0, 0, List.of());
        }

        // Niveau 1: capabilities
        List<Chamber> capabilityCompatibleChambers = allChambers.stream()
                .filter(chamber -> matchesRequiredCapabilities(chamber, capabilityRecipe))
                .toList();

        if (capabilityCompatibleChambers.isEmpty()) {
            return new RecipeCompatibilityResultDto(recipe.getId(), 0, 0, List.of());
        }

        // Niveau 2: contraintes quantitatives (payload si fourni, sinon calcul live depuis recette)
        List<ConfigurationConstraintEntry> constraints = resolveConstraints(request);

        List<Chamber> finalCompatibleChambers;
        if (constraints.isEmpty()) {
            finalCompatibleChambers = capabilityCompatibleChambers;
        } else {
            List<Long> chamberIds = capabilityCompatibleChambers.stream()
                    .map(Chamber::getId)
                    .toList();

            List<ChamberConfiguration> chamberConfigurations =
                    chamberConfigurationRepository.findByChamberIdsWithDefinition(chamberIds);

            Map<Long, List<ChamberConfiguration>> configurationsByChamberId =
                    chamberConfigurations.stream()
                            .collect(Collectors.groupingBy(cc -> cc.getChamber().getId()));

            finalCompatibleChambers = capabilityCompatibleChambers.stream()
                    .filter(chamber -> matchesLiveConfigurationConstraints(
                            configurationsByChamberId.getOrDefault(chamber.getId(), List.of()),
                            constraints))
                    .toList();
        }

        Map<Long, CompatibleMachineDto> machinesById = new HashMap<>();

        for (Chamber chamber : finalCompatibleChambers) {
            if (chamber.getMachine() == null || chamber.getMachine().getId() == null) {
                continue;
            }

            Long machineId = chamber.getMachine().getId();

            CompatibleMachineDto machineDto = machinesById.computeIfAbsent(machineId, ignored ->
                    new CompatibleMachineDto(
                            machineId,
                            chamber.getMachine().getCode(),
                            chamber.getMachine().getName(),
                            chamber.getMachine().getPlatformType(),
                            new ArrayList<>()));

            List<String> matchedCapabilityCodes = chamber.getCapabilities() == null
                    ? List.of()
                    : chamber.getCapabilities().stream()
                            .map(ChamberCapability::getCode)
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .toList();

            machineDto.getCompatibleChambers().add(
                    new CompatibleChamberDto(
                            chamber.getId(),
                            chamber.getCode(),
                            chamber.getName(),
                            matchedCapabilityCodes));
        }

        List<CompatibleMachineDto> machines = machinesById.values().stream()
                .sorted(Comparator.comparing(CompatibleMachineDto::getMachineCode, String.CASE_INSENSITIVE_ORDER))
                .toList();

        int compatibleChamberCount = finalCompatibleChambers.size();
        int compatibleMachineCount = machines.size();

        return new RecipeCompatibilityResultDto(
                recipe.getId(),
                compatibleMachineCount,
                compatibleChamberCount,
                machines);
    }

    // Record interne — clé de matching entre StepParameter et ChamberConfiguration
    private record ConfigurationConstraintEntry(
            Long configDefinitionId,  // O2_FLOW, TCRP, H2_FLOW
            String controllerCode,    // "O2_HIGH", "O2_LOW" — identique à ChamberConfiguration.code
            BigDecimal requestedValue
    ) {}

   
    private List<ConfigurationConstraintEntry> resolveConstraints(
            RecipeCompatibleMachineSearchRequestDto request) {

        if (request.getConfigurationConstraints() == null) {
            // null → calcul automatique depuis les StepParameters de la recette
            return buildConfigurationConstraintsFromRecipe(request.getRecipeId());
        }

        if (request.getConfigurationConstraints().isEmpty()) {
            // [] → pas de filtrage niveau 2, capabilities seules
            return List.of();
        }

        // Contraintes explicites fournies
        List<ConfigurationConstraintEntry> entries = new ArrayList<>();
        for (RecipeConfigurationConstraintDto c : request.getConfigurationConstraints()) {
            if (c == null
                    || c.getConfigurationDefinitionId() == null
                    || c.getRequestedValue() == null) {
                continue;
            }
            if (c.getRequestedValue().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            entries.add(new ConfigurationConstraintEntry(
                    c.getConfigurationDefinitionId(),
                    c.getConfigurationDefinitionCode(), // code du controller fourni explicitement
                    c.getRequestedValue()
            ));
        }
        return entries;
    }


    private List<ConfigurationConstraintEntry> buildConfigurationConstraintsFromRecipe(
            Long recipeId) {

        List<StepParameter> parameters = stepParameterRepository
                .findActiveCompatibilityParametersByRecipeId(
                        recipeId,
                        Set.of(StepKind.STEP, StepKind.PRESTEP),
                        ActivationState.DISABLED);

        List<ConfigurationConstraintEntry> entries = new ArrayList<>();

        for (StepParameter parameter : parameters) {
            if (parameter.getDefinition() == null
                    || parameter.getDefinition().getConfigurationDefinition() == null
                    || parameter.getDefinition().getConfigurationDefinition().getId() == null) {
                continue;
            }
            if (parameter.getDefinition().getValueType() != ParameterValueType.NUMBER) {
                continue;
            }

            BigDecimal requested = parseNumberFromValueJson(parameter.getValueJson());

            // 0 ou null → non renseigné → ignoré
            if (requested == null || requested.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            entries.add(new ConfigurationConstraintEntry(
                    parameter.getDefinition().getConfigurationDefinition().getId(),
                    parameter.getDefinition().getCode(), // "O2_HIGH", "O2_LOW", etc.
                    requested
            ));
        }

        return entries;
    }

    private BigDecimal parseNumberFromValueJson(String valueJson) {
        if (valueJson == null || valueJson.isBlank()) {
            return null;
        }

        try {
            JsonNode node = objectMapper.readTree(valueJson.trim());

            if (node.isNumber()) {
                return node.decimalValue();
            }

            if (node.isTextual()) {
                String normalized = node.asText().trim().replace(',', '.');
                if (normalized.isEmpty()) {
                    return null;
                }
                return new BigDecimal(normalized);
            }

            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean matchesRequiredCapabilities(Chamber chamber, Recipe recipe) {
        if (recipe.getRequiredCapabilities() == null || recipe.getRequiredCapabilities().isEmpty()) {
            return true;
        }

        Set<Long> chamberCapabilityIds = chamber.getCapabilities() == null
                ? Set.of()
                : chamber.getCapabilities().stream()
                        .map(ChamberCapability::getId)
                        .collect(Collectors.toCollection(HashSet::new));

        return recipe.getRequiredCapabilities().stream()
                .allMatch(required -> required != null
                        && required.getId() != null
                        && chamberCapabilityIds.contains(required.getId()));
    }

    private boolean matchesLiveConfigurationConstraints(
            List<ChamberConfiguration> chamberConfigurations,
            List<ConfigurationConstraintEntry> constraints) {

        for (ConfigurationConstraintEntry entry : constraints) {

            // Toutes les configs de cette chamber pour cette configDef
            List<ChamberConfiguration> candidateConfigs = chamberConfigurations.stream()
                    .filter(cc -> cc.getConfigurationDefinition() != null
                            && entry.configDefinitionId().equals(
                                    cc.getConfigurationDefinition().getId()))
                    .toList();

            if (candidateConfigs.isEmpty()) {
                // La chamber n'a aucune config pour cette configDef → rejetée
                return false;
            }

            // Chercher la config dont le code correspond exactement au controller
            // ex: ChamberConfiguration.code = "O2_HIGH" ← identique à ParameterDefinition.code
            List<ChamberConfiguration> exactMatch = candidateConfigs.stream()
                    .filter(cc -> entry.controllerCode() != null
                            && entry.controllerCode().equalsIgnoreCase(cc.getCode()))
                    .toList();

            // Si match exact → on vérifie seulement celle-là
            // Sinon fallback → on essaie toutes les instances (config sans équivalent paramètre)
            List<ChamberConfiguration> toCheck = exactMatch.isEmpty()
                    ? candidateConfigs
                    : exactMatch;

            boolean covered = toCheck.stream()
                    .anyMatch(cc -> rangeMatches(cc, entry.requestedValue()));

            if (!covered) {
                return false;
            }
        }

        return true;
    }

    private boolean rangeMatches(ChamberConfiguration configuration, BigDecimal requestedValue) {
        if (requestedValue == null) {
            return false;
        }

        if (configuration.getMinValue() != null && requestedValue.compareTo(configuration.getMinValue()) < 0) {
            return false;
        }

        if (configuration.getMaxValue() != null && requestedValue.compareTo(configuration.getMaxValue()) > 0) {
            return false;
        }

        return true; // bornes inclusives
    }
}