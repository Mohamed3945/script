package st.tt.script_back.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.ChamberConfigurationDto;
import st.tt.script_back.entities.Chamber;
import st.tt.script_back.entities.ChamberConfiguration;
import st.tt.script_back.entities.ConfigurationDefinition;
import st.tt.script_back.mappers.ChamberConfigurationMapper;
import st.tt.script_back.repositories.ChamberConfigurationRepository;
import st.tt.script_back.repositories.ChamberRepository;
import st.tt.script_back.repositories.ConfigurationDefinitionRepository;

@Service
public class ChamberConfigurationService {

    private final ChamberConfigurationRepository chamberConfigurationRepository;
    private final ChamberRepository chamberRepository;
    private final ConfigurationDefinitionRepository configurationDefinitionRepository;
    private final ChamberConfigurationMapper chamberConfigurationMapper;

    public ChamberConfigurationService(
            ChamberConfigurationRepository chamberConfigurationRepository,
            ChamberRepository chamberRepository,
            ConfigurationDefinitionRepository configurationDefinitionRepository,
            ChamberConfigurationMapper chamberConfigurationMapper) {
        this.chamberConfigurationRepository = chamberConfigurationRepository;
        this.chamberRepository = chamberRepository;
        this.configurationDefinitionRepository = configurationDefinitionRepository;
        this.chamberConfigurationMapper = chamberConfigurationMapper;
    }

    @Transactional(readOnly = true)
    public List<ChamberConfigurationDto> getByChamber(Long chamberId) {
        ensureChamberExists(chamberId);
        return chamberConfigurationMapper.toDtoList(
                chamberConfigurationRepository.findByChamberIdOrdered(chamberId)
        );
    }

    @Transactional(readOnly = true)
    public ChamberConfigurationDto getById(Long id) {
        ChamberConfiguration configuration = chamberConfigurationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "ChamberConfiguration with id " + id + " not found"));
        return chamberConfigurationMapper.toDto(configuration);
    }

    @Transactional
    public ChamberConfigurationDto createForChamber(Long chamberId, ChamberConfigurationDto request) {
        if (request == null) {
            throw new IllegalArgumentException("ChamberConfiguration payload is required");
        }

        Chamber chamber = chamberRepository.findById(chamberId)
                .orElseThrow(() -> new EntityNotFoundException("Chamber with id " + chamberId + " not found"));

        if (request.getConfigurationDefinitionId() == null) {
            throw new IllegalArgumentException("configurationDefinitionId is required");
        }

        ConfigurationDefinition definition = configurationDefinitionRepository.findById(request.getConfigurationDefinitionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "ConfigurationDefinition with id " + request.getConfigurationDefinitionId() + " not found"));

        if (request.getChamberConfigurationCode() == null || request.getChamberConfigurationCode().isBlank()) {
            throw new IllegalArgumentException("chamberConfigurationCode is required");
        }

        if (request.getChamberConfigurationName() == null || request.getChamberConfigurationName().isBlank()) {
            throw new IllegalArgumentException("chamberConfigurationName is required");
        }

        if (chamberConfigurationRepository.existsByChamberIdAndConfigurationDefinitionIdAndCode(
                chamberId,
                request.getConfigurationDefinitionId(),
                request.getChamberConfigurationCode())) {
            throw new IllegalArgumentException(
                    "A chamber configuration with this chamber/definition/code already exists");
        }

        ChamberConfiguration configuration = chamberConfigurationMapper.toEntity(request);
        configuration.setChamber(chamber);
        configuration.setConfigurationDefinition(definition);

        ChamberConfiguration saved = chamberConfigurationRepository.save(configuration);
        return chamberConfigurationMapper.toDto(saved);
    }

    @Transactional
    public ChamberConfigurationDto update(Long id, ChamberConfigurationDto request) {
        if (request == null) {
            throw new IllegalArgumentException("ChamberConfiguration payload is required");
        }

        ChamberConfiguration existing = chamberConfigurationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "ChamberConfiguration with id " + id + " not found"));

        Long chamberId = existing.getChamber() != null ? existing.getChamber().getId() : null;
        Long definitionId = request.getConfigurationDefinitionId() != null
                ? request.getConfigurationDefinitionId()
                : existing.getConfigurationDefinition().getId();

        if (request.getConfigurationDefinitionId() != null
                && !request.getConfigurationDefinitionId().equals(existing.getConfigurationDefinition().getId())) {
            ConfigurationDefinition definition = configurationDefinitionRepository.findById(request.getConfigurationDefinitionId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "ConfigurationDefinition with id " + request.getConfigurationDefinitionId() + " not found"));
            existing.setConfigurationDefinition(definition);
        }

        if (request.getChamberConfigurationCode() != null
                && !existing.getCode().equals(request.getChamberConfigurationCode())
                && chamberConfigurationRepository.existsByChamberIdAndConfigurationDefinitionIdAndCode(
                        chamberId,
                        definitionId,
                        request.getChamberConfigurationCode())) {
            throw new IllegalArgumentException(
                    "A chamber configuration with this chamber/definition/code already exists");
        }

        chamberConfigurationMapper.updateEntityFromDto(request, existing);
        ChamberConfiguration saved = chamberConfigurationRepository.save(existing);
        return chamberConfigurationMapper.toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!chamberConfigurationRepository.existsById(id)) {
            throw new EntityNotFoundException("ChamberConfiguration with id " + id + " not found");
        }
        chamberConfigurationRepository.deleteById(id);
    }

    private void ensureChamberExists(Long chamberId) {
        if (!chamberRepository.existsById(chamberId)) {
            throw new EntityNotFoundException("Chamber with id " + chamberId + " not found");
        }
    }
}
