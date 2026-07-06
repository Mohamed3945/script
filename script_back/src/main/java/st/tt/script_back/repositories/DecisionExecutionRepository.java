package st.tt.script_back.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import st.tt.script_back.entities.DecisionExecution;

public interface DecisionExecutionRepository extends JpaRepository<DecisionExecution, Long> {

	@Modifying
	@Query("""
			update DecisionExecution de
			set de.createdDerivedRecipeId = null
			where de.createdDerivedRecipeId = :recipeId
			""")
	int clearCreatedDerivedRecipeReference(Long recipeId);
}
