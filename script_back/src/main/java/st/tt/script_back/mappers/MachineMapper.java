package st.tt.script_back.mappers;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import st.tt.script_back.dto.ChamberDto;
import st.tt.script_back.dto.MachineDetailDto;
import st.tt.script_back.dto.MachineDto;
import st.tt.script_back.entities.Machine;

@Component
public class MachineMapper {

    public MachineDto toDto(Machine machine) {
        if (machine == null) {
            return null;
        }
        return new MachineDto(
                machine.getId(),
                machine.getCode(),
                machine.getName(),
                machine.getPlatformType()
        );
    }

    public MachineDetailDto toDetailDto(Machine machine, List<ChamberDto> chambers) {
        if (machine == null) {
            return null;
        }
        return new MachineDetailDto(
                machine.getId(),
                machine.getCode(),
                machine.getName(),
                machine.getPlatformType(),
                chambers == null ? Collections.emptyList() : chambers
        );
    }

    public List<MachineDto> toDtoList(List<Machine> machines) {
        if (machines == null) {
            return Collections.emptyList();
        }
        return machines.stream().map(this::toDto).toList();
    }

    public Machine toEntity(MachineDto dto) {
        if (dto == null) {
            return null;
        }
        Machine entity = new Machine();
        entity.setId(dto.getId());
        updateEntityFromDto(dto, entity);
        return entity;
    }

    public void updateEntityFromDto(MachineDto dto, Machine entity) {
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setPlatformType(dto.getPlatformType());
    }
}
