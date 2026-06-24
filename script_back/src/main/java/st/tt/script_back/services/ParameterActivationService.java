package st.tt.script_back.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import st.tt.script_back.entities.ParameterDependencyRule;
import st.tt.script_back.entities.Step;
import st.tt.script_back.entities.StepParameter;
import st.tt.script_back.enums.ActivationState;
import st.tt.script_back.enums.RuleEffect;
import st.tt.script_back.enums.RuleScope;
import st.tt.script_back.repositories.ParameterDependencyRuleRepository;
import st.tt.script_back.repositories.StepParameterRepository;
import st.tt.script_back.repositories.StepRepository;

@Service
public class ParameterActivationService {

    private static final Comparator<RuleMatch> RULE_MATCH_COMPARATOR = Comparator
            .comparingInt(RuleMatch::priority)
            .thenComparingInt(match -> effectRank(match.effect()));

    private final StepRepository stepRepository;
    private final StepParameterRepository stepParameterRepository;
    private final ParameterDependencyRuleRepository parameterDependencyRuleRepository;

    public ParameterActivationService(
            StepRepository stepRepository,
            StepParameterRepository stepParameterRepository,
            ParameterDependencyRuleRepository parameterDependencyRuleRepository) {
        this.stepRepository = stepRepository;
        this.stepParameterRepository = stepParameterRepository;
        this.parameterDependencyRuleRepository = parameterDependencyRuleRepository;
    }

    @Transactional
    public void recalculateRecipeActivationStates(Long recipeId) {
        List<Step> steps = stepRepository.findByRecipeIdOrderByOrderIndexAsc(recipeId);
        if (steps.isEmpty()) {
            return;
        }

        List<Long> stepIds = steps.stream().map(Step::getId).toList();
        List<StepParameter> parameters = stepParameterRepository
                .findByStepIdsWithDefinitionAndSelectedOption(stepIds);

        Map<Long, StepParameter> parameterById = new HashMap<>();
        Map<Long, List<StepParameter>> parametersByDefinitionId = new HashMap<>();
        Map<Long, Map<Long, StepParameter>> parametersByStepAndDefinition = new HashMap<>();
        Set<Long> definitionIds = new HashSet<>();

        for (StepParameter parameter : parameters) {
            parameterById.put(parameter.getId(), parameter);
            Long definitionId = parameter.getDefinition() != null ? parameter.getDefinition().getId() : null;
            Long stepId = parameter.getStep() != null ? parameter.getStep().getId() : null;
            if (definitionId != null) {
                definitionIds.add(definitionId);
                parametersByDefinitionId.computeIfAbsent(definitionId, ignored -> new ArrayList<>()).add(parameter);
            }
            if (stepId != null && definitionId != null) {
                parametersByStepAndDefinition
                        .computeIfAbsent(stepId, ignored -> new HashMap<>())
                        .put(definitionId, parameter);
            }
        }

        Map<Long, Boolean> hasIncomingRulesByDefinitionId = new HashMap<>();
        Map<Long, List<ParameterDependencyRule>> rulesBySourceDefinitionId = new HashMap<>();
        for (Long definitionId : definitionIds) {
            List<ParameterDependencyRule> incomingRules = parameterDependencyRuleRepository
                    .findByTargetDefinitionIdOrderByPriorityAscIdAsc(definitionId);
            hasIncomingRulesByDefinitionId.put(definitionId, !incomingRules.isEmpty());

            List<ParameterDependencyRule> sourceRules = parameterDependencyRuleRepository
                    .findBySourceDefinitionIdOrderByPriorityAscIdAsc(definitionId);
            rulesBySourceDefinitionId.put(definitionId, sourceRules);
        }

        Map<Long, List<RuleMatch>> matchesByTargetParameterId = new HashMap<>();

        for (StepParameter sourceParameter : parameters) {
            if (sourceParameter.getSelectedOption() == null || sourceParameter.getDefinition() == null) {
                continue;
            }

            Long sourceDefinitionId = sourceParameter.getDefinition().getId();
            Long selectedOptionId = sourceParameter.getSelectedOption().getId();
            List<ParameterDependencyRule> rules = rulesBySourceDefinitionId.getOrDefault(sourceDefinitionId, List.of());

            for (ParameterDependencyRule rule : rules) {
                if (rule.getTriggerOption() == null || rule.getTriggerOption().getId() == null) {
                    continue;
                }
                if (!selectedOptionId.equals(rule.getTriggerOption().getId())) {
                    continue;
                }

                List<StepParameter> targetParameters = resolveTargets(rule, sourceParameter, parametersByDefinitionId,
                        parametersByStepAndDefinition);
                for (StepParameter targetParameter : targetParameters) {
                    matchesByTargetParameterId
                            .computeIfAbsent(targetParameter.getId(), ignored -> new ArrayList<>())
                            .add(new RuleMatch(rule.getEffect(), rule.getPriority()));
                }
            }
        }

        for (StepParameter parameter : parameters) {
            Long definitionId = parameter.getDefinition() != null ? parameter.getDefinition().getId() : null;
            boolean hasIncomingRules = definitionId != null && Boolean.TRUE.equals(hasIncomingRulesByDefinitionId.get(definitionId));
            List<RuleMatch> matches = matchesByTargetParameterId.getOrDefault(parameter.getId(), List.of());

            ActivationState newState;
            if (matches.isEmpty()) {
                newState = hasIncomingRules ? ActivationState.WAIT : ActivationState.ENABLED;
            } else {
                RuleMatch winningMatch = matches.stream()
                        .sorted(RULE_MATCH_COMPARATOR.reversed())
                        .findFirst()
                        .orElse(null);
                newState = winningMatch == null ? ActivationState.WAIT : toActivationState(winningMatch.effect());
            }

            parameter.setActivationState(newState);
        }

        stepParameterRepository.saveAll(parameters);
    }

