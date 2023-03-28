package com.codef.gunfunapp.other;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.TreeSet;

import com.codef.gunfunapp.models.entities.Registry;
import com.codef.gunfunapp.models.entities.TriviaQuestionTemplate;
import com.codef.gunfunapp.models.entities.ValidCaliber;
import com.codef.gunfunapp.repos.RegistryRepo;
import com.codef.gunfunapp.repos.TriviaQuestionTemplateRepo;
import com.codef.gunfunapp.repos.ValidCaliberRepo;

public class ControllerUtils {

	public static String getAllGunNicknames(RegistryRepo gunRegistryRepo) {

		TreeSet<String> allGunNickNamesSet = getAllGunNicknameValues(gunRegistryRepo);
		String allGunNames = allGunNickNamesSet.toString();
		allGunNames = allGunNames.substring(1, allGunNames.length() - 1).replace(", ", "|");
		return allGunNames;

	}

	public static TreeSet<String> getAllGunNicknameValues(RegistryRepo gunRegistryRepo) {

		TreeSet<String> allGunNames = new TreeSet<String>();

		List<Registry> gunRegistryEntries = (List<Registry>) gunRegistryRepo.findAll();
		for (Registry gunRegistry : gunRegistryEntries) {
			allGunNames.add(gunRegistry.getNickname());
		}

		return allGunNames;

	}

	public static String getAllMakes(RegistryRepo gunRegistryRepo) {

		TreeSet<String> allMakesSet = getAllMakesValues(gunRegistryRepo);

		String allMakes = allMakesSet.toString();
		allMakes = allMakes.substring(1, allMakes.length() - 1).replace(", ", "|");

		return allMakes;
	}

	public static TreeSet<String> getAllMakesValues(RegistryRepo gunRegistryRepo) {

		TreeSet<String> allMakesSet = new TreeSet<String>();

		List<Registry> gunRegistryEntries = (List<Registry>) gunRegistryRepo.findAll();
		for (Registry gunRegistry : gunRegistryEntries) {
			allMakesSet.add(gunRegistry.getMake());
		}

		return allMakesSet;

	}

	public static String getAllModels(RegistryRepo gunRegistryRepo) {

		TreeSet<String> allModelSet = getAllModelsValues(gunRegistryRepo);

		String allModels = allModelSet.toString();
		allModels = allModels.substring(1, allModels.length() - 1).replace(", ", "|");

		return allModels;
	}

	public static TreeSet<String> getAllModelsValues(RegistryRepo gunRegistryRepo) {

		TreeSet<String> allModelSet = new TreeSet<String>();

		List<Registry> gunRegistryEntries = (List<Registry>) gunRegistryRepo.findAll();
		for (Registry gunRegistry : gunRegistryEntries) {
			allModelSet.add(gunRegistry.getModel());
		}

		return allModelSet;
	}

	public static String getAllCalibers(ValidCaliberRepo validCaliberRepo) throws SQLException {

		TreeSet<String> allCaliberSet_TreeSetString = getAllCaliberValues(validCaliberRepo);

		String allCalibers = allCaliberSet_TreeSetString.toString();
		allCalibers = allCalibers.substring(1, allCalibers.length() - 1).replace(", ", "|");

		return allCalibers;
	}

	public static TreeSet<String> getAllCaliberValues(ValidCaliberRepo validCaliberRepo) throws SQLException {

		TreeSet<String> allModelSet = new TreeSet<String>();

		List<ValidCaliber> validCalibers = (List<ValidCaliber>) validCaliberRepo.findAll();
		for (ValidCaliber singleValidCaliber : validCalibers) {
			allModelSet.add(singleValidCaliber.getCaliber());
		}

		return allModelSet;

	}

	public static String getPreferenceStringValue(Connection conn, String prefName) throws SQLException {
		return SystemUtils.getStringValueFromTable(conn,
				"SELECT PREFERENCE_VALUE FROM PREFERENCES WHERE PREFERENCE_KEY = '" + prefName
						+ "' AND PREFERENCE_TYPE = 'String'",
				"PREFERENCE_VALUE");
	}

	public static boolean getPreferenceBooleanValue(Connection conn, String prefName) throws SQLException {
		return Boolean.parseBoolean(SystemUtils.getStringValueFromTable(conn,
				"SELECT PREFERENCE_VALUE FROM PREFERENCES WHERE PREFERENCE_KEY = '" + prefName
						+ "' AND PREFERENCE_TYPE = 'Boolean'",
				"PREFERENCE_VALUE"));
	}

