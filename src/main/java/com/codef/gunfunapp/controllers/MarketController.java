package com.codef.gunfunapp.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.codef.gunfunapp.other.ModelUtils;
import com.codef.gunfunapp.other.SystemUtils;

@Controller
public class MarketController {

	@Autowired
	@Qualifier("jdbcMaster")
	private JdbcTemplate jdbcTemplateOne;

	@GetMapping("/report/market_report")
	public String marketReport(Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		mu.addToModel("reportTitle", "Market Value Report");
		String sql = "SELECT GUN_PK, NICKNAME, MAKE, MODEL, PURCHASE_COST, MARKET_COST, MARKET_COST_DATE, "
				+ "MARKET_URL, (MARKET_COST - PURCHASE_COST) AS CURRENT_VALUE_CHANGE FROM registry order by NICKNAME";
		mu.addToModel("report_ArrayListHashMapStringString", SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sql));

		String sumPC = SystemUtils.getStringValueFromTable(conn, "SELECT SUM(purchase_cost) as SumPC FROM registry",
				"SumPC");
		mu.addToModel("SumPC", Double.parseDouble(sumPC));

		String sumMK = SystemUtils.getStringValueFromTable(conn, "SELECT SUM(market_cost) as SumMK FROM registry",
				"SumMK");
		mu.addToModel("SumMK", Double.parseDouble(sumMK));

		String sumDiff = SystemUtils.getStringValueFromTable(conn,
				"SELECT (SUM(market_cost) - SUM(purchase_cost)) as SumDIFF FROM registry", "SumDIFF");
		mu.addToModel("SumDIFF", Double.parseDouble(sumDiff));

		conn.close();

		mu.printJson();
		return "report_market";
	}

	@GetMapping("/market/market_update")
	public String marketUpdate(Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		mu.addToModel("reportTitle", "Update Market Values");
		String sql = "SELECT GUN_PK, NICKNAME, MAKE, MODEL, MARKET_COST, MARKET_COST_DATE, MARKET_URL FROM registry order by NICKNAME";
		mu.addToModel("report_ArrayListHashMapStringString", SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sql));
		conn.close();

		mu.printJson();
		return "market_update";
	}

	@PostMapping("/market/market_update")
	public String marketUpdateFinal(HttpServletRequest request, Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		Map<String, String[]> requestParameterMap = request.getParameterMap();
		for (Object key : requestParameterMap.keySet()) {
			String keyStr = (String) key;
			if (keyStr.startsWith("MARKET_COST_")) {
				String gunPkToUpdate = keyStr.replaceAll("MARKET_COST_", "");
				String gunCostToUpdate = request.getParameter("MARKET_COST_" + gunPkToUpdate).replace(",", "");
				String gunUrlToUpdate = request.getParameter("MARKET_URL_" + gunPkToUpdate);

				if (!gunUrlToUpdate.equals("")) {
					SystemUtils.executeSQL(conn,
							"UPDATE registry SET MARKET_COST = '" + Double.parseDouble(gunCostToUpdate)
									+ "', MARKET_URL = '" + gunUrlToUpdate + "', MARKET_COST_DATE = '"
									+ new Date(System.currentTimeMillis()) + "' WHERE GUN_PK = " + gunPkToUpdate);
				}

			}

		}

		mu.addToModel("MESSAGE", "Market values updated.");

		conn.close();

		mu.printJson();
		return "frame_main";
	}

}
