package st.tt.script_back.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import java.time.Instant;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;





/**
 * DecisionOption class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "decision_option")
public class DecisionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String label;
    @Column(nullable = false)
    private String value;
    @Column(nullable = false, name = "order_index")
    private int orderIndex = 0;
    @Column(nullable = false, updatable = false, name = "create_time")
    private Instant createTime;
    @Column(nullable = false, name = "revise_time")
    private Instant reviseTime;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonBackReference("question-options")
    private DecisionQuestion decisionQuestion;

    @JsonManagedReference("option-transitions")
    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DecisionTransition> transitions = new ArrayList<>();

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
    }

    /**
     * Executes onUpdate.
     */
    @PreUpdate
    protected void onUpdate() {
        this.reviseTime = Instant.now();
    }

  
}
