package st.tt.script_back.entities;

import java.math.BigDecimal;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chamber_configuration")
public class ChamberConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamber_id", nullable = false)
    private Chamber chamber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuration_definition_id", nullable = false)
    private ConfigurationDefinition configurationDefinition;

    @Column(nullable = false, length = 128)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "nominal_value", precision = 12, scale = 3)
    private BigDecimal nominalValue;

    @Column(name = "min_value", precision = 12, scale = 3)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 12, scale = 3)
    private BigDecimal maxValue;

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
