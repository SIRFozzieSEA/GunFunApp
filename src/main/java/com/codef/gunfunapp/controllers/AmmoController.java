package com.codef.gunfunapp.controllers;

import java.io.IOException;
import java.math.BigDecimal;
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

import com.codef.gunfunapp.models.entities.Ammunition;
import com.codef.gunfunapp.other.ControllerUtils;
import com.codef.gunfunapp.other.ModelUtils;
import com.codef.gunfunapp.other.SystemUtils;
import com.codef.gunfunapp.repos.AmmunitionRepo;
import com.codef.gunfunapp.repos.ValidCaliberRepo;

@Controller
public class AmmoController {

	@Autowired
	private AmmunitionRepo gunAmmoRepo;

	@Autowired
	private ValidCaliberRepo validCaliberRepo;

	@Autowired
	@Qualifier("jdbcMaster")
	private JdbcTemplate jdbcTemplateOne;

	@GetMapping("/report/ammo")
	public String reportAmmo(HttpServletRequest request, Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		doReportOperations(conn, false);
		String sqlTop = "SELECT CALIBER, SUM(NO_OF_ROUNDS) AS NO_OF_ROUNDS, MAX(LAST_PURCHASE_DATE) AS LAST_PURCHASE_DATE, MAX(LAST_PURCHASE_QUANTITY) AS LAST_PURCHASE_QUANTITY, MAX(LAST_PURCHASE_COST) AS LAST_PURCHASE_COST FROM AMMUNITION_REPORTS GROUP BY CALIBER";
		ArrayList<HashMap<String, String>> reportRows = SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sqlTop);

		mu.addToModel("report_ArrayListHashMapStringString", reportRows);
		mu.addToModel("reportTotal", SystemUtils.getStringAsLong(SystemUtils.getStringValueFromTable(conn,
				"SELECT SUM(NO_OF_ROUNDS) AS NO_OF_ROUNDS_SHOT FROM AMMUNITION_REPORTS", "NO_OF_ROUNDS_SHOT")));

