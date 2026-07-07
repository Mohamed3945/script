package st.tt.script_back.mappers;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import st.tt.script_back.dto.ParameterDefinitionDetailDto;
import st.tt.script_back.dto.ParameterDefinitionDto;
import st.tt.script_back.dto.ParameterOptionDto;
import st.tt.script_back.entities.ConfigurationDefinition;
import st.tt.script_back.entities.ParameterDefinition;

/**
 * ParameterDefinitionMapper class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Component
public class ParameterDefinitionMapper {

    /**
     * Executes toDto.
     *
     * @param definition input argument consumed by toDto.
     * @return computed ParameterDefinitionDto result returned by toDto.
     */
    public ParameterDefinitionDto toDto(ParameterDefinition definition) {
        if (definition == null) {
            return null;
        }
        return new ParameterDefinitionDto(
                definition.getId(),
            definition.getCode(),
                definition.getName(),
                definition.getAlias(),
                definition.getUnit(),
                definition.getDescription(),
                definition.getValueType(),
                definition.isRequiredOnStep(),
                definition.getStepType(),
            definition.getDefaultValueJson(),
            definition.getParameterGroup(),
            definition.getParameterGroupOrder(),
            definition.getConfigurationDefinition() != null ? definition.getConfigurationDefinition().getId() : null,
            definition.getConfigurationDefinition() != null ? definition.getConfigurationDefinition().getCode() : null,
            definition.getConfigurationDefinition() != null ? definition.getConfigurationDefinition().getName() : null
        );
    }

    /**
     * Executes toDetailDto.
     *
     * @param definition input argument consumed by toDetailDto.
     * @param options input argument consumed by toDetailDto.
     * @return computed ParameterDefinitionDetailDto result returned by toDetailDto.
     */
    public ParameterDefinitionDetailDto toDetailDto(ParameterDefinition definition, List<ParameterOptionDto> options) {
        if (definition == null) {
            return null;
        }
        return new ParameterDefinitionDetailDto(
                definition.getId(),
            definition.getCode(),
                definition.getName(),
                definition.getAlias(),
                definition.getUnit(),
                definition.getDescription(),
                definition.getValueType(),
                definition.isRequiredOnStep(),
                definition.getStepType(),
                definition.getDefaultValueJson(),
            definition.getParameterGroup(),
            definition.getParameterGroupOrder(),
            definition.getConfigurationDefinition() != null ? definition.getConfigurationDefinition().getId() : null,
            definition.getConfigurationDefinition() != null ? definition.getConfigurationDefinition().getCode() : null,
            definition.getConfigurationDefinition() != null ? definition.getConfigurationDefinition().getName() : null,
                options == null ? Collections.emptyList() : options
        );
    }

    /**
     * Executes toDtoList.
     *
     * @param definitions input argument consumed by toDtoList.
     * @return computed List<ParameterDefinitionDto> result returned by toDtoList.
     */
    public List<ParameterDefinitionDto> toDtoList(List<ParameterDefinition> definitions) {
        if (definitions == null) {
            return Collections.emptyList();
        }
        return definitions.stream().map(this::toDto).toList();
    }

    /**
     * Executes toEntity.
     *
     * @param dto input argument consumed by toEntity.
     * @return computed ParameterDefinition result returned by toEntity.
     */
    public ParameterDefinition toEntity(ParameterDefinitionDto dto) {
        if (dto == null) {
            return null;
        }
        ParameterDefinition entity = new ParameterDefinition();
        entity.setId(dto.getId());
        updateEntityFromDto(dto, entity);
        return entity;
    }

    /**
     * Executes updateEntityFromDto.
     *
     * @param dto input argument consumed by updateEntityFromDto.
     * @param entity input argument consumed by updateEntityFromDto.
     */
    public void updateEntityFromDto(ParameterDefinitionDto dto, ParameterDefinition entity) {
        // code is server-managed (derived from name + stepType) and must not be overwritten from payload
        entity.setName(dto.getName());
        entity.setAlias(dto.getAlias());
        entity.setUnit(dto.getUnit());
        entity.setDescription(dto.getDescription());
        entity.setValueType(dto.getValueType());
        entity.setRequiredOnStep(dto.isRequiredOnStep());
        entity.setStepType(dto.getStepType());
        entity.setDefaultValueJson(dto.getDefaultValueJson());
        entity.setParameterGroup(dto.getParameterGroup());
        entity.setParameterGroupOrder(dto.getParameterGroupOrder() == null ? 0 : dto.getParameterGroupOrder());

        if (dto.getConfigurationDefinitionId() != null) {
            ConfigurationDefinition configurationDefinition = new ConfigurationDefinition();
            configurationDefinition.setId(dto.getConfigurationDefinitionId());
            entity.setConfigurationDefinition(configurationDefinition);
        } else {
            entity.setConfigurationDefinition(null);
        }
    }
}
