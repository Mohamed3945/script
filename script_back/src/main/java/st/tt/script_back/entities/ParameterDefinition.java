package st.tt.script_back.entities;

import java.time.Instant;

import org.hibernate.annotations.Check;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.ParameterValueType;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "parameter_definition")
@Check(constraints = "default_value_json IS NULL OR JSON_VALID(default_value_json)")
public class ParameterDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String alias;

    @Column(nullable = false, unique = true, length = 128)
    private String code;

    private String unit;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false)
    private ParameterValueType valueType = ParameterValueType.STRING;

    @Column(name = "required_on_step", nullable = false)
    private boolean requiredOnStep = true;

    @Column(name = "default_value_json", columnDefinition = "LONGTEXT")
    private String defaultValueJson;

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
    }

    @PreUpdate
    protected void onUpdate() {
        this.reviseTime = Instant.now();
    }
}
