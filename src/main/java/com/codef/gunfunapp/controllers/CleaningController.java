package com.codef.gunfunapp.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.codef.gunfunapp.models.entities.CleaningSession;
import com.codef.gunfunapp.other.ControllerUtils;
import com.codef.gunfunapp.other.ModelUtils;
import com.codef.gunfunapp.other.SystemUtils;
import com.codef.gunfunapp.repos.CleaningSessionRepo;

@Controller
public class CleaningController {

	@Autowired
	private CleaningSessionRepo gunCleaningSessionsRepo;

	@Autowired
	@Qualifier("jdbcMaster")
	private JdbcTemplate jdbcTemplateOne;

	@GetMapping("/report/cleaning")
	public String reportCleaning(HttpServletRequest request, Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		String orderBy = request.getParameter("orderBy") != null ? request.getParameter("orderBy") : "NICKNAME";
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		ControllerUtils.rebuildCleaningReport(conn);

		mu.addToModel("reportTitle", "Cleaning Report");
		String sql = "SELECT NICKNAME, CALIBER, SUM(NO_OF_ROUNDS) as TOTAL_ROUNDS_FIRED, MAX(LAST_FIRED_DATE) AS LAST_FIRED_DATE,  "
				+ "MAX(LAST_CLEANED_DATE) AS LAST_CLEANED_DATE FROM cleaning_reports WHERE CALIBER != '' group by NICKNAME, "
				+ "CALIBER order by " + orderBy;
		mu.addToModel("report_ArrayListHashMapStringString", SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sql));

		long totalRoundFired = 0;
		ResultSet resultset = SystemUtils.querySQL(conn, sql);
		while (resultset.next()) {
			totalRoundFired = totalRoundFired + resultset.getInt("TOTAL_ROUNDS_FIRED");
		}
		mu.addToModel("reportTotal", Long.toString(totalRoundFired));

		conn.close();

