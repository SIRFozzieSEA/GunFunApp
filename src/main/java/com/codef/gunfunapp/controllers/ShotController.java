package com.codef.gunfunapp.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
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

import com.codef.gunfunapp.models.entities.Registry;
import com.codef.gunfunapp.models.entities.ShootingSession;
import com.codef.gunfunapp.other.ControllerUtils;
import com.codef.gunfunapp.other.ModelUtils;
import com.codef.gunfunapp.other.SystemUtils;
import com.codef.gunfunapp.repos.RegistryRepo;
import com.codef.gunfunapp.repos.ShootingSessionRepo;
import com.codef.gunfunapp.repos.ValidCaliberRepo;

@Controller
public class ShotController {

	@Autowired
	private RegistryRepo gunRegistryRepo;

	@Autowired
	private ShootingSessionRepo gunShootingSessionsRepo;

	@Autowired
	private ValidCaliberRepo validCaliberRepo;

	@Autowired
	@Qualifier("jdbcMaster")
	private JdbcTemplate jdbcTemplateOne;

	@GetMapping("/report/shot")
	public String reportShot(HttpServletRequest request, Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		String orderBy = request.getParameter("orderBy") != null ? request.getParameter("orderBy")
				: "NICKNAME, CALIBER";
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		mu.addToModel("reportTitle", "Shot Report");
		String sql = "SELECT NICKNAME, CALIBER, MAX(FIRED_DATE) AS LAST_FIRED_DATE, sum(NO_OF_ROUNDS) AS TOTAL_ROUNDS_FIRED "
				+ "FROM shooting_sessions group by NICKNAME, CALIBER order by " + orderBy;
		mu.addToModel("report_ArrayListHashMapStringString", SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sql));
		sql = "SELECT SUM(NO_OF_ROUNDS) AS REPORT_TOTAL FROM shooting_sessions";
		mu.addToModel("reportTotal", SystemUtils.getStringValueFromTable(conn, sql, "REPORT_TOTAL"));
		conn.close();

