package com.codef.gunfunapp.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.codef.gunfunapp.models.entities.CarrySession;
import com.codef.gunfunapp.other.ControllerUtils;
import com.codef.gunfunapp.other.ModelUtils;
import com.codef.gunfunapp.other.SystemUtils;
import com.codef.gunfunapp.repos.CarrySessionRepo;
import com.codef.gunfunapp.repos.RegistryRepo;

@Controller
public class CarryController {

	@Autowired
	private RegistryRepo gunRegistryRepo;

	@Autowired
	private CarrySessionRepo gunCarrySessionsRepo;

	@Autowired
	@Qualifier("jdbcMaster")
	private JdbcTemplate jdbcTemplateOne;

	@GetMapping("/report/carry")
	public String reportCarry(HttpServletRequest request, Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		String orderBy = request.getParameter("orderBy") != null ? request.getParameter("orderBy")
				: "TOTAL_TIMES_CARRIED DESC";
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		mu.addToModel("reportTitle", "Carry Report");
		String sql = "SELECT carry_sessions.NICKNAME, CALIBER, COUNT(*) AS TOTAL_TIMES_CARRIED, MAX(CARRIED_DATE) "
				+ "AS LAST_CARRIED_DATE FROM carry_sessions INNER JOIN registry ON registry.NICKNAME = "
				+ "carry_sessions.NICKNAME GROUP BY  carry_sessions.NICKNAME, CALIBER ORDER BY " + orderBy;
		mu.addToModel("report_ArrayListHashMapStringString", SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sql));

		long totalCarryDays = 0;
		ResultSet resultset = SystemUtils.querySQL(conn, sql);
		while (resultset.next()) {
			totalCarryDays = totalCarryDays + resultset.getInt("TOTAL_TIMES_CARRIED");
		}
		mu.addToModel("reportTotal", Long.toString(totalCarryDays));

		conn.close();

