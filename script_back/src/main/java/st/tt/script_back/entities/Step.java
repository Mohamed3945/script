package st.tt.script_back.entities;

import java.time.Instant;

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
import st.tt.script_back.enums.StepKind;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "step", uniqueConstraints = {
    @UniqueConstraint(name = "uq_step_recipe_order", columnNames = {"recipe_id", "order_index"}),
    @UniqueConstraint(name = "uq_step_recipe_code", columnNames = {"recipe_id", "code"})
})
public class Step {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_kind", nullable = false)
    private StepKind stepKind = StepKind.STEP;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 128)
    private String code;

    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;

    @Column(name = "revise_time", nullable = false)
    private Instant reviseTime;

    @OneToMany(mappedBy = "step", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StepParameter> parameters = new ArrayList<>();

    @PrePersist
    @PreUpdate
    protected void validateAndTouch() {
        if (this.stepKind == StepKind.PRESTEP && (this.orderIndex == null || this.orderIndex != 0)) {
            throw new IllegalStateException("PRESTEP must have order_index = 0.");
        }
        if (this.stepKind == StepKind.STEP && (this.orderIndex == null || this.orderIndex < 1)) {
            throw new IllegalStateException("STEP must have order_index >= 1.");
        }

        Instant now = Instant.now();
        this.reviseTime = now;
        if (this.createTime == null) {
            this.createTime = now;
        }
    }
}