		mu.printJson();
		return "report_cleaning";

	}

	@GetMapping("/log/cleaning")
	public String logCleaning(HttpServletRequest request, Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		mu.addToModel("reportTitle", "Cleaning Log");

		String whereClause = request.getParameter("show") == null
				? "WHERE CLEANED_DATE > DATEADD('DAY', "
						+ ControllerUtils.getPreferenceLongValue(conn, "MAX_LOG_DAYS_CLEANING") + ", CURRENT_DATE)"
				: "";

		String sql = "SELECT CLEAN_PK, NICKNAME, CLEANED_DATE FROM cleaning_sessions " + whereClause
				+ " ORDER by CLEANED_DATE DESC, NICKNAME";

		ArrayList<HashMap<String, String>> report = SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sql);
		mu.addToModel("report_ArrayListHashMapStringString", enhanceAhssByDate(report, "CLEANED_DATE"));
		conn.close();

		mu.printJson();
		return "log_cleaning";
	}

	@PostMapping("/log/cleaning")
	public String logCleaningDelete(HttpServletRequest request, Model model)
			throws NumberFormatException, SQLException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		if (request.getParameter("password").equals(ControllerUtils.getDeleteMasterPassword(conn))) {

			Map<String, String[]> requestParameterMap = request.getParameterMap();
			for (Object key : requestParameterMap.keySet()) {
				String keyStr = (String) key;
				if (keyStr.startsWith("DELETE_CLEAN_PK_")) {
					String cleanPkToDelete = keyStr.replaceAll("DELETE_CLEAN_PK_", "");
					gunCleaningSessionsRepo.deleteById(Long.parseLong(cleanPkToDelete));
				}
			}

			mu.addToModel("MESSAGE", "Entries removed from cleaning log.");

		}

		ControllerUtils.rebuildLastDates(conn, null, "LAST_CLEANED_DATE");
		conn.close();

		mu.printJson();
		return "frame_main";
	}

	@GetMapping("/log/cleaning_add")
	public String logCleaningAdd(HttpServletRequest request, Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		TreeSet<String> gunsToCleanSet = new TreeSet<String>();

		String sql = "SELECT NICKNAME AS DISPLAY_VALUE FROM cleaning_reports order by DISPLAY_VALUE;";
		gunsToCleanSet = SystemUtils.makeFirstSQLColumnTreeSet(conn, sql);
		mu.addToModel("initialBlankEntries", Long.valueOf(gunsToCleanSet.size()));
		mu.addToModel("reportTitle", "Add Cleaning Entries (Dirty)");

		if (request.getParameter("dirtyOnly").equals("false")) {
			sql = "SELECT DISTINCT NICKNAME AS DISPLAY_VALUE FROM registry order by DISPLAY_VALUE;";
			gunsToCleanSet = SystemUtils.makeFirstSQLColumnTreeSet(conn, sql);
			mu.addToModel("reportTitle", "Add Cleaning Entries (All)");
			mu.addToModel("initialBlankEntries", Long.valueOf(10));
		}

		mu.addToModel("allGunNamesSet_TreeSetString", gunsToCleanSet);
		mu.addToModel("todaysDate", new Date(System.currentTimeMillis()));

		conn.close();

		mu.printJson();
		return "log_cleaning_add";
	}

	@PostMapping("/log/cleaning_add")
	public String logCleaningAddFinal(HttpServletRequest request, Model model) throws SQLException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		Map<String, String[]> requestParameterMap = request.getParameterMap();
		for (Object key : requestParameterMap.keySet()) {
			String keyStr = (String) key;
			String[] valueArray = (String[]) requestParameterMap.get(keyStr);
			String possibleNicknameValue = valueArray[0];

			if (keyStr.startsWith("NICKNAME_") && !possibleNicknameValue.equals("")) {
				String formFieldKey = keyStr.replaceAll("NICKNAME_", "");
				String dateParameterName = "CLEANED_DATE_" + formFieldKey;
				gunCleaningSessionsRepo.save(new CleaningSession(possibleNicknameValue,
						SystemUtils.parseDate(request.getParameter(dateParameterName))));
				ControllerUtils.updateRegistryLastCleanedDate(conn, possibleNicknameValue,
						request.getParameter(dateParameterName));
			}
		}

		mu.addToModel("MESSAGE", "Entries added to cleaning log.");

		conn.close();

		mu.printJson();
		return "frame_main";
	}

	// FUNCTIONS ----------------------------------

	private ArrayList<HashMap<String, String>> enhanceAhssByDate(ArrayList<HashMap<String, String>> originalAhss,
			String dateColumnName) {
		ArrayList<HashMap<String, String>> returnAhss = new ArrayList<HashMap<String, String>>();

		boolean firstPass = true;
		String currentDateValue = "";
		String lastDateValue = "";

		for (HashMap<String, String> currentRow : originalAhss) {
			currentDateValue = currentRow.get(dateColumnName);
			if (firstPass) {
				lastDateValue = currentDateValue;
				firstPass = false;
				returnAhss.add(currentRow);
			} else {

				if (currentDateValue.equals(lastDateValue)) {
					lastDateValue = currentDateValue;
					returnAhss.add(currentRow);
				} else {
					returnAhss.add(getShowTotalRowLogCleaning(true));
					returnAhss.add(getShowTotalRowLogCleaning(false));
					returnAhss.add(currentRow);
					lastDateValue = currentDateValue;

				}
			}
		}

		returnAhss.add(getShowTotalRowLogCleaning(true));
		return returnAhss;
	}

	private HashMap<String, String> getShowTotalRowLogCleaning(boolean showTotal) {
		HashMap<String, String> returnHashMap = new HashMap<String, String>();
		returnHashMap.put("SHOW_TOTAL", showTotal ? "YES" : "NO");
		returnHashMap.put("CLEAN_PK", "");
		returnHashMap.put("NICKNAME", "");
		returnHashMap.put("CLEANED_DATE", "");
		return returnHashMap;
	}

}