    private List<StepParameter> resolveTargets(
            ParameterDependencyRule rule,
            StepParameter sourceParameter,
            Map<Long, List<StepParameter>> parametersByDefinitionId,
            Map<Long, Map<Long, StepParameter>> parametersByStepAndDefinition) {
        Long targetDefinitionId = rule.getTargetDefinition() != null ? rule.getTargetDefinition().getId() : null;
        if (targetDefinitionId == null) {
            return List.of();
        }

        if (rule.getScope() == RuleScope.STEP) {
            Long stepId = sourceParameter.getStep() != null ? sourceParameter.getStep().getId() : null;
            if (stepId == null) {
                return List.of();
            }
            StepParameter target = parametersByStepAndDefinition
                    .getOrDefault(stepId, Map.of())
                    .get(targetDefinitionId);
            return target == null ? List.of() : List.of(target);
        }

        Long recipeId = sourceParameter.getStep() != null && sourceParameter.getStep().getRecipe() != null
                ? sourceParameter.getStep().getRecipe().getId()
                : null;
        if (recipeId == null) {
            throw new EntityNotFoundException("Source parameter recipe not found");
        }

        List<StepParameter> targets = parametersByDefinitionId.getOrDefault(targetDefinitionId, List.of());
        return targets.stream()
                .filter(target -> target.getStep() != null
                        && target.getStep().getRecipe() != null
                        && recipeId.equals(target.getStep().getRecipe().getId()))
                .toList();
    }

    private static ActivationState toActivationState(RuleEffect effect) {
        if (effect == null) {
            return ActivationState.WAIT;
        }
        return switch (effect) {
            case ENABLE -> ActivationState.ENABLED;
            case DISABLE -> ActivationState.DISABLED;
            case WAIT -> ActivationState.WAIT;
        };
    }

    private static int effectRank(RuleEffect effect) {
        if (effect == null) {
            return 0;
        }
        return switch (effect) {
            case WAIT -> 1;
            case ENABLE -> 2;
            case DISABLE -> 3;
        };
    }

    private record RuleMatch(RuleEffect effect, Integer priority) {
        public Integer priority() {
            return priority == null ? 0 : priority;
        }
    }
}