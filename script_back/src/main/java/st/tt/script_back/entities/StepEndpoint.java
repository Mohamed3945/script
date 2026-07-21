package st.tt.script_back.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Table(name = "step_endpoint")
public class StepEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false, unique = true)
    private Step step;

    // null → <ENDPOINT />  (step Time, pas de condition)
    // "AND" / "OR" → <ENDPOINT Clause="AND"> ... </ENDPOINT>
    @Column(length = 8)
    private String clause;

    @Column(name = "locked_by_golden", nullable = false)
    private boolean lockedByGolden = false;

    @OneToMany(mappedBy = "endpoint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StepEndpointCondition> conditions = new ArrayList<>();

    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;

    @Column(name = "revise_time", nullable = false)
    private Instant reviseTime;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.reviseTime = now;
        if (this.createTime == null) this.createTime = now;
    }

    @PreUpdate
    protected void onUpdate() { this.reviseTime = Instant.now(); }
}