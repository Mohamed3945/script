package st.tt.script_back.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import java.time.Instant;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;


/**
 * DecisionTransition class for the backend domain.
 * <p>
 * This type exposes behavior used by the application service layer.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "decision_transition")
public class DecisionTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, name ="create_time", updatable = false)
    private Instant createTime;
    @Column(nullable = false, name ="revise_time")
    private Instant reviseTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_question_id", nullable = false)
    @JsonBackReference("question-current-transitions")
    private DecisionQuestion currentQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)   
    @JsonBackReference("option-transitions")
    private DecisionOption option;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_question_id")
    @JsonBackReference("question-next-transitions")
    private DecisionQuestion nextQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_profile_id")   
    @JsonBackReference("result-profile-transitions")
    private DecisionResultProfile resultProfile;

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
