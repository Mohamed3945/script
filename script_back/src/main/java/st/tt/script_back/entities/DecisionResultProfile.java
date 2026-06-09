package st.tt.script_back.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.util.List;
import java.util.ArrayList;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import lombok.NoArgsConstructor;



@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "decision_result_profile")
public class DecisionResultProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String code;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false)
    private boolean active = true;
    @Column(nullable = false, updatable = false, name = "create_time")
    private Instant createTime;
    @Column(nullable = false, name = "revise_time")
    private Instant reviseTime;
    private Long goldenRecipeId;
    private Long machineId;

    @JsonManagedReference("result-profile-transitions")
    @OneToMany(mappedBy = "resultProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DecisionTransition> transitions = new ArrayList<>();

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