	public static long getPreferenceLongValue(Connection conn, String prefName)
			throws NumberFormatException, SQLException {
		return Long.parseLong(SystemUtils.getStringValueFromTable(conn,
				"SELECT PREFERENCE_VALUE FROM PREFERENCES WHERE PREFERENCE_KEY = '" + prefName
						+ "' AND PREFERENCE_TYPE = 'Long'",
				"PREFERENCE_VALUE"));
	}

	public static double getPreferenceDoubleValue(Connection conn, String prefName)
			throws NumberFormatException, SQLException {
		return Double.parseDouble(SystemUtils.getStringValueFromTable(conn,
				"SELECT PREFERENCE_VALUE FROM PREFERENCES WHERE PREFERENCE_KEY = '" + prefName
						+ "' AND PREFERENCE_TYPE = 'Double'",
				"PREFERENCE_VALUE"));
	}

	public static String getDeleteMasterPassword(Connection conn) throws SQLException {
		return getPreferenceStringValue(conn, "DELETE_MASTER_PASSWORD");
	}

	public static void addStandardQuestionsForGuns(TriviaQuestionTemplateRepo gunTriviaTemplateQuestionsRepo,
			List<Registry> gunRegistryEntries) {

		for (Registry gunRegistry : gunRegistryEntries) {

			String gunName = gunRegistry.getNickname();

			TriviaQuestionTemplate gunTriviaTemplateQuestions = new TriviaQuestionTemplate();
			gunTriviaTemplateQuestions.setQuestionType("MULTIPLE_CHOICE");
			gunTriviaTemplateQuestions.setQuestion("What is this gun's nickname?");
			gunTriviaTemplateQuestions.setQuestionResponses("ALL_GUNNAMES");
			gunTriviaTemplateQuestions.setCorrectResponse(gunName);
			gunTriviaTemplateQuestions.setImageLocation("REGISTRY");
			gunTriviaTemplateQuestions.setNickname(gunName);
			gunTriviaTemplateQuestionsRepo.save(gunTriviaTemplateQuestions);

			gunTriviaTemplateQuestions = new TriviaQuestionTemplate();
			gunTriviaTemplateQuestions.setQuestionType("MULTIPLE_CHOICE");
			gunTriviaTemplateQuestions.setQuestion("What is this gun's manufacturer?");
			gunTriviaTemplateQuestions.setQuestionResponses("ALL_MAKES");
			gunTriviaTemplateQuestions.setCorrectResponse(gunRegistry.getMake());
			gunTriviaTemplateQuestions.setImageLocation("REGISTRY");
			gunTriviaTemplateQuestions.setNickname(gunName);
			gunTriviaTemplateQuestionsRepo.save(gunTriviaTemplateQuestions);

			gunTriviaTemplateQuestions = new TriviaQuestionTemplate();
			gunTriviaTemplateQuestions.setQuestionType("MULTIPLE_CHOICE");
			gunTriviaTemplateQuestions.setQuestion("What is this gun's model?");
			gunTriviaTemplateQuestions.setQuestionResponses("ALL_MODELS");
			gunTriviaTemplateQuestions.setCorrectResponse(gunRegistry.getModel());
			gunTriviaTemplateQuestions.setImageLocation("REGISTRY");
			gunTriviaTemplateQuestions.setNickname(gunName);
			gunTriviaTemplateQuestionsRepo.save(gunTriviaTemplateQuestions);

			gunTriviaTemplateQuestions = new TriviaQuestionTemplate();
			gunTriviaTemplateQuestions.setQuestionType("MULTIPLE_CHOICE");
			gunTriviaTemplateQuestions.setQuestion("What is this gun's caliber?");
			gunTriviaTemplateQuestions.setQuestionResponses("ALL_CALIBERS");
			gunTriviaTemplateQuestions.setCorrectResponse(gunRegistry.getCaliber());
			gunTriviaTemplateQuestions.setImageLocation("REGISTRY");
			gunTriviaTemplateQuestions.setNickname(gunName);
			gunTriviaTemplateQuestionsRepo.save(gunTriviaTemplateQuestions);

		}

	}

