package st.tt.script_back.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "formula_reference",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_fr_slot", columnNames = {"formula_id", "slot"})
    },
    indexes = {
        @Index(name = "idx_fr_source", columnList = "step_code,definition_path")
    })
public class FormulaReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "formula_id", nullable = false)
    private ComputationFormula formula;

    @Column(name = "slot", nullable = false)
    private Integer slot;

    @Column(name = "step_code", nullable = false, length = 128)
    private String stepCode;

    @Column(name = "definition_path", nullable = false, length = 255)
    private String definitionPath;
}