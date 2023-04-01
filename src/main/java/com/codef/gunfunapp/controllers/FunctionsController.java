package com.codef.gunfunapp.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.codef.gunfunapp.models.entities.Preference;
import com.codef.gunfunapp.models.entities.Registry;
import com.codef.gunfunapp.models.entities.ValidCaliber;
import com.codef.gunfunapp.other.BuildUtils;
import com.codef.gunfunapp.other.ControllerUtils;
import com.codef.gunfunapp.other.ModelUtils;
import com.codef.gunfunapp.other.SystemUtils;
import com.codef.gunfunapp.repos.PreferenceRepo;
import com.codef.gunfunapp.repos.RegistryRepo;
import com.codef.gunfunapp.repos.TriviaQuestionTemplateRepo;
import com.codef.gunfunapp.repos.ValidCaliberRepo;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class FunctionsController {

	@Autowired
	private Environment env;

	@Autowired
	private RegistryRepo gunRegistryRepo;

	@Autowired
	private TriviaQuestionTemplateRepo gunTriviaTemplateQuestionsRepo;

	@Autowired
	private PreferenceRepo preferenceRepo;

	@Autowired
	private ValidCaliberRepo validCaliberRepo;

	@Autowired
	@Qualifier("jdbcMaster")
	private JdbcTemplate jdbcTemplateOne;

	@GetMapping("/function/backup")
	public String functionBackup(HttpServletRequest request, Model model)
			throws ClassNotFoundException, SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		ArrayList<String> backedUpItems = new ArrayList<String>();

		String gunFunAppLocation = env.getProperty("GUNFUN_APP_FOLDER");
		if (request.getParameter("what").equals("ALL") || request.getParameter("what").equals("TAB")) {
			backedUpItems.add("Data");

			String backupFolderLocation = gunFunAppLocation + "\\_backup\\"
					+ new Date(System.currentTimeMillis()).toString() + " DATA TAB";

			File oDirectory = new File(backupFolderLocation);
			if (!oDirectory.exists()) {
				oDirectory.mkdirs();
			}

			String sql = "SHOW TABLES";
			ResultSet resultset = SystemUtils.querySQL(conn, sql);
			while (resultset.next()) {

				String currentTableName = resultset.getString("TABLE_NAME");
				ArrayList<String> columnNames = new ArrayList<String>();
				String pkColumn = "";

				ResultSet resultsettwo = SystemUtils.querySQL(conn, "show columns from " + currentTableName);
				while (resultsettwo.next()) {
					String fieldName = resultsettwo.getString("FIELD");
					String fieldKey = resultsettwo.getString("KEY");

					if (fieldKey.equals("PRI")) {
						pkColumn = fieldName;
					} else {
						columnNames.add(fieldName);
					}
				}

				Collections.sort(columnNames);

				SystemUtils.exportSQLAsTabDelimitedDataFile(conn,
						"SELECT " + pkColumn + ", " + columnNames.toString().replaceAll("\\[", "").replaceAll("\\]", "")
								+ " FROM " + currentTableName + " ORDER BY " + pkColumn,
						backupFolderLocation + "\\" + currentTableName + ".tab", true);

			}

			String backupFolderLocationTwo = gunFunAppLocation + "\\_backup\\"
					+ new Date(System.currentTimeMillis()).toString() + " data_tab.zip";

			oDirectory = new File(backupFolderLocationTwo);
			if (oDirectory.exists()) {
				oDirectory.delete();
			}

			SystemUtils.zipDirectory(backupFolderLocation, backupFolderLocationTwo);
			SystemUtils.deleteFolder(backupFolderLocation);

		}

		if (request.getParameter("what").equals("ALL") || request.getParameter("what").equals("IMAGES")) {
			backedUpItems.add("Images");

			String backupFolderLocation = gunFunAppLocation + "\\_backup\\"
					+ new Date(System.currentTimeMillis()).toString() + " images.zip";

			File oDirectory = new File(backupFolderLocation);
			if (oDirectory.exists()) {
				oDirectory.delete();
			}

			SystemUtils.zipDirectory(env.getProperty("GUNFUN_APP_FOLDER") + "\\_images\\", backupFolderLocation);

		}

		if (request.getParameter("what").equals("ALL") || request.getParameter("what").equals("MANUALS")) {
			backedUpItems.add("Manuals");

			String backupFolderLocation = gunFunAppLocation + "\\_backup\\"
					+ new Date(System.currentTimeMillis()).toString() + " manuals.zip";

			File oDirectory = new File(backupFolderLocation);
			if (oDirectory.exists()) {
				oDirectory.delete();
			}

			SystemUtils.zipDirectory(env.getProperty("GUNFUN_APP_FOLDER") + "\\_manuals\\", backupFolderLocation);

		}

		if (request.getParameter("what").equals("ALL") || request.getParameter("what").equals("PROPERTIES")) {
			backedUpItems.add("Properties");

			Properties properties = new Properties();
            FileInputStream fileInputStream = new FileInputStream("application.properties");
            properties.load(fileInputStream);
            fileInputStream.close();
			
			
			String backupFolderLocation = gunFunAppLocation + "\\_backup\\"
					+ new Date(System.currentTimeMillis()).toString() + " application.properties";

			File oDirectory = new File(backupFolderLocation);
			if (oDirectory.exists()) {
				oDirectory.delete();
			}

			SystemUtils.writeStringToFile(properties.toString(), backupFolderLocation);
		}

		if (request.getParameter("what").equals("ALL") || request.getParameter("what").equals("SQL")) {
			backedUpItems.add("SQL");

			String backupFolderLocation = gunFunAppLocation + "\\_backup\\"
					+ new Date(System.currentTimeMillis()).toString() + " data.sql";
			backupSql(conn, backupFolderLocation);
		}

		conn.close();

		String backedUpItemsDisplay = backedUpItems.toString().replaceAll("\\[", "").replaceAll("\\]", "");
		mu.addToModel("MESSAGE", "Items backed up: " + backedUpItemsDisplay);

		mu.printJson();
		return "frame_main";

	}

	@GetMapping("/function/maintenance")
	public String showMaintenanceFunctions(Model model) throws ClassNotFoundException, SQLException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		mu.addToModel("reportTitle", "Maintenance Functions");

		mu.addToModel("showBuildSampleAssets", !ControllerUtils.getPreferenceBooleanValue(conn, "SAMPLE_ASSETS_BUILT"));

		conn.close();
		mu.printJson();
		return "maintenance_main";

	}

	@GetMapping("/function/maintenance/rebuild_sample_data")
	public String functionRebuildSampleData(Model model) throws IOException, SQLException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		BuildUtils.buildSampleData(env.getProperty("GUNFUN_APP_FOLDER"));
		mu.addToModel("MESSAGE", "Built sample data");

		SystemUtils.executeSQL(null,
				"UPDATE PREFERENCES SET PREFERENCE_VALUE = 'true' WHERE PREFERENCE_KEY = 'SAMPLE_ASSETS_BUILT'");
		conn.close();

		mu.printJson();
		return "frame_main";

	}

	@GetMapping("/function/maintenance/rebuild_last_dates")
	public String functionRebuildLastDates(Model model) throws SQLException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		ControllerUtils.rebuildLastDates(conn, null, null);

		conn.close();

		mu.addToModel("MESSAGE", "Rebuilt last fired dates");

		mu.printJson();
		return "frame_main";

	}

	@GetMapping("/function/maintenance/manage_preferences")
	public String functionManageListPrefs(Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		mu.addToModel("reportTitle", "Manage Preferences");

		String sql = "SELECT PREFERENCE_PK, PREFERENCE_KEY, PREFERENCE_TYPE, PREFERENCE_VALUE FROM PREFERENCES ORDER BY PREFERENCE_KEY";
		mu.addToModel("report_ArrayListHashMapStringString", SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sql));

		TreeSet<String> options = new TreeSet<String>();
		options.add("Boolean");
		options.add("Double");
		options.add("Long");
		options.add("String");
		mu.addToModel("maintenanceTypes_TreeSetString", options);

		mu.addToModel("initialBlankEntries", Long.valueOf(3));

		conn.close();

		mu.printJson();
		return "maintenance_prefs";

	}

	@PostMapping("/function/maintenance/manage_preferences")
	public String functionManagePrefs(HttpServletRequest request, Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		Map<String, String[]> requestParameterMap = request.getParameterMap();
		for (Object key : requestParameterMap.keySet()) {
			String keyStr = (String) key;

			if (keyStr.startsWith("PREFERENCE_KEY_")) {

				String formFieldKey = keyStr.replaceAll("PREFERENCE_KEY_", "");
				String prefValueParameter = "PREFERENCE_VALUE_" + formFieldKey;
				String prefTypeParameter = "PREFERENCE_TYPE_" + formFieldKey;
				String prefDeleteParameter = "DELETE_PK_" + formFieldKey;

				if (request.getParameter(prefDeleteParameter) != null) {
					// delete it
					SystemUtils.executeSQL(conn, "DELETE FROM PREFERENCES WHERE PREFERENCE_PK = " + formFieldKey);
				} else {
					// edit it
					SystemUtils.executeSQL(conn,
							"UPDATE PREFERENCES SET PREFERENCE_KEY = '" + request.getParameter(keyStr)
									+ "', PREFERENCE_VALUE = '" + request.getParameter(prefValueParameter)
									+ "', PREFERENCE_TYPE = '" + request.getParameter(prefTypeParameter)
									+ "' WHERE PREFERENCE_PK = " + formFieldKey);

				}

			}

			if (keyStr.startsWith("NEW_PREFERENCE_KEY_")) {

				String formFieldKey = keyStr.replaceAll("NEW_PREFERENCE_KEY_", "");
				String prefValueParameter = "NEW_PREFERENCE_VALUE_" + formFieldKey;
				String prefTypeParameter = "NEW_PREFERENCE_TYPE_" + formFieldKey;

				// make sure it's not blank
				if (request.getParameter(prefTypeParameter) != null
						&& !request.getParameter(prefTypeParameter).equals("")) {
					Preference newPref = new Preference();
					newPref.setPreferenceKey(request.getParameter(keyStr));
					newPref.setPreferenceValue(request.getParameter(prefValueParameter));
					newPref.setPreferenceType(request.getParameter(prefTypeParameter));
					preferenceRepo.save(newPref);
				}

			}

		}

		conn.close();
		mu.addToModel("MESSAGE", "Preferences managed");

		mu.printJson();
		return "frame_main";

	}

	@GetMapping("/function/maintenance/manage_valid_calibers")
	public String functionManageListCalibers(Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		mu.addToModel("reportTitle", "Manage Valid Calibers");

		String sql = "SELECT VALID_CALIBER_PK, CALIBER, SHOOTS_CALIBER FROM VALID_CALIBERS ORDER BY CALIBER";
		mu.addToModel("report_ArrayListHashMapStringString", SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sql));

		mu.addToModel("initialBlankEntries", Long.valueOf(3));

		conn.close();

		mu.printJson();
		return "maintenance_calibers";

	}

	@PostMapping("/function/maintenance/manage_valid_calibers")
	public String functionManageCalibers(HttpServletRequest request, Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		Map<String, String[]> requestParameterMap = request.getParameterMap();
		for (Object key : requestParameterMap.keySet()) {
			String keyStr = (String) key;

			if (keyStr.startsWith("CALIBER_")) {

				String formFieldKey = keyStr.replaceAll("CALIBER_", "");
				String prefShootsCaliberParameter = "SHOOTS_CALIBER_" + formFieldKey;
				String prefDeleteParameter = "DELETE_PK_" + formFieldKey;

				if (request.getParameter(prefDeleteParameter) != null) {
					// delete it
					SystemUtils.executeSQL(conn, "DELETE FROM VALID_CALIBERS WHERE VALID_CALIBER_PK = " + formFieldKey);
				} else {
					// edit it
					SystemUtils.executeSQL(conn,
							"UPDATE VALID_CALIBERS SET CALIBER = '" + request.getParameter(keyStr)
									+ "', SHOOTS_CALIBER = '" + request.getParameter(prefShootsCaliberParameter)
									+ "' WHERE VALID_CALIBER_PK = " + formFieldKey);

				}

			}

			if (keyStr.startsWith("NEW_CALIBER_")) {

				String formFieldKey = keyStr.replaceAll("NEW_CALIBER_", "");
				String prefShootsCaliberParameter = "NEW_SHOOTS_CALIBER_" + formFieldKey;

				// make sure it's not blank
				if (request.getParameter(keyStr) != null && !request.getParameter(keyStr).equals("")) {
					ValidCaliber newCaliber = new ValidCaliber();
					newCaliber.setCaliber(request.getParameter(keyStr));
					newCaliber.setShootsCaliber(request.getParameter(prefShootsCaliberParameter));
					validCaliberRepo.save(newCaliber);
				}

			}

		}

		conn.close();
		mu.addToModel("MESSAGE", "Valid calibers managed");

		mu.printJson();
		return "frame_main";

	}

	@GetMapping("/function/maintenance/rebuild_questions")
	public String functionRebuildQuestions(Model model) throws SQLException {

		ModelUtils mu = new ModelUtils(model);
		mu.addToModel("MESSAGE", rebuildQuestions(jdbcTemplateOne, gunRegistryRepo, gunTriviaTemplateQuestionsRepo));

		mu.printJson();
		return "frame_main";

	}

	@GetMapping("/function/maintenance/rebuild_cleaning_report")
	public String functionProcessCleaningReport(Model model) throws SQLException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		mu.addToModel("MESSAGE", ControllerUtils.rebuildCleaningReport(conn));
		conn.close();

		mu.printJson();
		return "frame_main";

	}

	@GetMapping("/function/maintenance/audit_pdf_manuals")
	public String functionAuditPdfs(Model model) throws SQLException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		int totalGuns = Integer.parseInt(SystemUtils.getStringValueFromTable(conn,
				"SELECT count(GUN_PK) as TOTAL_COUNT FROM registry", "TOTAL_COUNT"));

		String gunFunAppManualLocation = env.getProperty("GUNFUN_APP_FOLDER") + "\\_manuals\\";
		File directory = new File(gunFunAppManualLocation);
		File[] files = directory.listFiles();
		int fileCount = files.length;

		mu.addToModel("MESSAGE", "PDF (of Manuals) Audit Complete");

		// there is a NOT_FOUND PDF in that directory, so this needs to be -1
		if ((fileCount - 1) == totalGuns) {
			mu.addToModel("MESSAGEDETAILS", "All manuals are accounted for.");
		} else {
			TreeSet<String> manualsFound = new TreeSet<String>();
			for (File file : files) {
				manualsFound.add(file.getName().split("\\.")[0]);
			}

			TreeSet<String> gunsInRegistry = ControllerUtils.getAllGunNicknameValues(gunRegistryRepo);
			gunsInRegistry.removeAll(manualsFound);
			mu.addToModel("MESSAGEDETAILS", "Missing Manuals: " + gunsInRegistry.toString());
		}

		conn.close();

		mu.printJson();
		return "frame_main";

	}

	// FUNCTIONS --------------------------------------------------------

	private void backupSql(Connection conn, String backupFolderLocation) throws SQLException, IOException {

		String tabcl = "\t";
		String crlf = "\n";
		String crlfcl = ";\n";
		String crlfcm = ",\n";

		StringBuffer trunacateBuffer = new StringBuffer();
		StringBuffer dropBuffer = new StringBuffer();
		StringBuffer createBuffer = new StringBuffer();
		StringBuffer dataBuffer = new StringBuffer();
		StringBuffer sequenceBuffer = new StringBuffer();

		String sql = "SHOW TABLES";
		ResultSet resultset = SystemUtils.querySQL(conn, sql);
		while (resultset.next()) {

			String currentTableName = resultset.getString("TABLE_NAME");
			ArrayList<String> columnNames = new ArrayList<String>();
			String pkColumn = "";
			long lastPk = 0;

			trunacateBuffer.append("TRUNCATE TABLE " + currentTableName + crlfcl);
			dropBuffer.append("DROP TABLE IF EXISTS " + currentTableName + crlfcl);

			createBuffer.append("CREATE TABLE `" + currentTableName + "` (" + crlf);
			ResultSet resultsettwo = SystemUtils.querySQL(conn, "show columns from " + currentTableName);
			while (resultsettwo.next()) {
				String fieldName = resultsettwo.getString("FIELD");
				String fieldType = resultsettwo.getString("TYPE");
				String fieldNull = resultsettwo.getString("NULL");
				String fieldKey = resultsettwo.getString("KEY");
				String fieldDefault = resultsettwo.getString("DEFAULT");
				columnNames.add(fieldName);

				String defaultValue = " default ''";
				if (fieldType.startsWith("BIGINT")) {
					defaultValue = " default '0'";
				} else if (fieldType.startsWith("BOOLEAN")) {
					defaultValue = " default 'N'";
				}

				if (fieldType.equals("DECIMAL(10)")) {
					fieldType = "DECIMAL(10, 2)";
				}

				createBuffer.append(
						tabcl + "`" + fieldName + "` " + fieldType + (fieldDefault.equals("NULL") ? defaultValue : "")
								+ (fieldNull.equals("NO") ? " NOT NULL " : "")
								+ (fieldKey.equals("PRI") ? "auto_increment PRIMARY KEY" : "") + crlfcm);
				if (fieldKey.equals("PRI")) {
					pkColumn = fieldName;
				}
			}

			Collections.sort(columnNames);

			String oldCreateBuffer = createBuffer.toString();
			createBuffer = new StringBuffer();
			createBuffer
					.append(oldCreateBuffer.substring(0, oldCreateBuffer.length() - 2) + crlf + ")" + crlfcl + crlf);

			StringBuffer dataBufferTemp = new StringBuffer();
			dataBufferTemp.append("INSERT INTO " + currentTableName + " ("
					+ columnNames.toString().replaceAll("\\[", "").replaceAll("\\]", "") + ") @VALUES ");

			boolean hasData = false;
			resultsettwo = SystemUtils.querySQL(conn, "select * from " + currentTableName + " order by " + pkColumn);
			while (resultsettwo.next()) {
				hasData = true;
				dataBufferTemp.append(", " + crlf + tabcl + "(");
				for (String singleColumn : columnNames) {
					String singleColumnValue = resultsettwo.getString(singleColumn) != null
							&& resultsettwo.getString(singleColumn).length() > 0
									? "'" + resultsettwo.getString(singleColumn).replaceAll("'", "''") + "', "
									: "null, ";
					dataBufferTemp.append(singleColumnValue);
				}
				String oldDataBuffer = dataBufferTemp.toString().replaceAll("@VALUES ,", "VALUES");
				dataBufferTemp = new StringBuffer();
				dataBufferTemp.append(oldDataBuffer.substring(0, oldDataBuffer.length() - 2) + ")");
				lastPk = resultsettwo.getLong(pkColumn);
			}
			lastPk = lastPk + 1;

			if (!currentTableName.equals("ROLES")) {
				sequenceBuffer.append("ALTER TABLE " + currentTableName + " ALTER COLUMN " + pkColumn + " RESTART WITH "
						+ lastPk + crlfcl);
			}

			if (hasData) {
				dataBuffer.append(dataBufferTemp);
				dataBuffer.append(crlfcl + crlf);
			}
		}

		trunacateBuffer.append(crlf);
		dropBuffer.append(crlf);

		StringBuffer writeBuffer = new StringBuffer();
		writeBuffer.append(trunacateBuffer);
		writeBuffer.append(dropBuffer);
		writeBuffer.append(createBuffer);
		writeBuffer.append(dataBuffer);
		writeBuffer.append(sequenceBuffer);

		SystemUtils.writeStringToFile(writeBuffer.toString(), backupFolderLocation);

	}

	private String rebuildQuestions(JdbcTemplate jdbcTemplateOne, RegistryRepo gunRegistryRepo,
			TriviaQuestionTemplateRepo gunTriviaTemplateQuestionsRepo) throws SQLException {

		// put custom questions
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		SystemUtils.executeSQL(conn, "TRUNCATE TABLE trivia_question_templates");
		SystemUtils.executeSQL(conn, "ALTER TABLE trivia_question_templates ALTER COLUMN TRIVIA_PK RESTART WITH 1");

		SystemUtils.executeSQL(conn, "TRUNCATE TABLE trivia_rounds");
		SystemUtils.executeSQL(conn, "TRUNCATE TABLE trivia_round_questions");

		SystemUtils.executeSQL(conn, "ALTER TABLE trivia_rounds ALTER COLUMN ROUND_PK RESTART WITH 1");
		SystemUtils.executeSQL(conn, "ALTER TABLE trivia_round_questions ALTER COLUMN QUESTION_PK RESTART WITH 1");

		// put standard questions
		List<Registry> gunRegistryEntries = (List<Registry>) gunRegistryRepo.findAll();
		ControllerUtils.addStandardQuestionsForGuns(gunTriviaTemplateQuestionsRepo, gunRegistryEntries);

		SystemUtils.executeSQL(conn, "INSERT INTO trivia_question_templates "
				+ "(QUESTION_TYPE, QUESTION, QUESTION_RESPONSES, CORRECT_RESPONSE, IMAGE_LOCATION, NICKNAME) "
				+ " (SELECT QUESTION_TYPE, QUESTION, QUESTION_RESPONSES, CORRECT_RESPONSE, IMAGE_LOCATION, NICKNAME FROM "
				+ " trivia_question_templates_custom) ");

		conn.close();

		return "Question rounds, scores and questions have been rebuilt.";
	}

}
