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

/**
 * Recomputes parameter activation states for a recipe from dependency rules.
 * <p>
 * The service evaluates every selected source option, finds matching rules, resolves targets according to
 * the configured scope, then applies a single winning effect on each target parameter.
 * <p>
 * Conflict resolution is deterministic:
 * <ul>
 * <li>higher priority wins first,</li>
 * <li>for equal priority, {@link RuleEffect#DISABLE} wins over {@link RuleEffect#ENABLE}.</li>
 * </ul>
 * If no rule matches a parameter, the fallback state is {@link ActivationState#ENABLED}.
 */
@Service
public class ParameterActivationService {

    private static final int INACTIVE_SOURCE_PRIORITY = Integer.MAX_VALUE;

    private static final Comparator<RuleMatch> RULE_MATCH_COMPARATOR = Comparator
            .comparingInt(RuleMatch::priority)
            .thenComparingInt(match -> effectRank(match.effect()));

    private final StepRepository stepRepository;
    private final StepParameterRepository stepParameterRepository;
    private final ParameterDependencyRuleRepository parameterDependencyRuleRepository;

    /**
     * Creates the activation service with repositories needed for graph traversal and rule evaluation.
     *
     * @param stepRepository reads ordered steps for a recipe.
     * @param stepParameterRepository reads and stores step parameters with required associations.
     * @param parameterDependencyRuleRepository reads dependency rules grouped by source definition.
     */
    public ParameterActivationService(
            StepRepository stepRepository,
            StepParameterRepository stepParameterRepository,
            ParameterDependencyRuleRepository parameterDependencyRuleRepository) {
        this.stepRepository = stepRepository;
        this.stepParameterRepository = stepParameterRepository;
        this.parameterDependencyRuleRepository = parameterDependencyRuleRepository;
    }

