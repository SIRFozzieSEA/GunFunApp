package com.codef.gunfunapp.models.entities;

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
@Table(name = "trivia_round_questions")
public class TriviaRoundQuestion {

	@Column(name = "round_pk")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long roundPk;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "question_pk")
	private long questionPk;

	@Column(name = "question_is_answered")
	private Boolean questionIsAnswered;

	@Column(name = "question_is_correct")
	private Boolean questionIsCorrect;

	@Column(name = "question_type", length = 20)
	private String questionType;

	@Column(name = "question", length = 2000)
	private String question;

	@Column(name = "question_responses", length = 2000)
	private String questionResponses;

	@Column(name = "correct_response", length = 50)
	private String correctResponse;

	@Column(name = "user_response", length = 50)
	private String userResponse;

	@Column(name = "image_location", length = 200)
	private String imageLocation;

	@Column(name = "nickname", length = 20)
	private String nickname;

}
