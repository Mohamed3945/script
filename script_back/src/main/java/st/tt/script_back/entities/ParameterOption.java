package st.tt.script_back.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "parameter_option", uniqueConstraints = {
@UniqueConstraint(name = "uq_po_definition_label", columnNames = {"definition_id", "label"}),
@UniqueConstraint(name = "uq_po_definition_code", columnNames = {"definition_id", "code"})
})
public class ParameterOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "definition_id", nullable = false)
    private ParameterDefinition definition;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false, length = 128)
    private String code;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;

    @Column(name = "revise_time", nullable = false)
    private Instant reviseTime;

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
