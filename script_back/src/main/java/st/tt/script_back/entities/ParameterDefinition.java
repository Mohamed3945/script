package st.tt.script_back.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Check;

import jakarta.persistence.CascadeType;
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
import st.tt.script_back.enums.ParameterValueType;
import st.tt.script_back.enums.ParameterScope;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "parameter_definition", uniqueConstraints = {
    @UniqueConstraint(name = "uq_parameter_definition_step_name", columnNames = { "step_type", "name" }),
    @UniqueConstraint(name = "uq_parameter_definition_step_alias", columnNames = { "step_type", "alias" }),
    @UniqueConstraint(name = "uq_parameter_definition_step_code", columnNames = { "step_type", "code" }),
    @UniqueConstraint(name = "uq_parameter_definition_group_order", columnNames = { "parameter_group_id", "order_index_in_group" })
})
@Check(constraints = "default_value_json IS NULL OR JSON_VALID(default_value_json)")
public class ParameterDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String alias;

    @Column(nullable = false, length = 128)
    private String code;

    private String unit;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false)
    private ParameterValueType valueType = ParameterValueType.STRING;

    @Column(name = "required_on_step", nullable = false)
    private boolean requiredOnStep = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false)
    private ParameterScope stepType = ParameterScope.STEP;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuration_definition_id")
    private ConfigurationDefinition configurationDefinition;

    @Column(name = "default_value_json", columnDefinition = "LONGTEXT")
    private String defaultValueJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameter_group_id")
    private ParameterGroup parameterGroupRef;

    @Column(name = "order_index_in_group", nullable = false)
    private Integer orderIndexInGroup = 0;

    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;

    @Column(name = "revise_time", nullable = false)
    private Instant reviseTime;

    @OneToMany(mappedBy = "definition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParameterOption> options = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.reviseTime = now;
        if (this.createTime == null) {
            this.createTime = now;
        }
        if (this.orderIndexInGroup == null) {
            this.orderIndexInGroup = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.reviseTime = Instant.now();
        if (this.orderIndexInGroup == null) {
            this.orderIndexInGroup = 0;
        }
    }
}

