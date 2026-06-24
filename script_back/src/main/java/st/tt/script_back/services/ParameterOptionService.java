package st.tt.script_back.services;

import java.util.Locale;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.ParameterOptionDto;
import st.tt.script_back.entities.ParameterDefinition;
import st.tt.script_back.entities.ParameterOption;
import st.tt.script_back.mappers.ParameterOptionMapper;
import st.tt.script_back.repositories.ParameterDefinitionRepository;
import st.tt.script_back.repositories.ParameterOptionRepository;

@Service
public class ParameterOptionService {

    private final ParameterOptionRepository parameterOptionRepository;
    private final ParameterDefinitionRepository parameterDefinitionRepository;
    private final ParameterOptionMapper parameterOptionMapper;

    public ParameterOptionService(
            ParameterOptionRepository parameterOptionRepository,
            ParameterDefinitionRepository parameterDefinitionRepository,
            ParameterOptionMapper parameterOptionMapper) {
        this.parameterOptionRepository = parameterOptionRepository;
        this.parameterDefinitionRepository = parameterDefinitionRepository;
        this.parameterOptionMapper = parameterOptionMapper;
    }

    @Transactional(readOnly = true)
    public List<ParameterOptionDto> getOptionsByDefinitionId(Long definitionId) {
        if (!parameterDefinitionRepository.existsById(definitionId)) {
            throw new EntityNotFoundException("ParameterDefinition with id " + definitionId + " not found");
        }
        return parameterOptionMapper.toDtoList(parameterOptionRepository.findByDefinitionIdOrderByOrderIndexAsc(definitionId));
    }

    @Transactional
    public ParameterOptionDto createParameterOption(ParameterOptionDto request) {
        if (request == null) {
            throw new IllegalArgumentException("ParameterOption payload is required");
        }
        if (request.getDefinitionId() == null) {
            throw new IllegalArgumentException("definitionId is required");
        }
        if (request.getLabel() == null || request.getLabel().isBlank()) {
            throw new IllegalArgumentException("label is required");
        }

        ParameterDefinition definition = parameterDefinitionRepository.findById(request.getDefinitionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "ParameterDefinition with id " + request.getDefinitionId() + " not found"));

        ParameterOption option = parameterOptionMapper.toEntity(request);
        option.setDefinition(definition);
        option.setCode(ensureUniqueCodeForDefinition(
            definition.getId(),
            toNormalizedCode(request.getLabel(), "OPTION"),
            null));
        if (option.getOrderIndex() == null) {
            option.setOrderIndex(0);
        }

        ParameterOption saved = parameterOptionRepository.save(option);
        return parameterOptionMapper.toDto(saved);
    }

    @Transactional
    public ParameterOptionDto updateParameterOption(Long id, ParameterOptionDto request) {
        if (request == null) {
            throw new IllegalArgumentException("ParameterOption payload is required");
        }

        ParameterOption existing = parameterOptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ParameterOption with id " + id + " not found"));

        if (request.getDefinitionId() != null && existing.getDefinition() != null
                && !request.getDefinitionId().equals(existing.getDefinition().getId())) {
            throw new IllegalArgumentException("definitionId cannot be changed");
        }

        if (request.getLabel() == null || request.getLabel().isBlank()) {
            throw new IllegalArgumentException("label is required");
        }

        existing.setLabel(request.getLabel());
        Long definitionId = existing.getDefinition() != null ? existing.getDefinition().getId() : null;
        if (definitionId == null) {
            throw new IllegalArgumentException("definitionId is required");
        }

        existing.setCode(ensureUniqueCodeForDefinition(
                definitionId,
                toNormalizedCode(request.getLabel(), "OPTION"),
                existing.getId()));
        existing.setOrderIndex(request.getOrderIndex());
        ParameterOption saved = parameterOptionRepository.save(existing);
        return parameterOptionMapper.toDto(saved);
    }

    @Transactional
    public void deleteParameterOption(Long id) {
        if (!parameterOptionRepository.existsById(id)) {
            throw new EntityNotFoundException("ParameterOption with id " + id + " not found");
        }
        parameterOptionRepository.deleteById(id);
    }

    private String toNormalizedCode(String source, String fallbackPrefix) {
        String normalized = source == null ? "" : source
                .trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9_]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "")
                .toUpperCase(Locale.ROOT);

        return normalized.isBlank() ? fallbackPrefix : normalized;
    }

    private String ensureUniqueCodeForDefinition(Long definitionId, String baseCode, Long currentId) {
        String candidate = baseCode;
        int suffix = 2;

        while (true) {
            var existing = parameterOptionRepository.findByDefinitionIdAndCode(definitionId, candidate);
            if (existing.isEmpty() || (currentId != null && currentId.equals(existing.get().getId()))) {
                return candidate;
            }

            candidate = baseCode + "_" + suffix;
            suffix++;
        }
    }
}