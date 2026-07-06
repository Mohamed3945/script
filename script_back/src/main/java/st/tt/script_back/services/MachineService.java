package st.tt.script_back.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.ChamberDto;
import st.tt.script_back.dto.MachineDetailDto;
import st.tt.script_back.dto.MachineDto;
import st.tt.script_back.entities.Machine;
import st.tt.script_back.mappers.ChamberMapper;
import st.tt.script_back.mappers.MachineMapper;
import st.tt.script_back.repositories.MachineRepository;

@Service
public class MachineService {

    private final MachineRepository machineRepository;
    private final MachineMapper machineMapper;
    private final ChamberMapper chamberMapper;

    public MachineService(
            MachineRepository machineRepository,
            MachineMapper machineMapper,
            ChamberMapper chamberMapper) {
        this.machineRepository = machineRepository;
        this.machineMapper = machineMapper;
        this.chamberMapper = chamberMapper;
    }

    @Transactional(readOnly = true)
    public List<MachineDto> getMachines() {
        return machineMapper.toDtoList(machineRepository.findAllByOrderByReviseTimeDesc());
    }

    @Transactional(readOnly = true)
    public MachineDto getMachine(Long id) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Machine with id " + id + " not found"));
        return machineMapper.toDto(machine);
    }

    @Transactional(readOnly = true)
    public MachineDetailDto getMachineDetail(Long id) {
        Machine machine = machineRepository.findByIdWithChambers(id)
                .orElseThrow(() -> new EntityNotFoundException("Machine with id " + id + " not found"));
        List<ChamberDto> chambers = chamberMapper.toDtoList(machine.getChambers());
        return machineMapper.toDetailDto(machine, chambers);
    }

    @Transactional
    public MachineDto createMachine(MachineDto request) {
        validateCreate(request);
        Machine machine = machineMapper.toEntity(request);
        Machine saved = machineRepository.save(machine);
        return machineMapper.toDto(saved);
    }

    @Transactional
    public MachineDto updateMachine(Long id, MachineDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Machine payload is required");
        }

        Machine existing = machineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Machine with id " + id + " not found"));

        if (request.getCode() != null && !existing.getCode().equals(request.getCode())
                && machineRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Machine code already exists: " + request.getCode());
        }
        if (request.getName() != null && !existing.getName().equals(request.getName())
                && machineRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Machine name already exists: " + request.getName());
        }

        machineMapper.updateEntityFromDto(request, existing);
        Machine saved = machineRepository.save(existing);
        return machineMapper.toDto(saved);
    }

    @Transactional
    public void deleteMachine(Long id) {
        if (!machineRepository.existsById(id)) {
            throw new EntityNotFoundException("Machine with id " + id + " not found");
        }
        machineRepository.deleteById(id);
    }

    private void validateCreate(MachineDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Machine payload is required");
        }
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new IllegalArgumentException("Machine code is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Machine name is required");
        }
        if (request.getPlatformType() == null) {
            throw new IllegalArgumentException("Machine platformType is required");
        }
        if (machineRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Machine code already exists: " + request.getCode());
        }
        if (machineRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Machine name already exists: " + request.getName());
        }
    }
}
