package st.tt.script_back.entities;

import java.time.Instant;

import org.hibernate.annotations.Check;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.ActivationState;
import st.tt.script_back.enums.ComputationStatus;
import st.tt.script_back.enums.ParameterValueType;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "step_parameter", uniqueConstraints = {
    @UniqueConstraint(name = "uq_sp_order_in_scope", columnNames = {"step_id", "parent_order_scope", "order_index"}),
    @UniqueConstraint(name = "uq_sp_definition_per_scope", columnNames = {
        "step_id", "parent_order_scope", "definition_id"
    })
})
/**
 * StepParameter class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Check(constraints = "NOT (value_json IS NOT NULL AND selected_option_id IS NOT NULL)")
@Check(constraints = "value_json IS NULL OR JSON_VALID(value_json)")
public class StepParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private Step step;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "definition_id", nullable = false)
    private ParameterDefinition definition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_step_parameter_id")
    private StepParameter parentStepParameter;

    @Column(name = "parent_order_scope", nullable = false)
    private Long parentOrderScope = 0L;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "label_override")
    private String labelOverride;

    @Column(name = "value_json", columnDefinition = "LONGTEXT")
    private String valueJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private ParameterOption selectedOption;

    @Enumerated(EnumType.STRING)
    @Column(name = "activation_state", nullable = false)
    private ActivationState activationState = ActivationState.ENABLED;

    @Column(name = "locked_by_golden", nullable = false)
    private boolean lockedByGolden = false;

    @Column(name = "user_modified", nullable = false)
    private boolean userModified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "computation_status")
    private ComputationStatus computationStatus;

    @Column(name = "computed_at")
    private Instant computedAt;

    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;

    @Column(name = "revise_time", nullable = false)
    private Instant reviseTime;

    @OneToMany(mappedBy = "parentStepParameter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StepParameter> childStepParameters = new ArrayList<>();

    /**
     * Executes validateAndTouch.
     */
    @PrePersist
    @PreUpdate
    protected void validateAndTouch() {
        this.parentOrderScope = (this.parentStepParameter == null) ? 0L : this.parentStepParameter.getId();

        if (this.valueJson != null && this.selectedOption != null) {
            throw new IllegalStateException("Only one of value_json or selected_option_id can be set.");
        }

        if (this.definition == null) {
            throw new IllegalStateException("StepParameter.definition is required.");
        }

        if (this.selectedOption != null) {
            if (this.definition.getValueType() != ParameterValueType.ENUM) {
                throw new IllegalStateException("selected_option_id is only valid for ENUM parameters.");
            }
            if (this.selectedOption.getDefinition() != null
                    && this.selectedOption.getDefinition().getId() != null
                    && this.definition.getId() != null
                    && !this.selectedOption.getDefinition().getId().equals(this.definition.getId())) {
                throw new IllegalStateException("selected_option_id must belong to the same definition_id.");
            }
        }

        if (this.valueJson != null && this.definition.getValueType() == ParameterValueType.ENUM) {
            throw new IllegalStateException("ENUM parameters must use selected_option_id, not value_json.");
        }

        if (this.computationStatus != null && this.lockedByGolden) {
            throw new IllegalStateException("Un parametre calcule ne peut pas etre lockedByGolden.");
        }

        Instant now = Instant.now();
        this.reviseTime = now;
        if (this.createTime == null) {
            this.createTime = now;
        }
    }
}
