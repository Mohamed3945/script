package st.tt.script_back.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Référentiel des versions de Soft Machine installées sur les équipements.
 *
 * Rôle à l'export XML :
 *   - fournit les métadonnées de l'entête RECIPE (Format, xmlns)
 *   - fournit les valeurs par défaut du PASSPORT (ChamberType, Template)
 *
 * Les valeurs des PARAM (ParameterOption.label) sont directement utilisées
 * dans le XML sans mapping version — validé par test bidirectionnel B4_3 ↔ B4_4.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "soft_machine_version", uniqueConstraints = {
    @UniqueConstraint(name = "uq_smv_code", columnNames = {"code"})
})
public class SoftMachineVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Code technique unique.
     * Exemples : "B4_3", "B4_4", "B4_5"
     */
    @Column(nullable = false, length = 64)
    private String code;

    /**
     * Libellé affiché dans l'UI.
     * Exemple : "Soft Machine B4.4.x"
     */
    @Column(nullable = false)
    private String label;

    /**
     * Valeur de l'attribut Format= dans le tag racine <RECIPE>.
     * Toujours "1.0" dans les XML observés.
     */
    @Column(name = "xml_format", nullable = false, length = 32)
    private String xmlFormat = "1.0";

    /**
     * Valeur de l'attribut xmlns= dans le tag racine <RECIPE>.
     * Exemple : "x-schema:recipe_schema.xml"
     */
    @Column(name = "xml_schema", nullable = false, length = 255)
    private String xmlSchema = "x-schema:recipe_schema.xml";

    /**
     * Valeur par défaut de ChamberType dans le PASSPORT.
     * Utilisée si Recipe.chamberType est null.
     * Exemple : "CChRadRPCen"
     */
    @Column(name = "default_chamber_type", length = 128)
    private String defaultChamberType;

    /**
     * Valeur par défaut de Template dans le PASSPORT.
     * Utilisée si Recipe.template est null.
     * Toujours "Default" dans les XML observés.
     */
    @Column(name = "default_template", nullable = false, length = 128)
    private String defaultTemplate = "Default";

    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;

    @Column(name = "revise_time", nullable = false)
    private Instant reviseTime;

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