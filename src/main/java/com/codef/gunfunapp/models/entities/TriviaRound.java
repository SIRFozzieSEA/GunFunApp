package com.codef.gunfunapp.models.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString(includeFieldNames = true)
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "trivia_rounds")
public class TriviaRound {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "round_pk")
	private long roundPk;

	@Column(name = "user_name", length = 20)
	private String roundUser;

	@Column(name = "round_no_of_questions")
	private long roundNoOfQuestions;

	@Column(name = "round_no_of_questions_correct")
	private long roundNoOfQuestionsCorrect;

	@Column(name = "round_score", precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2)")
	private BigDecimal roundScore;

	@Column(name = "round_played_date")
	private java.sql.Date roundPlayedDate;

}
