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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.RuleEffect;
import st.tt.script_back.enums.RuleScope;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "parameter_dependency_rule", uniqueConstraints = {
    @UniqueConstraint(name = "uq_pdr_source_trigger_target", columnNames = {
        "source_definition_id", "trigger_option_id", "target_definition_id"
    })
})
@Check(constraints = "source_definition_id <> target_definition_id")
public class ParameterDependencyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_definition_id", nullable = false)
    private ParameterDefinition sourceDefinition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trigger_option_id", nullable = false)
    private ParameterOption triggerOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_definition_id", nullable = false)
    private ParameterDefinition targetDefinition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleEffect effect;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleScope scope;

    @Column(nullable = false)
    private Integer priority = 0;

    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;

    @Column(name = "revise_time", nullable = false)
    private Instant reviseTime;

    @PrePersist
    @PreUpdate
    protected void validateAndTouch() {
        if (sourceDefinition != null && targetDefinition != null && sourceDefinition.getId() != null
                && sourceDefinition.getId().equals(targetDefinition.getId())) {
            throw new IllegalStateException("A dependency rule cannot target its own source definition.");
        }

        if (triggerOption != null && sourceDefinition != null
                && triggerOption.getDefinition() != null
                && triggerOption.getDefinition().getId() != null
                && sourceDefinition.getId() != null
                && !triggerOption.getDefinition().getId().equals(sourceDefinition.getId())) {
            throw new IllegalStateException("Trigger option must belong to source definition.");
        }

        Instant now = Instant.now();
        this.reviseTime = now;
        if (this.createTime == null) {
            this.createTime = now;
        }
    }
}
