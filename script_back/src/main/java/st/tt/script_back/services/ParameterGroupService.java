package st.tt.script_back.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.ParameterGroupDto;
import st.tt.script_back.dto.ParameterGroupReorderRequestDto;
import st.tt.script_back.entities.ParameterGroup;
import st.tt.script_back.enums.StepType;
import st.tt.script_back.repositories.ParameterGroupRepository;

@Service
public class ParameterGroupService {

    private final ParameterGroupRepository parameterGroupRepository;

    public ParameterGroupService(ParameterGroupRepository parameterGroupRepository) {
        this.parameterGroupRepository = parameterGroupRepository;
    }

    @Transactional(readOnly = true)
    public List<ParameterGroupDto> getGroups(StepType stepType) {
        List<ParameterGroup> groups = parameterGroupRepository.findByStepTypeOrderByOrderIndexAsc(stepType);
        return groups.stream()
                .map(g -> new ParameterGroupDto(g.getId(), g.getName(), g.getStepType(), g.getOrderIndex()))
                .toList();
    }

    @Transactional
    public ParameterGroupDto createGroup(ParameterGroupDto request) {
        if (request == null) {
            throw new IllegalArgumentException("ParameterGroup payload is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Group name is required");
        }
        if (request.getStepType() == null) {
            throw new IllegalArgumentException("stepType is required");
        }

        int nextOrder = parameterGroupRepository.findTopByStepTypeOrderByOrderIndexDesc(request.getStepType())
                .map(group -> group.getOrderIndex() == null ? 0 : group.getOrderIndex() + 1)
                .orElse(0);

        ParameterGroup group = new ParameterGroup();
        group.setName(request.getName().trim());
        group.setStepType(request.getStepType());
        group.setOrderIndex(nextOrder);

        ParameterGroup saved = parameterGroupRepository.save(group);
        return new ParameterGroupDto(saved.getId(), saved.getName(), saved.getStepType(), saved.getOrderIndex());
    }

    @Transactional
    public ParameterGroupDto updateGroup(ParameterGroupDto request) {
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("Group id is required");
        }

        ParameterGroup existing = parameterGroupRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("ParameterGroup not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            existing.setName(request.getName().trim());
        }

        ParameterGroup saved = parameterGroupRepository.save(existing);
        return new ParameterGroupDto(saved.getId(), saved.getName(), saved.getStepType(), saved.getOrderIndex());
    }

    @Transactional
    public void reorderGroups(ParameterGroupReorderRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Reorder request is required");
        }
        if (request.getStepType() == null) {
            throw new IllegalArgumentException("stepType is required");
        }
        if (request.getOrderedGroupIds() == null || request.getOrderedGroupIds().isEmpty()) {
            throw new IllegalArgumentException("orderedGroupIds is required");
        }

        List<ParameterGroup> groups = parameterGroupRepository.findByStepTypeOrderByOrderIndexAsc(request.getStepType());
        if (groups.isEmpty()) {
            return;
        }

        Set<Long> existingIds = groups.stream()
                .map(ParameterGroup::getId)
                .collect(java.util.stream.Collectors.toSet());

        Set<Long> requestedIds = new HashSet<>(request.getOrderedGroupIds());

        if (!existingIds.equals(requestedIds)) {
            throw new IllegalArgumentException(
                    "orderedGroupIds must contain exactly all group ids of the selected stepType");
        }

        Map<Long, ParameterGroup> groupById = groups.stream()
                .collect(java.util.stream.Collectors.toMap(ParameterGroup::getId, g -> g));

        // Phase 1: move to temporary safe indexes to avoid unique collisions
        int tempBase = 1000;
        for (int i = 0; i < request.getOrderedGroupIds().size(); i++) {
            Long groupId = request.getOrderedGroupIds().get(i);
            ParameterGroup group = groupById.get(groupId);
            group.setOrderIndex(tempBase + i);
        }
        parameterGroupRepository.saveAll(groups);
        parameterGroupRepository.flush();

        // Phase 2: assign final indexes
        for (int i = 0; i < request.getOrderedGroupIds().size(); i++) {
            Long groupId = request.getOrderedGroupIds().get(i);
            ParameterGroup group = groupById.get(groupId);
            group.setOrderIndex(i);
        }
        parameterGroupRepository.saveAll(groups);
    }
}