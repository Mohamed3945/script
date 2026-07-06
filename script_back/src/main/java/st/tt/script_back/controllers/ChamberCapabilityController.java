package st.tt.script_back.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import st.tt.script_back.dto.ChamberCapabilityDto;
import st.tt.script_back.services.ChamberCapabilityService;

@RestController
@RequestMapping("/api/chamber-capabilities")
public class ChamberCapabilityController {

    private final ChamberCapabilityService chamberCapabilityService;

    public ChamberCapabilityController(ChamberCapabilityService chamberCapabilityService) {
        this.chamberCapabilityService = chamberCapabilityService;
    }

    @GetMapping
    public List<ChamberCapabilityDto> getCapabilities() {
        return chamberCapabilityService.getCapabilities();
    }

    @GetMapping("/{id}")
    public ChamberCapabilityDto getCapability(@PathVariable Long id) {
        return chamberCapabilityService.getCapability(id);
    }

    @PostMapping
    public ChamberCapabilityDto createCapability(@RequestBody ChamberCapabilityDto request) {
        return chamberCapabilityService.createCapability(request);
    }

    @PutMapping("/{id}")
    public ChamberCapabilityDto updateCapability(@PathVariable Long id, @RequestBody ChamberCapabilityDto request) {
        return chamberCapabilityService.updateCapability(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteCapability(@PathVariable Long id) {
        chamberCapabilityService.deleteCapability(id);
    }
}
