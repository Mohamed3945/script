package st.tt.script_back.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.CompatibleChamberDto;
import st.tt.script_back.dto.CompatibleMachineDto;
import st.tt.script_back.dto.RecipeCompatibilityResultDto;
import st.tt.script_back.dto.RecipeCompatibleMachineSearchRequestDto;
import st.tt.script_back.entities.Chamber;
import st.tt.script_back.entities.ChamberCapability;
import st.tt.script_back.entities.Machine;
import st.tt.script_back.entities.Recipe;
import st.tt.script_back.repositories.ChamberRepository;
import st.tt.script_back.repositories.RecipeRepository;

@Service
public class RecipeCompatibilityService {

    private final RecipeRepository recipeRepository;
    private final ChamberRepository chamberRepository;

    public RecipeCompatibilityService(
            RecipeRepository recipeRepository,
            ChamberRepository chamberRepository) {
        this.recipeRepository = recipeRepository;
        this.chamberRepository = chamberRepository;
    }

    @Transactional(readOnly = true)
    public RecipeCompatibilityResultDto findCompatibleMachines(RecipeCompatibleMachineSearchRequestDto request) {
        if (request == null || request.getRecipeId() == null) {
            throw new IllegalArgumentException("recipeId is required");
        }

        Recipe recipe = recipeRepository.findByIdWithRequirements(request.getRecipeId())
                .orElseThrow(() -> new EntityNotFoundException("Recipe with id " + request.getRecipeId() + " not found"));

        Set<String> requiredCapabilityCodes = recipe.getRequiredCapabilities() == null
                ? Set.of()
                : recipe.getRequiredCapabilities().stream()
                        .map(ChamberCapability::getCode)
                        .collect(Collectors.toSet());

        List<Chamber> allChambers = chamberRepository.findAllWithMachineCapabilitiesAndConfigurations();

        List<Chamber> compatibleChambers = allChambers.stream()
                .filter(chamber -> matchesRequiredCapabilities(chamber, requiredCapabilityCodes))
                .sorted(Comparator.comparing((Chamber c) -> c.getMachine().getCode())
                        .thenComparing(Chamber::getCode))
                .toList();

        Map<Long, List<Chamber>> chambersByMachineId = compatibleChambers.stream()
                .filter(chamber -> chamber.getMachine() != null && chamber.getMachine().getId() != null)
                .collect(Collectors.groupingBy(chamber -> chamber.getMachine().getId()));

        List<CompatibleMachineDto> machines = new ArrayList<>();
        for (Map.Entry<Long, List<Chamber>> entry : chambersByMachineId.entrySet()) {
            List<Chamber> machineChambers = entry.getValue();
            if (machineChambers.isEmpty()) {
                continue;
            }

            Machine machine = machineChambers.get(0).getMachine();

            List<CompatibleChamberDto> compatibleChamberDtos = machineChambers.stream()
                    .map(chamber -> new CompatibleChamberDto(
                            chamber.getId(),
                            chamber.getCode(),
                            chamber.getName(),
                            chamber.getCapabilities().stream()
                                    .map(ChamberCapability::getCode)
                                    .sorted()
                                    .toList()
                    ))
                    .toList();

            machines.add(new CompatibleMachineDto(
                    machine.getId(),
                    machine.getCode(),
                    machine.getName(),
                    machine.getPlatformType(),
                    compatibleChamberDtos
            ));
        }

        machines = machines.stream()
                .sorted(Comparator.comparing(CompatibleMachineDto::getMachineCode))
                .toList();

        return new RecipeCompatibilityResultDto(
                recipe.getId(),
                machines.size(),
                compatibleChambers.size(),
                machines
        );
    }

    private boolean matchesRequiredCapabilities(Chamber chamber, Set<String> requiredCapabilityCodes) {
        if (requiredCapabilityCodes == null || requiredCapabilityCodes.isEmpty()) {
            return true;
        }

        Set<String> chamberCapabilityCodes = chamber.getCapabilities() == null
                ? Set.of()
                : chamber.getCapabilities().stream()
                        .map(ChamberCapability::getCode)
                        .collect(Collectors.toSet());

        return chamberCapabilityCodes.containsAll(requiredCapabilityCodes);
    }

}