		mu.printJson();
		return "report_shot";

	}

	@GetMapping("/report/shotbycaliber")
	public String reportShotByCaliber(HttpServletRequest request, Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		String orderBy = request.getParameter("orderBy") != null ? request.getParameter("orderBy") : "CALIBER";
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		mu.addToModel("reportTitle", "Shot Report by Caliber");
		String sql = "SELECT CALIBER, SUM(NO_OF_ROUNDS) AS TOTAL_ROUNDS_FIRED FROM shooting_sessions "
				+ "GROUP BY CALIBER ORDER BY " + orderBy;
		mu.addToModel("report_ArrayListHashMapStringString", SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sql));
		sql = "SELECT SUM(NO_OF_ROUNDS) AS REPORT_TOTAL FROM shooting_sessions";
		mu.addToModel("reportTotal", SystemUtils.getStringValueFromTable(conn, sql, "REPORT_TOTAL"));

		conn.close();

		mu.printJson();
		return "report_shot_by_caliber";

	}

	@GetMapping("/log/shot")
	public String logShot(HttpServletRequest request, Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		mu.addToModel("reportTitle", "Shot Log");

		String whereClause = request.getParameter("show") == null ? "WHERE FIRED_DATE > DATEADD('DAY', "
				+ ControllerUtils.getPreferenceLongValue(conn, "MAX_LOG_DAYS_SHOT") + ", CURRENT_DATE)" : "";

		String sql = "SELECT SHOOT_PK, NICKNAME, CALIBER, NO_OF_ROUNDS, FIRED_DATE FROM shooting_sessions "
				+ whereClause + " ORDER by FIRED_DATE DESC, NICKNAME";

		ArrayList<HashMap<String, String>> report = SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sql);
		mu.addToModel("report_ArrayListHashMapStringString", enhanceAhssByDate(report, "FIRED_DATE", "NO_OF_ROUNDS"));
		mu.addToModel("allCaliberSet_TreeSetString", ControllerUtils.getAllCaliberValues(validCaliberRepo));
		conn.close();

		mu.printJson();
		return "log_shot";
	}

	@PostMapping("/log/shot")
	public String logShotDelete(HttpServletRequest request, Model model) throws NumberFormatException, SQLException {

		ModelUtils mu = new ModelUtils(model);
		boolean editPerformed = false;
		Map<String, String[]> requestParameterMap = request.getParameterMap();
		for (Object key : requestParameterMap.keySet()) {
			String keyStr = (String) key;
			if (keyStr.startsWith("GUN_PK_ROUNDS_")) {
				String shootPkToEdit = keyStr.replaceAll("GUN_PK_ROUNDS_", "");

				if (Long.parseLong(request.getParameter("GUN_PK_ROUNDS_" + shootPkToEdit)) > 0) {
					ShootingSession gunShootingSession = gunShootingSessionsRepo.findById(Long.parseLong(shootPkToEdit))
							.get();
					gunShootingSession
							.setNoOfRounds(Long.parseLong(request.getParameter("GUN_PK_ROUNDS_" + shootPkToEdit)));
					gunShootingSession.setFiredDate(
							SystemUtils.parseDate(request.getParameter("GUN_PK_FIRED_DATE_" + shootPkToEdit)));
					if (request.getParameter("GUN_PK_CALIBER_" + shootPkToEdit).equals("")) {
						// look up default caliber by nickname
						Registry gunRegistryEntry = gunRegistryRepo.findByNickname(gunShootingSession.getNickname())
								.get();
						gunShootingSession.setCaliber(gunRegistryEntry.getCaliber());
					} else {
						gunShootingSession.setCaliber(request.getParameter("GUN_PK_CALIBER_" + shootPkToEdit));
					}
					gunShootingSessionsRepo.save(gunShootingSession);
					editPerformed = true;
				}

			}
		}

		boolean deletesPerformed = false;
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		if (request.getParameter("password").equals(ControllerUtils.getDeleteMasterPassword(conn))) {

			requestParameterMap = request.getParameterMap();
			for (Object key : requestParameterMap.keySet()) {
				String keyStr = (String) key;
				if (keyStr.startsWith("DELETE_SHOOT_PK_")) {
					String shootPkToDelete = keyStr.replaceAll("DELETE_SHOOT_PK_", "");
					gunShootingSessionsRepo.deleteById(Long.parseLong(shootPkToDelete));
					deletesPerformed = true;
				}
			}
		}

		if (editPerformed) {
			mu.addToModel("MESSAGE", "Edits to shot log have been made.");
		}

		if (deletesPerformed) {
			mu.addToModel("MESSAGETWO", "Entries removed from shot log.");
		}

		ControllerUtils.rebuildLastDates(conn, null, "LAST_FIRED_DATE");
		conn.close();

		mu.printJson();
		return "frame_main";
	}

	@GetMapping("/log/shot_add")
	public String logShotAdd(Model model) throws SQLException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		mu.addToModel("reportTitle", "Add Shot Entries");
		mu.addToModel("allGunNamesSet_TreeSetString", ControllerUtils.getAllGunNicknameValues(gunRegistryRepo));
		mu.addToModel("allCaliberSet_TreeSetString", ControllerUtils.getAllCaliberValues(validCaliberRepo));
		mu.addToModel("initialBlankEntries", Long.valueOf(10));
		mu.addToModel("todaysDate", new Date(System.currentTimeMillis()));
		conn.close();

		mu.printJson();
		return "log_shot_add";
	}

	@PostMapping("/log/shot_add")
	public String logShotAddFinal(HttpServletRequest request, Model model) throws SQLException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		Map<String, String[]> requestParameterMap = request.getParameterMap();
		for (Object key : requestParameterMap.keySet()) {
			String keyStr = (String) key;
			String[] valueArray = (String[]) requestParameterMap.get(keyStr);
			String possibleNicknameValue = valueArray[0];

			if (keyStr.startsWith("NICKNAME_") && !possibleNicknameValue.equals("")) {

				String formFieldKey = keyStr.replaceAll("NICKNAME_", "");
				String dateParameterName = "SHOT_DATE_" + formFieldKey;
				String noOfRoundsParameterName = "NO_OF_ROUNDS_" + formFieldKey;
				String caliberParameterName = "CALIBER_" + formFieldKey;

				ShootingSession shootingSession = new ShootingSession();
				shootingSession.setNickname(possibleNicknameValue);
				shootingSession.setNoOfRounds(Long.parseLong(request.getParameter(noOfRoundsParameterName)));
				shootingSession.setFiredDate(SystemUtils.parseDate(request.getParameter(dateParameterName)));

				if (request.getParameter(caliberParameterName).equals("")) {
					// look up default caliber by nickname
					Registry gunRegistryEntry = gunRegistryRepo.findByNickname(possibleNicknameValue).get();
					shootingSession.setCaliber(gunRegistryEntry.getCaliber());
				} else {
					shootingSession.setCaliber(request.getParameter(caliberParameterName));
				}

				gunShootingSessionsRepo.save(shootingSession);

				ControllerUtils.updateRegistryLastFiredDate(conn, possibleNicknameValue,
						request.getParameter(dateParameterName));

			}
		}

		conn.close();

		mu.addToModel("MESSAGE", "Entries added to shot log.");

		mu.printJson();
		return "frame_main";
	}

	// FUNCTIONS ----------------------------------

	private ArrayList<HashMap<String, String>> enhanceAhssByDate(ArrayList<HashMap<String, String>> originalAhss,
			String dateColumnName, String roundColumnName) {
		ArrayList<HashMap<String, String>> returnAhss = new ArrayList<HashMap<String, String>>();

		boolean firstPass = true;
		String currentDateValue = "";
		String lastDateValue = "";
		String currentRoundValue = "";
		String lastRoundValue = "";

		for (HashMap<String, String> currentRow : originalAhss) {
			currentDateValue = currentRow.get(dateColumnName);
			currentRoundValue = currentRow.get(roundColumnName);
			if (firstPass) {
				lastDateValue = currentDateValue;
				lastRoundValue = currentRoundValue;
				firstPass = false;
				returnAhss.add(currentRow);
			} else {

				if (currentDateValue.equals(lastDateValue)) {
					lastDateValue = currentDateValue;
					lastRoundValue = currentRoundValue;
					returnAhss.add(currentRow);
				} else {
					if (lastRoundValue.equals("0")) {
						returnAhss.add(getShowTotalRowLogShot(true));
					}
					returnAhss.add(getShowTotalRowLogShot(false));
					returnAhss.add(currentRow);
					lastDateValue = currentDateValue;
					lastRoundValue = currentRoundValue;

				}
			}
		}

		return returnAhss;
	}

	private HashMap<String, String> getShowTotalRowLogShot(boolean showTotal) {
		HashMap<String, String> returnHashMap = new HashMap<String, String>();
		returnHashMap.put("CALIBER", "");
		returnHashMap.put("SHOW_TOTAL", showTotal ? "YES" : "NO");
		returnHashMap.put("SHOOT_PK", "");
		returnHashMap.put("NICKNAME", "");
		returnHashMap.put("FIRED_DATE", "");
		returnHashMap.put("NO_OF_ROUNDS", "");
		return returnHashMap;
	}

}
