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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.enums.RecipeStatus;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "recipe")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipe_kind", nullable = false)
    private RecipeKind recipeKind = RecipeKind.DERIVED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_recipe_id")
    private Recipe parentRecipe;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "creator", nullable = false)
    private Long creatorId;

    @Column(name = "revisor")
    private Long revisorId;

    @Column(name = "process_family")
    private String processFamily;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecipeStatus status = RecipeStatus.DRAFT;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(nullable = false)
    private boolean frozen = false;

    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;

    @Column(name = "revise_time", nullable = false)
    private Instant reviseTime;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Step> steps = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.reviseTime = now;
        if (this.createTime == null) {
            this.createTime = now;
        }
        if (this.recipeKind == RecipeKind.DERIVED && this.parentRecipe == null) {
            throw new IllegalStateException("A derived recipe must reference a parent recipe.");
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.reviseTime = Instant.now();
    }
}
