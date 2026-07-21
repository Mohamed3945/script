package st.tt.script_back.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.ParameterScope;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "parameter_group", uniqueConstraints = {
    @UniqueConstraint(name = "uq_parameter_group_step_type_name", columnNames = {"step_type", "name"}),
    @UniqueConstraint(name = "uq_parameter_group_step_type_order", columnNames = {"step_type", "order_index"})
})
public class ParameterGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false)
    private ParameterScope stepType = ParameterScope.STEP;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;

    @Column(name = "revise_time", nullable = false)
    private Instant reviseTime;

    @OneToMany(mappedBy = "parameterGroupRef", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<ParameterDefinition> definitions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createTime = this.createTime == null ? now : this.createTime;
        this.reviseTime = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.reviseTime = Instant.now();
    }
}

