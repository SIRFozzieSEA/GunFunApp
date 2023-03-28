package com.codef.gunfunapp.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.codef.gunfunapp.models.entities.TriviaRoundQuestion;

@Repository
public interface TriviaRoundQuestionRepo extends JpaRepository<TriviaRoundQuestion, Long> {

	@Query("SELECT gtrqr FROM TriviaRoundQuestion gtrqr WHERE gtrqr.roundPk=:roundPk AND questionIsAnswered=false ORDER BY questionPk")
	List<TriviaRoundQuestion> findByRoundPk(@Param("roundPk") Long roundPk);

}