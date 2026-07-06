package st.tt.script_back.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.ConfigurationDefinitionDetailDto;
import st.tt.script_back.dto.ConfigurationDefinitionDto;
import st.tt.script_back.entities.ConfigurationDefinition;
import st.tt.script_back.mappers.ConfigurationDefinitionMapper;
import st.tt.script_back.repositories.ConfigurationDefinitionRepository;

@Service
public class ConfigurationDefinitionService {

    private final ConfigurationDefinitionRepository configurationDefinitionRepository;
    private final ConfigurationDefinitionMapper configurationDefinitionMapper;

    public ConfigurationDefinitionService(
            ConfigurationDefinitionRepository configurationDefinitionRepository,
            ConfigurationDefinitionMapper configurationDefinitionMapper) {
        this.configurationDefinitionRepository = configurationDefinitionRepository;
        this.configurationDefinitionMapper = configurationDefinitionMapper;
    }

    @Transactional(readOnly = true)
    public List<ConfigurationDefinitionDto> getConfigurationDefinitions() {
        return configurationDefinitionMapper.toDtoList(
                configurationDefinitionRepository.findAllByOrderByDisplayOrderAscCodeAsc()
        );
    }

    @Transactional(readOnly = true)
    public ConfigurationDefinitionDto getConfigurationDefinition(Long id) {
        ConfigurationDefinition definition = configurationDefinitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "ConfigurationDefinition with id " + id + " not found"));
        return configurationDefinitionMapper.toDto(definition);
    }

    @Transactional(readOnly = true)
    public ConfigurationDefinitionDetailDto getConfigurationDefinitionDetail(Long id) {
        ConfigurationDefinition definition = configurationDefinitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "ConfigurationDefinition with id " + id + " not found"));
        return configurationDefinitionMapper.toDetailDto(definition);
    }

    @Transactional
    public ConfigurationDefinitionDto createConfigurationDefinition(ConfigurationDefinitionDto request) {
        validateCreate(request);
        ConfigurationDefinition definition = configurationDefinitionMapper.toEntity(request);
        ConfigurationDefinition saved = configurationDefinitionRepository.save(definition);
        return configurationDefinitionMapper.toDto(saved);
    }

    @Transactional
    public ConfigurationDefinitionDto updateConfigurationDefinition(Long id, ConfigurationDefinitionDto request) {
        if (request == null) {
            throw new IllegalArgumentException("ConfigurationDefinition payload is required");
        }

        ConfigurationDefinition existing = configurationDefinitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "ConfigurationDefinition with id " + id + " not found"));

        if (request.getCode() != null && !existing.getCode().equals(request.getCode())
                && configurationDefinitionRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("ConfigurationDefinition code already exists: " + request.getCode());
        }
        if (request.getName() != null && !existing.getName().equals(request.getName())
                && configurationDefinitionRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("ConfigurationDefinition name already exists: " + request.getName());
        }

        configurationDefinitionMapper.updateEntityFromDto(request, existing);
        ConfigurationDefinition saved = configurationDefinitionRepository.save(existing);
        return configurationDefinitionMapper.toDto(saved);
    }

    @Transactional
    public void deleteConfigurationDefinition(Long id) {
        if (!configurationDefinitionRepository.existsById(id)) {
            throw new EntityNotFoundException("ConfigurationDefinition with id " + id + " not found");
        }
        configurationDefinitionRepository.deleteById(id);
    }

    private void validateCreate(ConfigurationDefinitionDto request) {
        if (request == null) {
            throw new IllegalArgumentException("ConfigurationDefinition payload is required");
        }
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new IllegalArgumentException("ConfigurationDefinition code is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("ConfigurationDefinition name is required");
        }
        if (request.getValueType() == null) {
            throw new IllegalArgumentException("ConfigurationDefinition valueType is required");
        }
        if (request.getQuestionForForm() == null || request.getQuestionForForm().isBlank()) {
            throw new IllegalArgumentException("ConfigurationDefinition questionForForm is required");
        }
        if (request.getDisplayOrder() == null) {
            request.setDisplayOrder(0);
        }
        if (configurationDefinitionRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("ConfigurationDefinition code already exists: " + request.getCode());
        }
        if (configurationDefinitionRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("ConfigurationDefinition name already exists: " + request.getName());
        }
    }
}
