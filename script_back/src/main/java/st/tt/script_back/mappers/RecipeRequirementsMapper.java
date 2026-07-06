package st.tt.script_back.mappers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import st.tt.script_back.dto.RecipeRequiredCapabilityDto;
import st.tt.script_back.dto.RecipeRequiredConfigurationDto;
import st.tt.script_back.dto.RecipeRequirementsDto;
import st.tt.script_back.entities.ChamberCapability;
import st.tt.script_back.entities.ConfigurationDefinition;
import st.tt.script_back.entities.Recipe;

@Component
public class RecipeRequirementsMapper {

    public RecipeRequirementsDto toDto(Recipe recipe) {
        if (recipe == null) {
            return null;
        }

        List<RecipeRequiredCapabilityDto> capabilities = recipe.getRequiredCapabilities() == null
                ? Collections.emptyList()
                : recipe.getRequiredCapabilities().stream()
                        .sorted(Comparator.comparing((ChamberCapability c) -> c.getCategory().name())
                                .thenComparing(ChamberCapability::getLabel))
                        .map(capability -> new RecipeRequiredCapabilityDto(
                                capability.getId(),
                                capability.getCode(),
                                capability.getLabel(),
                                capability.getCategory()))
                        .toList();

        List<RecipeRequiredConfigurationDto> configurations = recipe.getRequiredConfigurationDefinitions() == null
                ? Collections.emptyList()
                : recipe.getRequiredConfigurationDefinitions().stream()
                        .sorted(Comparator.comparing(
                                ConfigurationDefinition::getDisplayOrder,
                                Comparator.nullsFirst(Comparator.naturalOrder()))
                                .thenComparing(ConfigurationDefinition::getCode))
                        .map(definition -> new RecipeRequiredConfigurationDto(
                                definition.getId(),
                                definition.getCode(),
                                definition.getName(),
                                definition.getValueType(),
                                definition.getUnit(),
                                definition.getQuestionForForm(),
                                definition.getQuestionGroup(),
                                definition.getDisplayOrder()))
                        .toList();

        return new RecipeRequirementsDto(
                recipe.getId(),
                null,
                recipe.getName(),
                capabilities,
                configurations);
    }
}
