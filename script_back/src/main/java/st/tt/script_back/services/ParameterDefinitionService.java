package st.tt.script_back.services;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.ParameterDefinitionDto;
import st.tt.script_back.dto.ParameterDefinitionReorderRequestDto;
import st.tt.script_back.entities.ParameterDefinition;
import st.tt.script_back.entities.ParameterGroup;
import st.tt.script_back.enums.ParameterValueType;
import st.tt.script_back.enums.StepType;
import st.tt.script_back.mappers.ParameterDefinitionMapper;
import st.tt.script_back.repositories.ParameterDefinitionRepository;
import st.tt.script_back.repositories.ParameterGroupRepository;
import st.tt.script_back.repositories.StepParameterRepository;

@Service
public class ParameterDefinitionService {

    private final ParameterDefinitionRepository parameterDefinitionRepository;
    private final ParameterDefinitionMapper parameterDefinitionMapper;
    private final StepParameterRepository stepParameterRepository;
    private final ParameterGroupRepository parameterGroupRepository;
    private static final Logger log = LoggerFactory.getLogger(ParameterDefinitionService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ParameterDefinitionService(
            ParameterDefinitionRepository parameterDefinitionRepository,
            ParameterDefinitionMapper parameterDefinitionMapper,
            StepParameterRepository stepParameterRepository,
            ParameterGroupRepository parameterGroupRepository) {
        this.parameterDefinitionRepository = parameterDefinitionRepository;
        this.parameterDefinitionMapper = parameterDefinitionMapper;
        this.stepParameterRepository = stepParameterRepository;
        this.parameterGroupRepository = parameterGroupRepository;
    }

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

        if (request.getParameterGroupId() != null) {
            ParameterGroup group = parameterGroupRepository.findById(request.getParameterGroupId())
                    .orElseThrow(() -> new EntityNotFoundException("ParameterGroup not found"));
            definition.setParameterGroupRef(group);

            int nextOrder = parameterDefinitionRepository
                    .findTopByParameterGroupRefIdOrderByOrderIndexInGroupDesc(group.getId())
                    .map(d -> d.getOrderIndexInGroup() == null ? 0 : d.getOrderIndexInGroup() + 1)
                    .orElse(0);
            definition.setOrderIndexInGroup(nextOrder);
        } else {
            definition.setOrderIndexInGroup(0);
        }

        definition.setDefaultValueJson(
                normalizeJsonValue(definition.getDefaultValueJson(), definition.getValueType()));

        String baseCode = toNormalizedCode(request.getName(), "PARAMETER");
        definition.setCode(ensureUniqueCode(baseCode, null, definition.getStepType()));

        ParameterDefinition saved = parameterDefinitionRepository.save(definition);
        return parameterDefinitionMapper.toDto(saved);
    }

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

    @Transactional(readOnly = true)
    public ParameterDefinitionDto getParameterDefinition(Long id) {
        ParameterDefinition definition = parameterDefinitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ParameterDefinition with id " + id + " not found"));
        return parameterDefinitionMapper.toDto(definition);
    }

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

        if (existing.getStepType() == null) {
            existing.setStepType(StepType.STEP);
        }

        existing.setDefaultValueJson(
                normalizeJsonValue(existing.getDefaultValueJson(), existing.getValueType()));

        String baseCode = toNormalizedCode(existing.getName(), "PARAMETER");
        existing.setCode(ensureUniqueCode(baseCode, existing.getId(), existing.getStepType()));

