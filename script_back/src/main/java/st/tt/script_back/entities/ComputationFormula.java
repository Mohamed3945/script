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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.enums.RoundingPolicy;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "computation_formula", uniqueConstraints = {
    @UniqueConstraint(name = "uq_cf_target", columnNames = {
        "recipe_id", "target_step_code", "target_definition_path"
    })
})
public class ComputationFormula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(name = "target_step_code", nullable = false, length = 128)
    private String targetStepCode;

    @Column(name = "target_definition_path", nullable = false, length = 255)
    private String targetDefinitionPath;

    @Column(name = "expression", nullable = false, columnDefinition = "TEXT")
    private String expression;

    @Enumerated(EnumType.STRING)
    @Column(name = "rounding_mode", nullable = false)
    private RoundingPolicy roundingMode = RoundingPolicy.HALF_UP;

    @Column(name = "decimals")
    private Integer decimals;

    @Column(name = "label")
    private String label;

    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;

    @Column(name = "revise_time", nullable = false)
    private Instant reviseTime;

    @OneToMany(mappedBy = "formula", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("slot ASC")
    private List<FormulaReference> references = new ArrayList<>();

    @PrePersist
    @PreUpdate
    protected void validateAndTouch() {
        if (recipe != null && recipe.getRecipeKind() != RecipeKind.GOLDEN) {
            throw new IllegalStateException("Une formule ne peut etre portee que par une recette GOLDEN.");
        }

        Instant now = Instant.now();
        this.reviseTime = now;
        if (this.createTime == null) {
            this.createTime = now;
        }
    }
}