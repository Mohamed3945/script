package st.tt.script_back.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.dto.ParameterDependencyRuleViewDto;
import st.tt.script_back.entities.ParameterDependencyRule;
import st.tt.script_back.mappers.ParameterDependencyRuleViewMapper;
import st.tt.script_back.repositories.ParameterDependencyRuleRepository;

/**
 * ParameterDependencyRuleQueryService class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Service
public class ParameterDependencyRuleQueryService {

    private final ParameterDependencyRuleRepository parameterDependencyRuleRepository;
    private final ParameterDependencyRuleViewMapper parameterDependencyRuleViewMapper;

    /**
     * Executes ParameterDependencyRuleQueryService.
     *
     * @param parameterDependencyRuleRepository input argument consumed by ParameterDependencyRuleQueryService.
     * @param parameterDependencyRuleViewMapper input argument consumed by ParameterDependencyRuleQueryService.
     */
    public ParameterDependencyRuleQueryService(
            ParameterDependencyRuleRepository parameterDependencyRuleRepository,
            ParameterDependencyRuleViewMapper parameterDependencyRuleViewMapper) {
        this.parameterDependencyRuleRepository = parameterDependencyRuleRepository;
        this.parameterDependencyRuleViewMapper = parameterDependencyRuleViewMapper;
    }

    /**
     * Executes getRules.
     * @return computed List<ParameterDependencyRuleViewDto> result returned by getRules.
     */
    @Transactional(readOnly = true)
    public List<ParameterDependencyRuleViewDto> getRules() {
        List<ParameterDependencyRule> rules = parameterDependencyRuleRepository.findAllForView();
        return parameterDependencyRuleViewMapper.toDtoList(rules);
    }

    /**
     * Executes getRule.
     *
     * @param id input argument consumed by getRule.
     * @return computed ParameterDependencyRuleViewDto result returned by getRule.
     */
    @Transactional(readOnly = true)
    public ParameterDependencyRuleViewDto getRule(Long id) {
        ParameterDependencyRule rule = parameterDependencyRuleRepository.findByIdForView(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "ParameterDependencyRule with id " + id + " not found"));
        return parameterDependencyRuleViewMapper.toDto(rule);
    }
}
