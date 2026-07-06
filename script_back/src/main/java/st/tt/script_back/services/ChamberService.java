package st.tt.script_back.services;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.ChamberCapabilityDto;
import st.tt.script_back.dto.ChamberConfigurationDto;
import st.tt.script_back.dto.ChamberDetailDto;
import st.tt.script_back.dto.ChamberDto;
import st.tt.script_back.entities.Chamber;
import st.tt.script_back.entities.ChamberCapability;
import st.tt.script_back.entities.Machine;
import st.tt.script_back.mappers.ChamberCapabilityMapper;
import st.tt.script_back.mappers.ChamberConfigurationMapper;
import st.tt.script_back.mappers.ChamberMapper;
import st.tt.script_back.repositories.ChamberCapabilityRepository;
import st.tt.script_back.repositories.ChamberConfigurationRepository;
import st.tt.script_back.repositories.ChamberRepository;
import st.tt.script_back.repositories.MachineRepository;

@Service
public class ChamberService {

    private final ChamberRepository chamberRepository;
    private final MachineRepository machineRepository;
    private final ChamberCapabilityRepository chamberCapabilityRepository;
    private final ChamberConfigurationRepository chamberConfigurationRepository;
    private final ChamberMapper chamberMapper;
    private final ChamberCapabilityMapper chamberCapabilityMapper;
    private final ChamberConfigurationMapper chamberConfigurationMapper;

    public ChamberService(
            ChamberRepository chamberRepository,
            MachineRepository machineRepository,
            ChamberCapabilityRepository chamberCapabilityRepository,
            ChamberConfigurationRepository chamberConfigurationRepository,
            ChamberMapper chamberMapper,
            ChamberCapabilityMapper chamberCapabilityMapper,
            ChamberConfigurationMapper chamberConfigurationMapper) {
        this.chamberRepository = chamberRepository;
        this.machineRepository = machineRepository;
        this.chamberCapabilityRepository = chamberCapabilityRepository;
        this.chamberConfigurationRepository = chamberConfigurationRepository;
        this.chamberMapper = chamberMapper;
        this.chamberCapabilityMapper = chamberCapabilityMapper;
        this.chamberConfigurationMapper = chamberConfigurationMapper;
    }

    @Transactional(readOnly = true)
    public List<ChamberDto> getChambersByMachine(Long machineId) {
        ensureMachineExists(machineId);
        return chamberMapper.toDtoList(chamberRepository.findByMachineIdOrderByCodeAsc(machineId));
    }

    @Transactional(readOnly = true)
    public ChamberDto getChamber(Long id) {
        Chamber chamber = chamberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chamber with id " + id + " not found"));
        return chamberMapper.toDto(chamber);
    }

    @Transactional(readOnly = true)
    public ChamberDetailDto getChamberDetail(Long id) {
        Chamber chamber = chamberRepository.findByIdWithMachineCapabilitiesAndConfigurations(id)
                .orElseThrow(() -> new EntityNotFoundException("Chamber with id " + id + " not found"));

        List<ChamberCapabilityDto> capabilities = chamberCapabilityMapper.toDtoList(
                chamber.getCapabilities().stream()
                        .sorted(Comparator.comparing((ChamberCapability c) -> c.getCategory().name())
                                .thenComparing(ChamberCapability::getLabel))
                        .toList());

        List<ChamberConfigurationDto> configurations = chamberConfigurationMapper.toDtoList(
                chamber.getConfigurations().stream()
                        .sorted(Comparator.comparing(
                                (st.tt.script_back.entities.ChamberConfiguration c) -> c.getConfigurationDefinition().getCode())
                                .thenComparing(st.tt.script_back.entities.ChamberConfiguration::getCode))
                        .toList());

        return chamberMapper.toDetailDto(chamber, capabilities, configurations);
    }

    @Transactional
    public ChamberDto createChamber(Long machineId, ChamberDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Chamber payload is required");
        }

        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new EntityNotFoundException("Machine with id " + machineId + " not found"));

        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new IllegalArgumentException("Chamber code is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Chamber name is required");
        }
        if (chamberRepository.existsByMachineIdAndCode(machineId, request.getCode())) {
            throw new IllegalArgumentException("Chamber code already exists for this machine: " + request.getCode());
        }
        if (chamberRepository.existsByMachineIdAndName(machineId, request.getName())) {
            throw new IllegalArgumentException("Chamber name already exists for this machine: " + request.getName());
        }

        Chamber chamber = chamberMapper.toEntity(request);
        chamber.setMachine(machine);
        chamber.setCapabilities(new HashSet<>());
        chamber.setConfigurations(new java.util.ArrayList<>());
        Chamber saved = chamberRepository.save(chamber);
        return chamberMapper.toDto(saved);
    }

    @Transactional
    public ChamberDto updateChamber(Long id, ChamberDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Chamber payload is required");
        }

        Chamber existing = chamberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chamber with id " + id + " not found"));

        Long machineId = existing.getMachine().getId();

        if (request.getCode() != null && !existing.getCode().equals(request.getCode())
                && chamberRepository.existsByMachineIdAndCode(machineId, request.getCode())) {
            throw new IllegalArgumentException("Chamber code already exists for this machine: " + request.getCode());
        }

        if (request.getName() != null && !existing.getName().equals(request.getName())
                && chamberRepository.existsByMachineIdAndName(machineId, request.getName())) {
            throw new IllegalArgumentException("Chamber name already exists for this machine: " + request.getName());
        }

        chamberMapper.updateEntityFromDto(request, existing);
        Chamber saved = chamberRepository.save(existing);
        return chamberMapper.toDto(saved);
    }

    @Transactional
    public void deleteChamber(Long id) {
        if (!chamberRepository.existsById(id)) {
            throw new EntityNotFoundException("Chamber with id " + id + " not found");
        }
        chamberRepository.deleteById(id);
    }

    @Transactional
    public void addCapabilityToChamber(Long chamberId, Long capabilityId) {
        Chamber chamber = chamberRepository.findByIdWithMachineCapabilitiesAndConfigurations(chamberId)
                .orElseThrow(() -> new EntityNotFoundException("Chamber with id " + chamberId + " not found"));

        ChamberCapability capability = chamberCapabilityRepository.findById(capabilityId)
                .orElseThrow(() -> new EntityNotFoundException("Capability with id " + capabilityId + " not found"));

        chamber.getCapabilities().add(capability);
        chamberRepository.save(chamber);
    }

    @Transactional
    public void removeCapabilityFromChamber(Long chamberId, Long capabilityId) {
        Chamber chamber = chamberRepository.findByIdWithMachineCapabilitiesAndConfigurations(chamberId)
                .orElseThrow(() -> new EntityNotFoundException("Chamber with id " + chamberId + " not found"));

        chamber.getCapabilities().removeIf(capability -> capability.getId().equals(capabilityId));
        chamberRepository.save(chamber);
    }

    private void ensureMachineExists(Long machineId) {
        if (!machineRepository.existsById(machineId)) {
            throw new EntityNotFoundException("Machine with id " + machineId + " not found");
        }
    }
}
