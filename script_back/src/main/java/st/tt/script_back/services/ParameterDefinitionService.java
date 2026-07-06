package st.tt.script_back.services;

import java.util.Locale;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.ParameterDefinitionDto;
import st.tt.script_back.entities.ParameterDefinition;
import st.tt.script_back.enums.StepType;
import st.tt.script_back.mappers.ParameterDefinitionMapper;
import st.tt.script_back.repositories.ParameterDefinitionRepository;
import st.tt.script_back.repositories.StepParameterRepository;

/**
 * ParameterDefinitionService class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Service
public class ParameterDefinitionService {

    private final ParameterDefinitionRepository parameterDefinitionRepository;
    private final ParameterDefinitionMapper parameterDefinitionMapper;
    private final StepParameterRepository stepParameterRepository;

    /**
     * Executes ParameterDefinitionService.
     *
     * @param parameterDefinitionRepository input argument consumed by ParameterDefinitionService.
     * @param parameterDefinitionMapper input argument consumed by ParameterDefinitionService.
     * @param stepParameterRepository input argument consumed by ParameterDefinitionService.
     */
    public ParameterDefinitionService(
            ParameterDefinitionRepository parameterDefinitionRepository,
            ParameterDefinitionMapper parameterDefinitionMapper,
            StepParameterRepository stepParameterRepository) {
        this.parameterDefinitionRepository = parameterDefinitionRepository;
        this.parameterDefinitionMapper = parameterDefinitionMapper;
        this.stepParameterRepository = stepParameterRepository;
    }

    /**
     * Executes createParameterDefinition.
     *
     * @param request input argument consumed by createParameterDefinition.
     * @return computed ParameterDefinitionDto result returned by createParameterDefinition.
     */
    @Transactional
    public ParameterDefinitionDto createParameterDefinition(ParameterDefinitionDto request) {
        if (request == null) {
            throw new IllegalArgumentException("ParameterDefinition payload is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }

        ParameterDefinition definition = parameterDefinitionMapper.toEntity(request);
        if (definition.getStepType() == null) {
            definition.setStepType(StepType.STEP);
        }
        String baseCode = toNormalizedCode(request.getName(), "PARAMETER");
        definition.setCode(ensureUniqueCode(baseCode, null, definition.getStepType()));
        ParameterDefinition saved = parameterDefinitionRepository.save(definition);
        return parameterDefinitionMapper.toDto(saved);
    }

    /**
     * Executes getParameterDefinitions.
     *
     * @param stepType input argument consumed by getParameterDefinitions.
     * @return computed List<ParameterDefinitionDto> result returned by getParameterDefinitions.
     */
    @Transactional(readOnly = true)
    public List<ParameterDefinitionDto> getParameterDefinitions(StepType stepType) {
        List<ParameterDefinition> definitions;
        if (stepType == null) {
            definitions = parameterDefinitionRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        } else {
            definitions = parameterDefinitionRepository.findByStepTypeOrderByNameAsc(stepType);
        }
        return parameterDefinitionMapper.toDtoList(definitions);
    }

    /**
     * Executes getParameterDefinition.
     *
     * @param id input argument consumed by getParameterDefinition.
     * @return computed ParameterDefinitionDto result returned by getParameterDefinition.
     */
    @Transactional(readOnly = true)
    public ParameterDefinitionDto getParameterDefinition(Long id) {
        ParameterDefinition definition = parameterDefinitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ParameterDefinition with id " + id + " not found"));
        return parameterDefinitionMapper.toDto(definition);
    }

    /**
     * Executes updateParameterDefinition.
     *
     * @param id input argument consumed by updateParameterDefinition.
     * @param request input argument consumed by updateParameterDefinition.
     * @return computed ParameterDefinitionDto result returned by updateParameterDefinition.
     */
    @Transactional
    public ParameterDefinitionDto updateParameterDefinition(Long id, ParameterDefinitionDto request) {
        if (request == null) {
            throw new IllegalArgumentException("ParameterDefinition payload is required");
        }

        ParameterDefinition existing = parameterDefinitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ParameterDefinition with id " + id + " not found"));
        parameterDefinitionMapper.updateEntityFromDto(request, existing);

        if (existing.getName() == null || existing.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }

        String baseCode = toNormalizedCode(existing.getName(), "PARAMETER");
        if (existing.getStepType() == null) {
            existing.setStepType(StepType.STEP);
        }
        existing.setCode(ensureUniqueCode(baseCode, existing.getId(), existing.getStepType()));

        ParameterDefinition saved = parameterDefinitionRepository.save(existing);
        return parameterDefinitionMapper.toDto(saved);
    }

    /**
     * Executes deleteParameterDefinition.
     *
     * @param id input argument consumed by deleteParameterDefinition.
     */
    @Transactional
    public void deleteParameterDefinition(Long id) {
        if (!parameterDefinitionRepository.existsById(id)) {
            throw new EntityNotFoundException("ParameterDefinition with id " + id + " not found");
        }

        if (stepParameterRepository.existsByDefinitionId(id)) {
            throw new IllegalStateException(
                    "Delete failed: this parameter is used in recipes.");
        }

        if (stepParameterRepository.existsBySelectedOptionDefinitionId(id)) {
            throw new IllegalStateException(
                    "Delete failed: an option of this parameter is already used in recipes.");
        }

        parameterDefinitionRepository.deleteById(id);
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

    private String ensureUniqueCode(String baseCode, Long currentId, StepType stepType) {
        String candidate = baseCode;
        int suffix = 2;

        while (true) {
            var existing = parameterDefinitionRepository.findByCodeAndStepType(candidate, stepType);
            if (existing.isEmpty() || (currentId != null && currentId.equals(existing.get().getId()))) {
                return candidate;
            }

            candidate = baseCode + "_" + suffix;
            suffix++;
        }
    }
}
