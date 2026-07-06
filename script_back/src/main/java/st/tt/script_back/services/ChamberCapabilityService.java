package st.tt.script_back.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.ChamberCapabilityDto;
import st.tt.script_back.entities.ChamberCapability;
import st.tt.script_back.mappers.ChamberCapabilityMapper;
import st.tt.script_back.repositories.ChamberCapabilityRepository;

@Service
public class ChamberCapabilityService {

    private final ChamberCapabilityRepository chamberCapabilityRepository;
    private final ChamberCapabilityMapper chamberCapabilityMapper;

    public ChamberCapabilityService(
            ChamberCapabilityRepository chamberCapabilityRepository,
            ChamberCapabilityMapper chamberCapabilityMapper) {
        this.chamberCapabilityRepository = chamberCapabilityRepository;
        this.chamberCapabilityMapper = chamberCapabilityMapper;
    }

    @Transactional(readOnly = true)
    public List<ChamberCapabilityDto> getCapabilities() {
        return chamberCapabilityMapper.toDtoList(
                chamberCapabilityRepository.findAllByOrderByCategoryAscLabelAsc()
        );
    }

    @Transactional(readOnly = true)
    public ChamberCapabilityDto getCapability(Long id) {
        ChamberCapability capability = chamberCapabilityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Capability with id " + id + " not found"));
        return chamberCapabilityMapper.toDto(capability);
    }

    @Transactional
    public ChamberCapabilityDto createCapability(ChamberCapabilityDto request) {
        validateCreate(request);
        ChamberCapability capability = chamberCapabilityMapper.toEntity(request);
        ChamberCapability saved = chamberCapabilityRepository.save(capability);
        return chamberCapabilityMapper.toDto(saved);
    }

    @Transactional
    public ChamberCapabilityDto updateCapability(Long id, ChamberCapabilityDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Capability payload is required");
        }

        ChamberCapability existing = chamberCapabilityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Capability with id " + id + " not found"));

        if (request.getCode() != null && !existing.getCode().equals(request.getCode())
                && chamberCapabilityRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Capability code already exists: " + request.getCode());
        }
        if (request.getLabel() != null && !existing.getLabel().equals(request.getLabel())
                && chamberCapabilityRepository.existsByLabel(request.getLabel())) {
            throw new IllegalArgumentException("Capability label already exists: " + request.getLabel());
        }

        chamberCapabilityMapper.updateEntityFromDto(request, existing);
        ChamberCapability saved = chamberCapabilityRepository.save(existing);
        return chamberCapabilityMapper.toDto(saved);
    }

    @Transactional
    public void deleteCapability(Long id) {
        if (!chamberCapabilityRepository.existsById(id)) {
            throw new EntityNotFoundException("Capability with id " + id + " not found");
        }
        chamberCapabilityRepository.deleteById(id);
    }

    private void validateCreate(ChamberCapabilityDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Capability payload is required");
        }
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new IllegalArgumentException("Capability code is required");
        }
        if (request.getLabel() == null || request.getLabel().isBlank()) {
            throw new IllegalArgumentException("Capability label is required");
        }
        if (request.getCategory() == null) {
            throw new IllegalArgumentException("Capability category is required");
        }
        if (chamberCapabilityRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Capability code already exists: " + request.getCode());
        }
        if (chamberCapabilityRepository.existsByLabel(request.getLabel())) {
            throw new IllegalArgumentException("Capability label already exists: " + request.getLabel());
        }
    }
}