		mu.printJson();
		return "report_carry";

	}

	@GetMapping("/log/carry")
	public String logCarry(HttpServletRequest request, Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		mu.addToModel("reportTitle", "Carry Log");

		String whereClause = request.getParameter("show") == null ? "WHERE CARRIED_DATE > DATEADD('DAY', "
				+ ControllerUtils.getPreferenceLongValue(conn, "MAX_LOG_DAYS_CARRY") + ", CURRENT_DATE)" : "";

		String sql = "SELECT CARRY_PK, NICKNAME, CARRIED_DATE, DAY_OF_WEEK FROM carry_sessions " + whereClause
				+ " ORDER by CARRIED_DATE DESC, NICKNAME";

		ArrayList<HashMap<String, String>> report = SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sql);
		mu.addToModel("report_ArrayListHashMapStringString", enhanceAhssByDate(report, "DAY_OF_WEEK", "Sunday"));
		conn.close();

		mu.printJson();
		return "log_carry";
	}

	@GetMapping("/log/carry_add")
	public String logCarryAdd(HttpServletRequest request, Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		mu.addToModel("reportTitle", "Add Carry Entries");
		mu.addToModel("allGunNamesSet_TreeSetString", ControllerUtils.getAllGunNicknameValues(gunRegistryRepo));

		int daysToDisplay = 16;
		long currentDateInMillis = System.currentTimeMillis();

		ArrayList<Date> weekDates = new ArrayList<Date>();
		ArrayList<String> dayDates = new ArrayList<String>();

		long eightDaysAgoInMillis = currentDateInMillis - (86400000 * 8);
		for (int i = 0; i < daysToDisplay; i++) {
			weekDates.add(new Date(eightDaysAgoInMillis + (86400000 * i)));
			dayDates.add(SystemUtils.getDayFromDate(new Date(eightDaysAgoInMillis + (86400000 * i))));
		}

		mu.addToModel("initialBlankEntries", Long.valueOf(daysToDisplay - 1));
		mu.addToModel("defaultEdc", ControllerUtils.getPreferenceStringValue(conn, "DEFAULT_EDC"));
		mu.addToModel("weekDates_ArrayListDate", weekDates);
		mu.addToModel("dayDates_ArrayListString", dayDates);
		mu.addToModel("todaysDate", new Date(currentDateInMillis));
		mu.addToModel("shutDownAfter", Boolean.valueOf(request.getParameter("SHUTDOWN_AFTER")));
		conn.close();

		mu.printJson();
		return "log_carry_add";
	}

	@PostMapping("/log/carry")
	public String logCarryDelete(HttpServletRequest request, Model model) throws SQLException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		if (request.getParameter("password").equals(ControllerUtils.getDeleteMasterPassword(conn))) {

			Map<String, String[]> requestParameterMap = request.getParameterMap();
			for (Object key : requestParameterMap.keySet()) {
				String keyStr = (String) key;
				if (keyStr.startsWith("DELETE_CARRY_PK_")) {
					String carryPkToDelete = keyStr.replaceAll("DELETE_CARRY_PK_", "");
					gunCarrySessionsRepo.deleteById(Long.parseLong(carryPkToDelete));
					String dateParameterName = "DELETE_CARRIED_DATE_" + carryPkToDelete;
					java.sql.Date dateCarried = SystemUtils.parseDate(request.getParameter(dateParameterName));
					boolean needBlanklines = checkBlankLinesNeeded(conn, "", dateCarried);
					if (needBlanklines) {
						gunCarrySessionsRepo
								.save(new CarrySession("", dateCarried, SystemUtils.getDayFromDate(dateCarried)));
					}
				}
			}

			mu.addToModel("MESSAGE", "Entries removed from carry log.");

		}

		ControllerUtils.rebuildLastDates(conn, null, "LAST_CARRIED_DATE");
		conn.close();

		mu.printJson();
		return "frame_main";
	}

	@PostMapping("/log/carry_add")
	public String logCarryAddFinal(HttpServletRequest request, Model model) throws SQLException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		Map<String, String[]> requestParameterMap = request.getParameterMap();
		for (Object key : requestParameterMap.keySet()) {
			String keyStr = (String) key;
			String[] valueArray = (String[]) requestParameterMap.get(keyStr);
			String possibleNicknameValue = valueArray[0];

			if (keyStr.startsWith("NICKNAME_")) {
				String formFieldKey = keyStr.replaceAll("NICKNAME_", "");
				String dateParameterName = "CARRIED_DATE_" + formFieldKey;
				java.sql.Date dateCarried = SystemUtils.parseDate(request.getParameter(dateParameterName));
				boolean needBlanklines = checkBlankLinesNeeded(conn, possibleNicknameValue, dateCarried);

				if (possibleNicknameValue.equals("")) {
					// empty entry
					if (needBlanklines) {
						gunCarrySessionsRepo
								.save(new CarrySession("", dateCarried, SystemUtils.getDayFromDate(dateCarried)));
					}
				} else {
					if (needBlanklines) {
						// make sure there's no duplicate for that day
						gunCarrySessionsRepo.save(new CarrySession(possibleNicknameValue, dateCarried,
								SystemUtils.getDayFromDate(dateCarried)));
						ControllerUtils.updateRegistryLastCarriedDate(conn, possibleNicknameValue,
								request.getParameter(dateParameterName));
					}
				}

			}
		}

		conn.close();

		mu.addToModel("MESSAGE", "Entries added to carry log.");

		boolean addAndShutDown = Boolean.valueOf(request.getParameter("SHUTDOWN_AFTER"));
		if (addAndShutDown) {
			try {
				System.out.println("Shutting down the PC after 5 seconds.");
				String commandArray[] = new String[] { "cmd.exe", "/c", "shutdown /s /t 5" };
				Runtime.getRuntime().exec(commandArray);
			} catch (IOException e) {
				System.out.println("Exception: " + e);
			}
		}

		mu.printJson();
		return "frame_main";
	}

	// FUNCTIONS -----------------------------

	// TODO: NEED TO CHECK THIS LOGIC HERE, BUT FOR CLEANING UP, YOU CAN USE:
	// DELETE FROM CARRY_SESSIONS WHERE NICKNAME is null and CARRIED_DATE in
	// (SELECT CARRIED_DATE FROM CARRY_SESSIONS WHERE NICKNAME is not null)

	private boolean checkBlankLinesNeeded(Connection conn, String nickname, java.sql.Date dateCarried)
			throws SQLException {

		if (nickname.equals("")) {
			// empty entry, check and see if empty entry exists before adding one
			String totalEmptyLines = SystemUtils.getStringValueFromTable(conn,
					"SELECT count(*) as TOTAL_COUNT FROM carry_sessions WHERE CARRIED_DATE = '" + dateCarried + "'",
					"TOTAL_COUNT");
			if (Long.parseLong(totalEmptyLines) > 0) {
				return false;
			} else {
				return true;
			}
		} else {

			String totalEmptyLines = SystemUtils.getStringValueFromTable(conn,
					"SELECT count(*) as TOTAL_COUNT FROM carry_sessions WHERE NICKNAME = '" + nickname
							+ "' AND CARRIED_DATE = '" + dateCarried + "'",
					"TOTAL_COUNT");
			if (Long.parseLong(totalEmptyLines) > 0) {
				return false;
			} else {
				String sql = "DELETE FROM carry_sessions WHERE (NICKNAME is NULL OR NICKNAME = '') AND CARRIED_DATE = '"
						+ dateCarried + "'";
				SystemUtils.executeSQL(conn, sql);
				return true;
			}
		}

	}

	private ArrayList<HashMap<String, String>> enhanceAhssByDate(ArrayList<HashMap<String, String>> originalAhss,
			String dayOfWeekColumnName, String dayOfWeekBreaksOn) {
		ArrayList<HashMap<String, String>> returnAhss = new ArrayList<HashMap<String, String>>();

		boolean firstPass = true;
		String currentDayValue = "";

		for (HashMap<String, String> currentRow : originalAhss) {
			currentDayValue = currentRow.get(dayOfWeekColumnName);
			if (firstPass) {
				firstPass = false;
				returnAhss.add(currentRow);
			} else {

				if (currentDayValue.equals(dayOfWeekBreaksOn)) {
					returnAhss.add(getBlankRowLogCarry());
				}
				returnAhss.add(currentRow);
			}
		}

		return returnAhss;
	}

	private HashMap<String, String> getBlankRowLogCarry() {
		HashMap<String, String> returnHashMap = new HashMap<String, String>();
		returnHashMap.put("CARRY_PK", "");
		returnHashMap.put("NICKNAME", "");
		returnHashMap.put("CARRIED_DATE", "");
		returnHashMap.put("DAY_OF_WEEK", "");
		return returnHashMap;
	}

}