	public static String rebuildCleaningReport(Connection conn) throws SQLException {

		SystemUtils.executeSQL(conn, "TRUNCATE TABLE cleaning_reports");

		String sqlSelect = "SELECT r.NICKNAME, r.CALIBER, r.LAST_CLEANED_DATE, r.LAST_FIRED_DATE, SUM(s.NO_OF_ROUNDS) AS NO_OF_ROUNDS "
				+ " FROM REGISTRY r  INNER JOIN SHOOTING_SESSIONS s ON r.NICKNAME = s.NICKNAME "
				+ " WHERE r.LAST_CLEANED_DATE < s.FIRED_DATE GROUP BY r.NICKNAME, s.CALIBER";

		String sqlTwo = "INSERT INTO cleaning_reports (NICKNAME, CALIBER, LAST_CLEANED_DATE, LAST_FIRED_DATE, NO_OF_ROUNDS) "
				+ sqlSelect;
		SystemUtils.executeSQL(conn, sqlTwo);

		SystemUtils.executeSQL(conn, "UPDATE registry SET gun_is_dirty = false");
		SystemUtils.executeSQL(conn,
				"UPDATE registry SET gun_is_dirty = true WHERE NICKNAME IN (SELECT NICKNAME FROM cleaning_reports)");

		return "Cleaning report processed";

	}

	public static void rebuildLastDates(Connection conn, String gunNickname, String whichLast) throws SQLException {

		String whereClause = "";
		if (gunNickname != null) {
			whereClause = "WHERE NICKNAME = '" + gunNickname + "'";
		}

		ResultSet rs = null;

		if (whichLast == null || whichLast.equals("LAST_FIRED_DATE")) {
			// last fired date
			rs = SystemUtils.querySQL(conn, "SELECT NICKNAME, MAX(FIRED_DATE) AS MAX_FIRED FROM SHOOTING_SESSIONS "
					+ whereClause + " GROUP BY NICKNAME");
			while (rs.next()) {
				SystemUtils.executeSQL(conn, "UPDATE REGISTRY SET LAST_FIRED_DATE = '" + rs.getDate("MAX_FIRED")
						+ "' WHERE NICKNAME = '" + rs.getString("NICKNAME") + "'");
			}
		}

		if (whichLast == null || whichLast.equals("LAST_CLEANED_DATE")) {
			// last cleaned date
			rs = SystemUtils.querySQL(conn, "SELECT NICKNAME, MAX(CLEANED_DATE) AS MAX_CLEANED FROM CLEANING_SESSIONS "
					+ whereClause + " GROUP BY NICKNAME");
			while (rs.next()) {
				SystemUtils.executeSQL(conn, "UPDATE REGISTRY SET LAST_CLEANED_DATE = '" + rs.getDate("MAX_CLEANED")
						+ "' WHERE NICKNAME = '" + rs.getString("NICKNAME") + "'");
			}
		}

		if (whichLast == null || whichLast.equals("LAST_CARRIED_DATE")) {
			// last carried date
			rs = SystemUtils.querySQL(conn, "SELECT NICKNAME, MAX(CARRIED_DATE) AS MAX_CARRIED FROM CARRY_SESSIONS "
					+ whereClause + " GROUP BY NICKNAME");
			while (rs.next()) {
				SystemUtils.executeSQL(conn, "UPDATE REGISTRY SET LAST_CARRIED_DATE = '" + rs.getDate("MAX_CARRIED")
						+ "' WHERE NICKNAME = '" + rs.getString("NICKNAME") + "'");
			}
		}

	}

	public static void updateRegistryLastFiredDate(Connection conn, String nickname, String date) throws SQLException {
		SystemUtils.executeSQL(conn, "UPDATE REGISTRY SET LAST_FIRED_DATE = '" + date + "' WHERE NICKNAME = '"
				+ nickname + "' AND LAST_FIRED_DATE < '" + date + "'");
	}

	public static void updateRegistryLastCarriedDate(Connection conn, String nickname, String date)
			throws SQLException {
		SystemUtils.executeSQL(conn, "UPDATE REGISTRY SET LAST_CARRIED_DATE = '" + date + "' WHERE NICKNAME = '"
				+ nickname + "' AND LAST_CARRIED_DATE < '" + date + "'");
	}

	public static void updateRegistryLastCleanedDate(Connection conn, String nickname, String date)
			throws SQLException {
		SystemUtils.executeSQL(conn, "UPDATE REGISTRY SET LAST_CLEANED_DATE = '" + date + "' WHERE NICKNAME = '"
				+ nickname + "' AND LAST_CLEANED_DATE < '" + date + "'");
	}

}
