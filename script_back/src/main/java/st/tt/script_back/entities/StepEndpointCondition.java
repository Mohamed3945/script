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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.EndpointOperator;
import st.tt.script_back.enums.ParameterScope;
import st.tt.script_back.enums.ParameterValueType;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Table(name = "step_endpoint_condition")
@Check(constraints = "(value_json IS NULL) <> (selected_option_id IS NULL)")
public class StepEndpointCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id", nullable = false)
    private StepEndpoint endpoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_parameter_id", nullable = false)
    private ParameterDefinition endpointParameter;

    @Column(name = "value_json", columnDefinition = "LONGTEXT")
    private String valueJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private ParameterOption selectedOption;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private EndpointOperator operator;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;

    @Column(name = "revise_time", nullable = false)
    private Instant reviseTime;

    @PrePersist
    @PreUpdate
    protected void validateAndTouch() {
        if ((this.valueJson == null) == (this.selectedOption == null)) {
            throw new IllegalStateException("Exactly one of value_json or selected_option_id must be set.");
        }

        if (this.endpointParameter == null) {
            throw new IllegalStateException("StepEndpointCondition.endpointParameter is required.");
        }

        if (this.endpointParameter.getStepType() != ParameterScope.ENDPOINT) {
            throw new IllegalStateException("endpoint_parameter_id must reference an ENDPOINT parameter definition.");
        }

        ParameterValueType valueType = this.endpointParameter.getValueType();

        if (this.selectedOption != null) {
            if (valueType != ParameterValueType.ENUM) {
                throw new IllegalStateException("selected_option_id is only valid for ENUM endpoint parameters.");
            }
            if (this.selectedOption.getDefinition() != null
                    && this.selectedOption.getDefinition().getId() != null
                    && this.endpointParameter.getId() != null
                    && !this.selectedOption.getDefinition().getId().equals(this.endpointParameter.getId())) {
                throw new IllegalStateException("selected_option_id must belong to endpoint_parameter_id.");
            }
        }

        if (this.valueJson != null && valueType == ParameterValueType.ENUM) {
            throw new IllegalStateException("ENUM endpoint parameters must use selected_option_id, not value_json.");
        }

        boolean numericOperator = this.operator == EndpointOperator.LT
                || this.operator == EndpointOperator.LTE
                || this.operator == EndpointOperator.GT
                || this.operator == EndpointOperator.GTE;

        if (numericOperator && valueType != ParameterValueType.NUMBER) {
            throw new IllegalStateException("LT/LTE/GT/GTE operators are only valid for NUMBER endpoint parameters.");
        }

        Instant now = Instant.now();
        this.reviseTime = now;
        if (this.createTime == null) this.createTime = now;
    }
}