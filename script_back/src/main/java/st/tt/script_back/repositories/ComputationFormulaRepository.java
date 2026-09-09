package st.tt.script_back.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import st.tt.script_back.entities.ComputationFormula;

public interface ComputationFormulaRepository extends JpaRepository<ComputationFormula, Long> {

        @Query("""
                        select cf
                        from ComputationFormula cf
                        left join fetch cf.references ref
                        where cf.id = :id
                        """)
        Optional<ComputationFormula> findByIdWithReferences(@Param("id") Long id);

    @Query("""
            select distinct cf
            from ComputationFormula cf
            left join fetch cf.references ref
            where cf.recipe.id = :recipeId
            order by cf.id asc
            """)
    List<ComputationFormula> findByRecipeIdWithReferences(@Param("recipeId") Long recipeId);

    Optional<ComputationFormula> findByRecipeIdAndTargetStepCodeAndTargetDefinitionPath(
            Long recipeId,
            String targetStepCode,
            String targetDefinitionPath);

    boolean existsByRecipeIdAndTargetStepCodeAndTargetDefinitionPath(
            Long recipeId,
            String targetStepCode,
            String targetDefinitionPath);
}
