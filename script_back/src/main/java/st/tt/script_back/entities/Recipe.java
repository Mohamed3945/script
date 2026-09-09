package st.tt.script_back.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import st.tt.script_back.enums.RecipeIapcMode;
import st.tt.script_back.enums.RecipeKind;
import st.tt.script_back.enums.RecipeResumableMode;
import st.tt.script_back.enums.RecipeStatus;
import st.tt.script_back.enums.RecipeWaferMode;

/**
 * Recipe class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
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

    @Enumerated(EnumType.STRING)
    @Column(name = "wafer", nullable = false)
    private RecipeWaferMode wafer = RecipeWaferMode.PRESENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "iapc", nullable = false)
    private RecipeIapcMode iapc = RecipeIapcMode.NO;

    @Enumerated(EnumType.STRING)
    @Column(name = "resumable", nullable = false)
    private RecipeResumableMode resumable = RecipeResumableMode.NO;

    @Column(name = "chamber_type")
    private String chamberType;

    @Column(name = "access_display_groups")
    private String accessDisplayGroups = "ALL";

    @Column(name = "access_modify_groups")
    private String accessModifyGroups = "ALL";

    @Column(name = "uda_file")
    private String udaFile;

    @Column(name = "type")
    private String type;

    @Column(name = "max_time")
    private Integer maxTime;

    @Column(name = "template")
    private String template;

    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;

    @Column(name = "revise_time", nullable = false)
    private Instant reviseTime;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Step> steps = new ArrayList<>();

        @ManyToMany
        @JoinTable(
            name = "recipe_required_capability",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "capability_id")
        )
        private Set<ChamberCapability> requiredCapabilities = new HashSet<>();

        @ManyToMany
        @JoinTable(
            name = "recipe_required_configuration",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "configuration_definition_id")
        )
        private Set<ConfigurationDefinition> requiredConfigurationDefinitions = new HashSet<>();

    /**
     * Executes onCreate.
     */
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

    /**
     * Executes onUpdate.
     */
    @PreUpdate
    protected void onUpdate() {
        this.reviseTime = Instant.now();
    }
}
