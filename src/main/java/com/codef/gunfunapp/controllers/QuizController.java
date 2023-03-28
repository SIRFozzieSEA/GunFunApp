package com.codef.gunfunapp.controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.codef.gunfunapp.models.entities.TriviaQuestionTemplate;
import com.codef.gunfunapp.models.entities.TriviaRound;
import com.codef.gunfunapp.models.entities.TriviaRoundQuestion;
import com.codef.gunfunapp.other.ControllerUtils;
import com.codef.gunfunapp.other.ModelUtils;
import com.codef.gunfunapp.other.SystemUtils;
import com.codef.gunfunapp.repos.RegistryRepo;
import com.codef.gunfunapp.repos.TriviaQuestionTemplateRepo;
import com.codef.gunfunapp.repos.TriviaRoundQuestionRepo;
import com.codef.gunfunapp.repos.TriviaRoundRepo;
import com.codef.gunfunapp.repos.ValidCaliberRepo;

@Controller
public class QuizController {

	@Autowired
	private RegistryRepo gunRegistryRepo;

	@Autowired
	private TriviaRoundRepo gunTriviaRoundsRepo;

	@Autowired
	private TriviaRoundQuestionRepo gunTriviaGameRepo;

	@Autowired
	private TriviaQuestionTemplateRepo gunTriviaTemplateQuestionsRepo;

	@Autowired
	private ValidCaliberRepo validCaliberRepo;

	@Autowired
	@Qualifier("jdbcMaster")
	private JdbcTemplate jdbcTemplateOne;

	@GetMapping("/quiz/start")
	public String quizStart(HttpServletRequest request, Model model) throws SQLException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		mu.addToModel("reportTitle", "Start Quiz");
		mu.addToModel("getAllGunNames", ControllerUtils.getAllGunNicknames(gunRegistryRepo));
		mu.addToModel("getAllMakes", ControllerUtils.getAllMakes(gunRegistryRepo));
		mu.addToModel("getAllModels", ControllerUtils.getAllModels(gunRegistryRepo));
		mu.addToModel("getAllCalibers", ControllerUtils.getAllCalibers(validCaliberRepo));

		String maxQuestions = SystemUtils.getStringValueFromTable(conn,
				"SELECT count(trivia_pk) AS MAX_QUESTIONS FROM trivia_question_templates", "MAX_QUESTIONS");
		mu.addToModel("maxQuestions", Integer.parseInt(maxQuestions));

		conn.close();