		// ------------------------- Now let's do the ammo report since last ordered
		doReportOperations(conn, true);
		sqlTop = "SELECT CALIBER, -(SUM(NO_OF_ROUNDS)) AS NO_OF_ROUNDS, MAX(LAST_PURCHASE_DATE) AS LAST_PURCHASE_DATE, MAX(LAST_PURCHASE_QUANTITY) AS LAST_PURCHASE_QUANTITY, MAX(LAST_FIRED_DATE) AS LAST_FIRED_DATE FROM AMMUNITION_REPORTS GROUP BY CALIBER";
		ArrayList<HashMap<String, String>> reportRows2 = SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sqlTop);

		mu.addToModel("report2_ArrayListHashMapStringString", reportRows2);
		mu.addToModel("reportTotal2", SystemUtils.getStringAsLong(SystemUtils.getStringValueFromTable(conn,
				"SELECT -(SUM(NO_OF_ROUNDS)) AS NO_OF_ROUNDS_SHOT FROM AMMUNITION_REPORTS", "NO_OF_ROUNDS_SHOT")));

		conn.close();

		mu.printJson();
		return "report_ammo";
	}

	private void doReportOperations(Connection conn, boolean extraOp) throws SQLException {

		SystemUtils.executeSQL(conn, "TRUNCATE TABLE ammunition_reports");
		SystemUtils.executeSQL(conn, "ALTER TABLE ammunition_reports ALTER COLUMN REPORT_AMMO_PK RESTART WITH 1");

		if (extraOp) {
			SystemUtils.executeSQL(conn,
					"INSERT INTO AMMUNITION_REPORTS (CALIBER, NO_OF_ROUNDS, PURCHASE_QUANTITY, PURCHASE_COST, PURCHASE_DATE) "
							+ "SELECT CALIBER, '0', NO_OF_ROUNDS AS ROUNDS2, PURCHASE_COST, PURCHASE_DATE FROM AMMUNITION ORDER BY CALIBER, PURCHASE_DATE");
			SystemUtils.executeSQL(conn,
					"INSERT INTO AMMUNITION_REPORTS (CALIBER, NO_OF_ROUNDS, FIRED_QUANTITY, FIRED_DATE) "
							+ "SELECT CALIBER, -(SUM(NO_OF_ROUNDS)) AS ROUNDS1, SUM(NO_OF_ROUNDS) AS ROUNDS2, FIRED_DATE FROM SHOOTING_SESSIONS GROUP BY CALIBER, FIRED_DATE");
		} else {
			SystemUtils.executeSQL(conn,
					"INSERT INTO AMMUNITION_REPORTS (CALIBER, NO_OF_ROUNDS, PURCHASE_QUANTITY, PURCHASE_COST, PURCHASE_DATE) "
							+ "SELECT CALIBER, NO_OF_ROUNDS AS ROUNDS1, NO_OF_ROUNDS AS ROUNDS2, PURCHASE_COST, PURCHASE_DATE FROM AMMUNITION ORDER BY CALIBER, PURCHASE_DATE");
			SystemUtils.executeSQL(conn,
					"INSERT INTO AMMUNITION_REPORTS (CALIBER, NO_OF_ROUNDS, FIRED_QUANTITY, FIRED_DATE) "
							+ "SELECT CALIBER, -(SUM(NO_OF_ROUNDS)) AS ROUNDS1, SUM(NO_OF_ROUNDS) AS ROUNDS2, FIRED_DATE FROM SHOOTING_SESSIONS GROUP BY CALIBER, FIRED_DATE");
		}

		// get first purchase date for ammo and remove any shot logs before that date
		String lastCaliber = "";
		ResultSet rs = SystemUtils.querySQL(conn, "SELECT * FROM AMMUNITION ORDER BY CALIBER, PURCHASE_DATE");
		while (rs.next()) {
			if (!rs.getString("CALIBER").equals(lastCaliber)) {
				lastCaliber = rs.getString("CALIBER");
				// get the date, remove stuff
				SystemUtils.executeSQL(conn,
						"DELETE FROM ammunition_reports WHERE " + "NO_OF_ROUNDS < 0 AND CALIBER = '" + lastCaliber
								+ "' AND FIRED_DATE < '" + rs.getDate("PURCHASE_DATE") + "'");
			}
		}

		// get last info for ammo
		lastCaliber = "";
		rs = SystemUtils.querySQL(conn, "SELECT * FROM AMMUNITION ORDER BY CALIBER, PURCHASE_DATE DESC");
		while (rs.next()) {
			if (!rs.getString("CALIBER").equals(lastCaliber)) {
				lastCaliber = rs.getString("CALIBER");
				// update stuff
				SystemUtils.executeSQL(conn,
						"UPDATE ammunition_reports SET " + "LAST_PURCHASE_QUANTITY = '" + rs.getInt("NO_OF_ROUNDS")
								+ "', " + "LAST_PURCHASE_COST = '" + rs.getBigDecimal("PURCHASE_COST") + "', "
								+ "LAST_PURCHASE_DATE = '" + rs.getDate("PURCHASE_DATE") + "' WHERE " + "CALIBER = '"
								+ lastCaliber + "'");
			}
		}

		// get last info for shots
		lastCaliber = "";
		rs = SystemUtils.querySQL(conn, "SELECT * FROM SHOOTING_SESSIONS ORDER BY CALIBER, FIRED_DATE DESC");
		while (rs.next()) {
			if (!rs.getString("CALIBER").equals(lastCaliber)) {
				lastCaliber = rs.getString("CALIBER");
				// update stuff
				SystemUtils.executeSQL(conn,
						"UPDATE ammunition_reports SET " + "LAST_FIRED_QUANTITY = '" + rs.getInt("NO_OF_ROUNDS") + "', "
								+ "LAST_FIRED_DATE = '" + rs.getDate("FIRED_DATE") + "' WHERE " + "CALIBER = '"
								+ lastCaliber + "'");
			}
		}

		if (extraOp) {
			// get first last date for ammo and remove any ammo logs before that date
			lastCaliber = "";
			rs = SystemUtils.querySQL(conn, "SELECT * FROM AMMUNITION ORDER BY CALIBER, PURCHASE_DATE DESC");
			while (rs.next()) {
				if (!rs.getString("CALIBER").equals(lastCaliber)) {
					lastCaliber = rs.getString("CALIBER");
					// get the date, remove stuff
					SystemUtils.executeSQL(conn, "DELETE FROM ammunition_reports WHERE " + "CALIBER = '" + lastCaliber
							+ "' AND PURCHASE_DATE != '" + rs.getDate("PURCHASE_DATE") + "'");
					SystemUtils.executeSQL(conn,
							"DELETE FROM ammunition_reports WHERE " + "NO_OF_ROUNDS < 0 AND CALIBER = '" + lastCaliber
									+ "' AND FIRED_DATE < '" + rs.getDate("PURCHASE_DATE") + "'");
				}
			}
		}

	}

	@GetMapping("/log/ammo")
	public String logAmmo(HttpServletRequest request, Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		mu.addToModel("reportTitle", "Ammo Log");

		String whereClause = request.getParameter("show") == null
				? "WHERE PURCHASE_DATE > DATEADD('DAY', "
						+ ControllerUtils.getPreferenceLongValue(conn, "MAX_LOG_DAYS_PURCHASED") + ", CURRENT_DATE)"
				: "";

		String sql = "SELECT AMMO_PK, CALIBER, NO_OF_ROUNDS, PURCHASE_COST, PURCHASE_DATE FROM ammunition "
				+ whereClause + " ORDER by PURCHASE_DATE DESC, CALIBER";

		ArrayList<HashMap<String, String>> report = SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sql);
		mu.addToModel("report_ArrayListHashMapStringString", enhanceAhssByDate(report, "PURCHASE_DATE"));
		conn.close();

		mu.printJson();
		return "log_ammo";
	}

	@PostMapping("/log/ammo")
	public String logAmmoDelete(HttpServletRequest request, Model model) throws NumberFormatException, SQLException {

		ModelUtils mu = new ModelUtils(model);
		Map<String, String[]> requestParameterMap = request.getParameterMap();
		boolean deletesPerformed = false;
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		if (request.getParameter("password").equals(ControllerUtils.getDeleteMasterPassword(conn))) {

			requestParameterMap = request.getParameterMap();
			for (Object key : requestParameterMap.keySet()) {
				String keyStr = (String) key;
				if (keyStr.startsWith("DELETE_AMMO_PK_")) {
					String ammoPkToDelete = keyStr.replaceAll("DELETE_AMMO_PK_", "");
					gunAmmoRepo.deleteById(Long.parseLong(ammoPkToDelete));
					deletesPerformed = true;
				}
			}
		}

		if (deletesPerformed) {
			mu.addToModel("MESSAGETWO", "Entries removed from ammo log.");
		}

		conn.close();

		mu.printJson();
		return "frame_main";
	}

	@GetMapping("/log/ammo_add")
	public String logAmmoAdd(Model model) throws SQLException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		mu.addToModel("reportTitle", "Add Ammo Entries");
		mu.addToModel("allCaliberSet_TreeSetString", ControllerUtils.getAllCaliberValues(validCaliberRepo));
		mu.addToModel("initialBlankEntries", Long.valueOf(10));
		mu.addToModel("todaysDate", new Date(System.currentTimeMillis()));
		conn.close();

		mu.printJson();
		return "log_ammo_add";
	}

	@PostMapping("/log/ammo_add")
	public String logAmmoAddFinal(HttpServletRequest request, Model model) throws SQLException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		Map<String, String[]> requestParameterMap = request.getParameterMap();
		for (Object key : requestParameterMap.keySet()) {
			String keyStr = (String) key;
			String[] valueArray = (String[]) requestParameterMap.get(keyStr);
			String possibleCaliberValue = valueArray[0];

			if (keyStr.startsWith("CALIBER_") && !possibleCaliberValue.equals("")) {

				String formFieldKey = keyStr.replaceAll("CALIBER_", "");
				String dateParameterName = "PURCHASE_DATE_" + formFieldKey;
				String noOfRoundsParameterName = "NO_OF_ROUNDS_" + formFieldKey;
				String caliberParameterName = "CALIBER_" + formFieldKey;
				String purchaseCost = "PURCHASE_COST_" + formFieldKey;

				Ammunition ammoBuy = new Ammunition();
				ammoBuy.setCaliber(request.getParameter(caliberParameterName));
				ammoBuy.setNoOfRounds(Long.parseLong(request.getParameter(noOfRoundsParameterName)));
				ammoBuy.setPurchaseDate(SystemUtils.parseDate(request.getParameter(dateParameterName)));
				ammoBuy.setPurchaseCost(BigDecimal.valueOf(Double.parseDouble(request.getParameter(purchaseCost))));
				gunAmmoRepo.save(ammoBuy);

			}
		}

		conn.close();

		mu.addToModel("MESSAGE", "Entries added to ammo log.");

		mu.printJson();
		return "frame_main";
	}

	// FUNCTIONS -----------------------------

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
					returnAhss.add(getShowTotalRowLogAmmo(true));
					returnAhss.add(getShowTotalRowLogAmmo(false));
					returnAhss.add(currentRow);
					lastDateValue = currentDateValue;

				}
			}
		}

		returnAhss.add(getShowTotalRowLogAmmo(true));
		return returnAhss;
	}

	private HashMap<String, String> getShowTotalRowLogAmmo(boolean showTotal) {
		HashMap<String, String> returnHashMap = new HashMap<String, String>();
		returnHashMap.put("SHOW_TOTAL", showTotal ? "YES" : "NO");
		returnHashMap.put("AMMO_PK", "");
		returnHashMap.put("CALIBER", "");
		returnHashMap.put("NO_OF_ROUNDS", "");
		returnHashMap.put("PURCHASE_DATE", "");
		return returnHashMap;
	}

}
