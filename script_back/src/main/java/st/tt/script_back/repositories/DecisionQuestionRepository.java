package st.tt.script_back.repositories;

import st.tt.script_back.entities.DecisionQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DecisionQuestionRepository extends JpaRepository<DecisionQuestion, Long> {

    @Query("""
            select distinct q
            from DecisionQuestion q
            left join fetch q.options o
            where q.code = :code
            """)
    Optional<DecisionQuestion> findByCodeWithOptions(String code);

    @Query("""
            select distinct q
            from DecisionQuestion q
            left join fetch q.options o
            where q.id = :id
            """)
    Optional<DecisionQuestion> findByIdWithOptions(Long id);


    @Query("""
            select distinct q
            from DecisionQuestion q
            left join fetch q.options o
            where q.entryPoint = true
            """)
    Optional<DecisionQuestion> findEntryPointQuestionWithOptions();

}