		mu.printJson();
		return "quiz_start";
	}

	@PostMapping("/quiz/question")
	public String quizQuestion(HttpServletRequest request,
			@ModelAttribute TriviaRoundQuestion gunTriviaRoundsQuestionsArg, Model model)
			throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		Long roundPk = null;
		Long questionPk = null;

		String roundPkAsString = request.getParameter("roundPk");
		if (roundPkAsString == null) {
			// new game
			String contestantName = request.getParameter("contestantName");
			long noOfQuestions = Long.parseLong(request.getParameter("noOfQuestions"));
			roundPk = createNewRound(jdbcTemplateOne, gunTriviaGameRepo, gunTriviaRoundsRepo,
					gunTriviaTemplateQuestionsRepo, contestantName, noOfQuestions);
		} else {
			// existing question
			roundPk = gunTriviaRoundsQuestionsArg.getRoundPk();
			questionPk = gunTriviaRoundsQuestionsArg.getQuestionPk();

			TriviaRoundQuestion gunTriviaRoundsQuestions = gunTriviaGameRepo.findById(questionPk).get();
			gunTriviaRoundsQuestions.setQuestionIsAnswered(true);
			gunTriviaRoundsQuestions.setUserResponse(gunTriviaRoundsQuestionsArg.getUserResponse());
			if (gunTriviaRoundsQuestions.getCorrectResponse().equals(gunTriviaRoundsQuestionsArg.getUserResponse())) {
				gunTriviaRoundsQuestions.setQuestionIsCorrect(true);
			} else {
				gunTriviaRoundsQuestions.setQuestionIsCorrect(false);
			}
			gunTriviaGameRepo.save(gunTriviaRoundsQuestions);
		}

		mu.addToModel("getAllGunNames", request.getParameter("getAllGunNames"));
		mu.addToModel("getAllMakes", request.getParameter("getAllMakes"));
		mu.addToModel("getAllModels", request.getParameter("getAllModels"));
		mu.addToModel("getAllCalibers", request.getParameter("getAllCalibers"));

		List<TriviaRoundQuestion> gunTriviaRoundsQuestions = gunTriviaGameRepo.findByRoundPk(roundPk);
		if (gunTriviaRoundsQuestions.size() > 0) {

			TriviaRoundQuestion question = gunTriviaRoundsQuestions.get(0);

			mu.addToModel("gunTriviaRoundsQuestions", question);

			if (question.getQuestionType().equals("MULTIPLE_CHOICE")) {
				String responses = question.getQuestionResponses();

				switch (responses) {
				case "ALL_GUNNAMES":
					responses = request.getParameter("getAllGunNames");
					break;
				case "ALL_MAKES":
					responses = request.getParameter("getAllMakes");
					break;
				case "ALL_MODELS":
					responses = request.getParameter("getAllModels");
					break;
				case "ALL_CALIBERS":
					responses = request.getParameter("getAllCalibers");
					break;
				}

				mu.addToModel("dropDownAnswers_ArrayListString",
						new ArrayList<String>(Arrays.asList(responses.split("\\|"))));
			}

			Connection conn = jdbcTemplateOne.getDataSource().getConnection();
			String totalQuestions = SystemUtils.getStringValueFromTable(conn,
					"SELECT COUNT(QUESTION_PK) as TOTAL_COUNT FROM trivia_round_questions WHERE ROUND_PK = " + roundPk,
					"TOTAL_COUNT");
			String totalQuestionsAnswered = SystemUtils.getStringValueFromTable(conn,
					"SELECT COUNT(QUESTION_PK) as TOTAL_COUNT_ANSWERED FROM trivia_round_questions WHERE QUESTION_IS_ANSWERED = true AND ROUND_PK = "
							+ roundPk,
					"TOTAL_COUNT_ANSWERED");
			mu.addToModel("reportTitle",
					"Quiz - Question " + (Long.valueOf(totalQuestionsAnswered) + 1) + " of " + totalQuestions);
			conn.close();

			return "quiz_question";

		} else {

			Connection conn = jdbcTemplateOne.getDataSource().getConnection();

			String totalQuestions = SystemUtils.getStringValueFromTable(conn,
					"SELECT COUNT(QUESTION_PK) as TOTAL_COUNT FROM trivia_round_questions WHERE ROUND_PK = " + roundPk,
					"TOTAL_COUNT");
			String totalQuestionsAnsweredCorrectly = SystemUtils.getStringValueFromTable(conn,
					"SELECT COUNT(QUESTION_PK) as TOTAL_COUNT_ANSWERED_CORRECTLY FROM trivia_round_questions WHERE QUESTION_IS_CORRECT = true AND ROUND_PK = "
							+ roundPk,
					"TOTAL_COUNT_ANSWERED_CORRECTLY");

			Double score = Double.parseDouble(totalQuestionsAnsweredCorrectly) * 100
					/ Double.parseDouble(totalQuestions);

			TriviaRound gunTriviaRounds = gunTriviaRoundsRepo.findById(roundPk).get();
			gunTriviaRounds.setRoundNoOfQuestionsCorrect(Long.parseLong(totalQuestionsAnsweredCorrectly));
			gunTriviaRounds.setRoundScore(BigDecimal.valueOf(score));
			gunTriviaRoundsRepo.save(gunTriviaRounds);

			String sql = "SELECT QUESTION_PK, QUESTION_IS_CORRECT, QUESTION, CORRECT_RESPONSE, USER_RESPONSE, IMAGE_LOCATION, NICKNAME "
					+ "FROM trivia_round_questions WHERE ROUND_PK = " + roundPk + " ORDER by QUESTION_PK";
			mu.addToModel("reviewAllQuestions_ArrayListHashMapStringString",
					SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sql));

			conn.close();

			mu.addToModel("reportTitle", "Quiz Complete!");
			mu.addToModel("score", score);

			mu.printJson();
			return "quiz_complete";

		}

	}
	
	public Long createNewRound(JdbcTemplate jdbcTemplateOne, TriviaRoundQuestionRepo gunTriviaGameRepo,
			TriviaRoundRepo gunTriviaRoundsRepo, TriviaQuestionTemplateRepo gunTriviaTemplateQuestionsRepo,
			String username, long no_of_questions) throws SQLException, IOException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		TriviaRound gunTriviaRounds = new TriviaRound();
		gunTriviaRounds.setRoundUser(username);
		gunTriviaRounds.setRoundNoOfQuestions(no_of_questions);
		gunTriviaRounds.setRoundPlayedDate(new Date(System.currentTimeMillis()));
		gunTriviaRoundsRepo.save(gunTriviaRounds);

		Long roundPk = gunTriviaRounds.getRoundPk();

		String sql = "SELECT trivia_pk FROM trivia_question_templates";
		ArrayList<HashMap<String, String>> totalQuestionsAvailable = SystemUtils.makeSQLAsArrayListHashMapPlain(conn,
				sql);

		HashSet<Long> randomIds = new HashSet<Long>();
		Random random = new Random();
		for (int i = 0; i < no_of_questions; i++) {
			int randomIndex = random.nextInt(totalQuestionsAvailable.size());
			HashMap<String, String> randomQuestion = totalQuestionsAvailable.get(randomIndex);
			randomIds.add(Long.parseLong(randomQuestion.get("TRIVIA_PK")));
			totalQuestionsAvailable.remove(randomIndex);
		}

		for (long singleId : randomIds) {
			try {
				TriviaQuestionTemplate gunTriviaTemplateQuestions = gunTriviaTemplateQuestionsRepo.findById(singleId)
						.get();

				TriviaRoundQuestion gunTriviaRoundsQuestions = new TriviaRoundQuestion();
				gunTriviaRoundsQuestions.setRoundPk(roundPk);
				gunTriviaRoundsQuestions.setQuestionType(gunTriviaTemplateQuestions.getQuestionType());
				gunTriviaRoundsQuestions.setQuestion(gunTriviaTemplateQuestions.getQuestion());
				gunTriviaRoundsQuestions.setQuestionResponses(gunTriviaTemplateQuestions.getQuestionResponses());
				gunTriviaRoundsQuestions.setCorrectResponse(gunTriviaTemplateQuestions.getCorrectResponse());
				gunTriviaRoundsQuestions.setImageLocation(gunTriviaTemplateQuestions.getImageLocation());
				gunTriviaRoundsQuestions.setNickname(gunTriviaTemplateQuestions.getNickname());
				gunTriviaRoundsQuestions.setQuestionIsAnswered(false);
				gunTriviaGameRepo.save(gunTriviaRoundsQuestions);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return roundPk;

	}

}