    /**
     * Recalculates activation state for all parameters of a recipe in one transactional pass.
     * <p>
     * Processing steps:
     * <ol>
     * <li>load steps and their parameters (with definitions and selected options),</li>
     * <li>index parameters for fast target lookup by definition and by step,</li>
     * <li>collect rule matches for each target parameter from selected source options,</li>
     * <li>pick one winner per target using priority and effect ranking,</li>
     * <li>persist final activation states in batch.</li>
     * </ol>
     * This method is idempotent for unchanged recipe data.
     *
     * @param recipeId recipe identifier whose activation graph must be recomputed.
     */
    @Transactional
    public void recalculateRecipeActivationStates(Long recipeId) {
        List<Step> steps = stepRepository.findByRecipeIdOrderByOrderIndexAsc(recipeId);
        if (steps.isEmpty()) {
            return;
        }

        List<Long> stepIds = steps.stream().map(Step::getId).toList();
        List<StepParameter> parameters = stepParameterRepository
                .findByStepIdsWithDefinitionAndSelectedOption(stepIds);

        Map<Long, List<StepParameter>> parametersByDefinitionId = new HashMap<>();
        Map<Long, Map<Long, StepParameter>> parametersByStepAndDefinition = new HashMap<>();
        Set<Long> definitionIds = new HashSet<>();

        for (StepParameter parameter : parameters) {
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

        Map<Long, List<ParameterDependencyRule>> rulesBySourceDefinitionId = new HashMap<>();
        for (Long definitionId : definitionIds) {
            List<ParameterDependencyRule> sourceRules = parameterDependencyRuleRepository
                    .findBySourceDefinitionIdOrderByPriorityAscIdAsc(definitionId);
            rulesBySourceDefinitionId.put(definitionId, sourceRules);
        }

        Map<Long, ActivationState> currentStates = new HashMap<>();
        for (StepParameter parameter : parameters) {
            ActivationState initialState = parameter.getActivationState() == null
                    ? ActivationState.ENABLED
                    : parameter.getActivationState();
            currentStates.put(parameter.getId(), initialState);
        }

        int maxIterations = Math.max(1, parameters.size());
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            Map<Long, List<RuleMatch>> matchesByTargetParameterId = new HashMap<>();

            for (StepParameter sourceParameter : parameters) {
                if (sourceParameter.getDefinition() == null) {
                    continue;
                }

                Long sourceDefinitionId = sourceParameter.getDefinition().getId();
                List<ParameterDependencyRule> rules = rulesBySourceDefinitionId.getOrDefault(sourceDefinitionId, List.of());

                ActivationState sourceState = currentStates.getOrDefault(sourceParameter.getId(), ActivationState.ENABLED);
                if (sourceState == ActivationState.DISABLED) {
                    for (ParameterDependencyRule rule : rules) {
                        List<StepParameter> targetParameters = resolveTargets(rule, sourceParameter, parametersByDefinitionId,
                                parametersByStepAndDefinition);
                        for (StepParameter targetParameter : targetParameters) {
                            matchesByTargetParameterId
                                    .computeIfAbsent(targetParameter.getId(), ignored -> new ArrayList<>())
                                    .add(new RuleMatch(RuleEffect.DISABLE, INACTIVE_SOURCE_PRIORITY));
                        }
                    }
                    continue;
                }

                if (sourceParameter.getSelectedOption() == null || sourceParameter.getSelectedOption().getId() == null) {
                    continue;
                }

                Long selectedOptionId = sourceParameter.getSelectedOption().getId();

                for (ParameterDependencyRule rule : rules) {
                    if (rule.getTriggerOption() == null || rule.getTriggerOption().getId() == null) {
                        continue;
                    }
                    if (!selectedOptionId.equals(rule.getTriggerOption().getId())) {
                        continue;
                    }
                    if (!matchesRequiredSourceActivationContext(rule, sourceParameter)) {
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

            Map<Long, ActivationState> nextStates = new HashMap<>();
            for (StepParameter parameter : parameters) {
                List<RuleMatch> matches = matchesByTargetParameterId.getOrDefault(parameter.getId(), List.of());

                ActivationState newState;
                if (matches.isEmpty()) {
                    // No matching rule means this parameter must stay editable by default.
                    newState = ActivationState.ENABLED;
                } else {
                    RuleMatch winningMatch = matches.stream()
                            .sorted(RULE_MATCH_COMPARATOR.reversed())
                            .findFirst()
                            .orElse(null);
                    newState = winningMatch == null ? ActivationState.ENABLED : toActivationState(winningMatch.effect());
                }

                nextStates.put(parameter.getId(), newState);
            }

            if (nextStates.equals(currentStates)) {
                currentStates = nextStates;
                break;
            }

            currentStates = nextStates;
        }

        for (StepParameter parameter : parameters) {
            parameter.setActivationState(currentStates.getOrDefault(parameter.getId(), ActivationState.ENABLED));
        }

        stepParameterRepository.saveAll(parameters);
    }

    /**
     * Resolves target parameters affected by a matching rule for a given source parameter.
     * <p>
     * Scope behavior:
     * <ul>
     * <li>{@link RuleScope#STEP}: only the parameter with the target definition in the same step is eligible,</li>
     * <li>recipe-level scope: all parameters with the target definition in the same recipe are eligible.</li>
     * </ul>
     *
     * @param rule matching dependency rule.
     * @param sourceParameter source parameter that triggered the rule.
     * @param parametersByDefinitionId parameters indexed by definition id.
     * @param parametersByStepAndDefinition parameters indexed by step id then definition id.
     * @return target parameters that should receive this rule effect.
     * @throws EntityNotFoundException when recipe context is missing for a recipe-scoped resolution.
     */
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

    /**
     * Converts a rule effect to the persisted activation state.
     *
     * @param effect winning rule effect.
     * @return {@link ActivationState#ENABLED} for null or ENABLE, {@link ActivationState#DISABLED} for DISABLE.
     */
    private static ActivationState toActivationState(RuleEffect effect) {
        if (effect == null) {
            return ActivationState.ENABLED;
        }
        return switch (effect) {
            case ENABLE -> ActivationState.ENABLED;
            case DISABLE -> ActivationState.DISABLED;
        };
    }

    /**
     * Checks whether the optional source-activation context required by a rule is satisfied.
     * <p>
     * A rule without required source activation option always matches.
     * Otherwise, the source parameter must have a parent parameter with a selected option equal to the required one.
     *
     * @param rule candidate dependency rule.
     * @param sourceParameter source parameter being evaluated.
     * @return {@code true} when contextual source activation requirements are satisfied.
     */
    private static boolean matchesRequiredSourceActivationContext(
            ParameterDependencyRule rule,
            StepParameter sourceParameter) {
        if (rule.getRequiredSourceActivationOption() == null
                || rule.getRequiredSourceActivationOption().getId() == null) {
            return true;
        }

        if (sourceParameter.getParentStepParameter() == null
                || sourceParameter.getParentStepParameter().getSelectedOption() == null
                || sourceParameter.getParentStepParameter().getSelectedOption().getId() == null) {
            return false;
        }

        Long requiredId = rule.getRequiredSourceActivationOption().getId();
        Long parentSelectedOptionId = sourceParameter.getParentStepParameter().getSelectedOption().getId();
        return requiredId.equals(parentSelectedOptionId);
    }

    /**
     * Provides a rank used as tie-breaker when two matches have the same priority.
     *
     * @param effect rule effect to rank.
     * @return rank where DISABLE is stronger than ENABLE.
     */
    private static int effectRank(RuleEffect effect) {
        if (effect == null) {
            return 0;
        }
        return switch (effect) {
            case ENABLE -> 1;
            case DISABLE -> 2;
        };
    }

    private record RuleMatch(RuleEffect effect, Integer priority) {
        /**
         * Returns normalized priority, defaulting null to 0 for deterministic ordering.
         *
         * @return non-null priority value.
         */
        public Integer priority() {
            return priority == null ? 0 : priority;
        }
    }
}
