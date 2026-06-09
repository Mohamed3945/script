package st.tt.script_back.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import st.tt.script_back.enums.QuestionType;
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
@Table(name = "decision_question")
public class DecisionQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String code;
    @Column(nullable = false)
    private String label;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "question_type")
    private QuestionType questionType = QuestionType.SINGLE_CHOICE;
    @Column(name = "is_entry_point", nullable = false)
    private boolean entryPoint = false;
    @Column(nullable = false, name = "order_index")
    private int orderIndex = 0;
    @Column(nullable = false)
    private boolean active = true;
    @Column(nullable = false, updatable = false, name = "create_time")
    private Instant createTime;
    @Column(nullable = false, name = "revise_time")
    private Instant reviseTime;

    @JsonManagedReference("question-options")
    @OneToMany(mappedBy = "decisionQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DecisionOption> options = new ArrayList<>();

    @JsonManagedReference("question-current-transitions")
    @OneToMany(mappedBy = "currentQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DecisionTransition> transitionToThisQuestion = new ArrayList<>();

    @JsonManagedReference("question-next-transitions")
    @OneToMany(mappedBy = "nextQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DecisionTransition> transitionFromThisQuestion = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.questionType == null) {
            this.questionType = QuestionType.SINGLE_CHOICE;
        }
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