        ParameterDefinition saved = parameterDefinitionRepository.save(existing);
        return parameterDefinitionMapper.toDto(saved);
    }

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

    @Transactional
    public void reorderDefinitionsInGroup(ParameterDefinitionReorderRequestDto request) {
        log.info("reorderDefinitionsInGroup called with groupId={} orderedDefinitionIds={}",
                request != null ? request.getParameterGroupId() : null,
                request != null ? request.getOrderedDefinitionIds() : null);

        if (request == null) {
            throw new IllegalArgumentException("Reorder request is required");
        }
        if (request.getParameterGroupId() == null) {
            throw new IllegalArgumentException("parameterGroupId is required");
        }
        if (request.getOrderedDefinitionIds() == null || request.getOrderedDefinitionIds().isEmpty()) {
            throw new IllegalArgumentException("orderedDefinitionIds is required");
        }

        List<ParameterDefinition> definitions = parameterDefinitionRepository
                .findByParameterGroupRefIdOrderByOrderIndexInGroupAsc(request.getParameterGroupId());

        log.info("definitions loaded for groupId={} -> ids={}",
                request.getParameterGroupId(),
                definitions.stream().map(ParameterDefinition::getId).toList());

        if (definitions.isEmpty()) {
            return;
        }

        Set<Long> existingIds = definitions.stream()
                .map(ParameterDefinition::getId)
                .collect(java.util.stream.Collectors.toSet());

        Set<Long> requestedIds = new java.util.HashSet<>(request.getOrderedDefinitionIds());

        log.info("existingIds={} requestedIds={}", existingIds, requestedIds);

        if (!existingIds.equals(requestedIds)) {
            throw new IllegalArgumentException(
                    "orderedDefinitionIds must contain exactly all definitions of the target group");
        }

        Map<Long, ParameterDefinition> definitionById = definitions.stream()
                .collect(java.util.stream.Collectors.toMap(ParameterDefinition::getId, d -> d));

        int tempBase = 1000;
        for (int i = 0; i < request.getOrderedDefinitionIds().size(); i++) {
            Long definitionId = request.getOrderedDefinitionIds().get(i);
            ParameterDefinition definition = definitionById.get(definitionId);
            if (definition == null) {
                throw new IllegalStateException("Definition id " + definitionId + " not found in group map");
            }
            definition.setOrderIndexInGroup(tempBase + i);
        }

        parameterDefinitionRepository.saveAll(definitions);
        parameterDefinitionRepository.flush();

        for (int i = 0; i < request.getOrderedDefinitionIds().size(); i++) {
            Long definitionId = request.getOrderedDefinitionIds().get(i);
            ParameterDefinition definition = definitionById.get(definitionId);
            definition.setOrderIndexInGroup(i);
        }

        parameterDefinitionRepository.saveAll(definitions);
        log.info("reorderDefinitionsInGroup completed for groupId={}", request.getParameterGroupId());
    }
    @Transactional
    public ParameterDefinitionDto moveDefinitionToGroup(st.tt.script_back.dto.ParameterDefinitionMoveRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Move request is required");
        }
        if (request.getDefinitionId() == null) {
            throw new IllegalArgumentException("definitionId is required");
        }
        if (request.getTargetGroupId() == null) {
            throw new IllegalArgumentException("targetGroupId is required");
        }

        ParameterDefinition definition = parameterDefinitionRepository.findById(request.getDefinitionId())
                .orElseThrow(() -> new EntityNotFoundException("ParameterDefinition not found"));

        ParameterGroup targetGroup = parameterGroupRepository.findById(request.getTargetGroupId())
                .orElseThrow(() -> new EntityNotFoundException("Target ParameterGroup not found"));

        if (definition.getStepType() != targetGroup.getStepType()) {
            throw new IllegalArgumentException("Definition stepType must match target group stepType");
        }

        Long currentGroupId = definition.getParameterGroupRef() != null ? definition.getParameterGroupRef().getId() : null;

        if (currentGroupId != null && currentGroupId.equals(targetGroup.getId())) {
            // same group, just reorder insertion if needed
            List<ParameterDefinition> sameGroupDefinitions =
                    parameterDefinitionRepository.findByParameterGroupRefIdOrderByOrderIndexInGroupAsc(targetGroup.getId());

            sameGroupDefinitions.removeIf(d -> d.getId().equals(definition.getId()));

            int requestedIndex = request.getTargetIndex() == null ? sameGroupDefinitions.size() : request.getTargetIndex();
            int safeIndex = Math.max(0, Math.min(requestedIndex, sameGroupDefinitions.size()));
            sameGroupDefinitions.add(safeIndex, definition);

            for (int i = 0; i < sameGroupDefinitions.size(); i++) {
                sameGroupDefinitions.get(i).setOrderIndexInGroup(i);
            }

            parameterDefinitionRepository.saveAll(sameGroupDefinitions);
            return parameterDefinitionMapper.toDto(definition);
        }

        // Remove from old group and reindex old siblings
        if (definition.getParameterGroupRef() != null) {
            List<ParameterDefinition> oldGroupDefinitions =
                    parameterDefinitionRepository.findByParameterGroupRefIdOrderByOrderIndexInGroupAsc(
                            definition.getParameterGroupRef().getId());

            oldGroupDefinitions.removeIf(d -> d.getId().equals(definition.getId()));

            for (int i = 0; i < oldGroupDefinitions.size(); i++) {
                oldGroupDefinitions.get(i).setOrderIndexInGroup(i);
            }

            parameterDefinitionRepository.saveAll(oldGroupDefinitions);
        }

        // Insert into target group
        List<ParameterDefinition> targetDefinitions =
                parameterDefinitionRepository.findByParameterGroupRefIdOrderByOrderIndexInGroupAsc(targetGroup.getId());

        definition.setParameterGroupRef(targetGroup);

        int requestedIndex = request.getTargetIndex() == null ? targetDefinitions.size() : request.getTargetIndex();
        int safeIndex = Math.max(0, Math.min(requestedIndex, targetDefinitions.size()));

        targetDefinitions.add(safeIndex, definition);

        for (int i = 0; i < targetDefinitions.size(); i++) {
            targetDefinitions.get(i).setOrderIndexInGroup(i);
        }

        parameterDefinitionRepository.saveAll(targetDefinitions);
        return parameterDefinitionMapper.toDto(definition);
    }

    @Transactional(readOnly = true)
    public List<ParameterDefinitionDto> findByConfigurationDefinitionId(Long configDefId) {
        return parameterDefinitionRepository
                .findByConfigurationDefinitionIdOrderByNameAsc(configDefId)
                .stream()
                .map(parameterDefinitionMapper::toDto)
                .toList();
    }
    private String normalizeJsonValue(String rawValue, ParameterValueType valueType) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        if (valueType == null) {
            return rawValue;
        }

        String trimmed = rawValue.trim();

        try {
            switch (valueType) {
                case STRING:
                    if (isJsonStringLiteral(trimmed)) {
                        return trimmed;
                    }
                    return objectMapper.writeValueAsString(trimmed);

                case NUMBER:
                    JsonNode numberNode = objectMapper.readTree(trimmed);
                    if (!numberNode.isNumber()) {
                        throw new IllegalArgumentException("Default value must be a valid number");
                    }
                    return numberNode.toString();

                case BOOLEAN:
                    JsonNode booleanNode = objectMapper.readTree(trimmed);
                    if (!booleanNode.isBoolean()) {
                        throw new IllegalArgumentException("Default value must be true or false");
                    }
                    return booleanNode.toString();

                case JSON:
                    objectMapper.readTree(trimmed);
                    return trimmed;

                case ENUM:
                    return null;

                default:
                    return trimmed;
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Invalid default value for valueType " + valueType + ": " + ex.getMessage(), ex);
        }
    }

    private boolean isJsonStringLiteral(String value) {
        try {
            JsonNode node = objectMapper.readTree(value);
            return node.isTextual();
        } catch (Exception ex) {
            return false;
        }
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