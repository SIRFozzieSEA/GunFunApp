package com.codef.gunfunapp.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.FileTypeMap;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.codef.gunfunapp.other.ModelUtils;
import com.codef.gunfunapp.other.SystemUtils;

@Controller
public class MainController {

	@Autowired
	private Environment env;

	@Autowired
	@Qualifier("jdbcMaster")
	private JdbcTemplate jdbcTemplateOne;
	
	@Value("${spring.datasource.jdbcUrl}")
	private String jdbcPath;
	
	@Value("${spring.datasource.driverClassName}")
	private String jdbcDriver;
	
	@Value("${spring.datasource.username}")
	private String jdbcUsername;
	
	@Value("${spring.datasource.password}")
	private String jdbcPassword;
	
	@Value("${server.port}")
	private String serverPort;
	
	@Value("${spring.h2.console.path}")
	private String h2ConsolePath;
	
	

//	private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);

	/*
	 * Main App Windows
	 */

	// jdbc:h2:file:/E:\Documents\Personal\Gun Stuff\GunFunApp\_data\gunfunmvc
	// jdbc:h2:file:/C:\GunFunAppTest\_data\gunfunmvc

	@GetMapping("/")
	public String indexLaunch(Model model) throws SQLException, IOException {

		try {
			Connection conn = jdbcTemplateOne.getDataSource().getConnection();
			conn.close();
		} catch (SQLException e) {
			// Database cannot be connected to, most likely BitLocker
			e.printStackTrace();
			return "index_nc";
		}

//		LOGGER.trace("A TRACE Message");
//		LOGGER.debug("A DEBUG Message");
//		LOGGER.info("An INFO Message");
//		LOGGER.warn("A WARN Message");
//		LOGGER.error("An ERROR Message");

		return "index";

	}
	
	@GetMapping("/h2_login")
	public String h2Launch(Model model) throws URISyntaxException, IOException {
		
		String jsession = getJsessionFromLoginPage("http://localhost:" + serverPort + "/h2-console");
		model.addAttribute("action", "/h2-console/login.do?jsessionid=" + jsession);
		model.addAttribute("username", jdbcUsername);
		model.addAttribute("password", jdbcPassword);
		model.addAttribute("setting", "Generic H2 (Embedded)");
		model.addAttribute("driver", jdbcDriver);
		model.addAttribute("jdbcPath", jdbcPath);
		model.addAttribute("stylesheet", "http://localhost:" + serverPort + h2ConsolePath + "/stylesheet.css");
		return "h2_login";
	}

	@GetMapping("/frame_navigation")
	public String frameNavigation(Model model) {
		return "frame_navigation";
	}

	@GetMapping("/frame_main")
	public String frameMain(@RequestParam(name = "showStats", required = false) boolean showStats, Model model)
			throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		if (showStats) {

			Connection conn = jdbcTemplateOne.getDataSource().getConnection();

			String totalGuns = SystemUtils.getStringValueFromTable(conn,
					"SELECT count(GUN_PK) as TOTAL_COUNT FROM registry", "TOTAL_COUNT");

			if (Long.parseLong(totalGuns) > 0) {

				String maxCost = SystemUtils.getStringValueFromTable(conn,
						"SELECT MAX(purchase_cost) as MAX_COST FROM registry", "MAX_COST");
				mu.addToModel("maxCost", Double.parseDouble(maxCost));

				String totalCost = SystemUtils.getStringValueFromTable(conn,
						"SELECT SUM(purchase_cost) as TOTAL_COST FROM registry", "TOTAL_COST");
				mu.addToModel("totalCost", Double.parseDouble(totalCost));

				String totalMarketCost = SystemUtils.getStringValueFromTable(conn,
						"SELECT SUM(market_cost) as MARKET_COST FROM registry", "MARKET_COST");
				mu.addToModel("totalMarketCost", Double.parseDouble(totalMarketCost));

				mu.addToModel("totalDifferenceCost",
						Double.parseDouble(totalMarketCost) - Double.parseDouble(totalCost));

				String avgCost = SystemUtils.getStringValueFromTable(conn,
						"SELECT avg(purchase_cost) as AVG_COST FROM registry", "AVG_COST");
				mu.addToModel("avgCost", Double.parseDouble(avgCost));

				String minCost = SystemUtils.getStringValueFromTable(conn,
						"SELECT MIN(purchase_cost) as MIN_COST FROM registry", "MIN_COST");
				mu.addToModel("minCost", Double.parseDouble(minCost));

				String maxBarrel = SystemUtils.getStringValueFromTable(conn,
						"SELECT MAX(barrel_length) as MAX_BL FROM registry", "MAX_BL");
				mu.addToModel("maxBarrel", Double.parseDouble(maxBarrel));

				String avgBarrel = SystemUtils.getStringValueFromTable(conn,
						"SELECT avg(barrel_length) as AVG_BL FROM registry", "AVG_BL");
				mu.addToModel("avgBarrel", Double.parseDouble(avgBarrel));

				String minBarrel = SystemUtils.getStringValueFromTable(conn,
						"SELECT MIN(barrel_length) as MIN_BL FROM registry", "MIN_BL");
				mu.addToModel("minBarrel", Double.parseDouble(minBarrel));

				String sql = "SELECT MAKE, count(MAKE) as TOTAL_COUNT FROM registry GROUP BY MAKE ORDER BY MAKE";
				mu.addToModel("manufacturers_ArrayListHashMapStringString",
						SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sql));

				sql = "SELECT CALIBER, count(CALIBER) as TOTAL_COUNT FROM registry GROUP BY CALIBER ORDER BY CALIBER";
				mu.addToModel("calibers_ArrayListHashMapStringString",
						SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sql));

				sql = "SELECT FRAME_MATERIAL, count(FRAME_MATERIAL) as TOTAL_COUNT FROM registry GROUP BY FRAME_MATERIAL ORDER BY FRAME_MATERIAL";
				mu.addToModel("frameMaterials_ArrayListHashMapStringString",
						SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sql));

				sql = "SELECT GUN_TYPE, count(GUN_TYPE) as TOTAL_COUNT FROM registry GROUP BY GUN_TYPE ORDER BY GUN_TYPE";
				mu.addToModel("gunType_ArrayListHashMapStringString",
						SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sql));

				String firstDayCarry = SystemUtils.getStringValueFromTable(conn,
						"SELECT MIN(CARRIED_DATE) as MIN_CARRIED_DATE FROM CARRY_SESSIONS", "MIN_CARRIED_DATE");
				mu.addToModel("carry_firstDay", firstDayCarry);
				String totalPotentialCarryDays = SystemUtils.getStringValueFromTable(conn,
						"SELECT DATEDIFF('DAY', MIN(CARRIED_DATE), NOW()) AS TOTAL_DAYS FROM CARRY_SESSIONS",
						"TOTAL_DAYS");
				mu.addToModel("carry_totalPotentialDays", Long.parseLong(totalPotentialCarryDays));
				String totalDays = SystemUtils.getStringValueFromTable(conn,
						"SELECT COUNT(DISTINCT CARRIED_DATE) AS TOTAL_DAYS FROM CARRY_SESSIONS WHERE NICKNAME IS NOT NULL AND NICKNAME != ''",
						"TOTAL_DAYS");
				mu.addToModel("carry_totalDays", Long.parseLong(totalDays));
				mu.addToModel("carry_totalPercentage",
						Double.valueOf(Long.parseLong(totalDays) * 100 / Long.parseLong(totalPotentialCarryDays)));
				
				String firstDayRange = SystemUtils.getStringValueFromTable(conn,
						"SELECT MIN(FIRED_DATE) as MIN_FIRED_DATE FROM SHOOTING_SESSIONS", "MIN_FIRED_DATE");
				mu.addToModel("range_firstDay", firstDayRange);
				String totalPotentialRangeDays = SystemUtils.getStringValueFromTable(conn,
						"SELECT DATEDIFF('DAY', MIN(FIRED_DATE), NOW()) AS TOTAL_DAYS FROM SHOOTING_SESSIONS",
						"TOTAL_DAYS");
				mu.addToModel("range_totalPotentialDays", Long.parseLong(totalPotentialRangeDays));
				totalDays = SystemUtils.getStringValueFromTable(conn,
						"SELECT COUNT(DISTINCT FIRED_DATE) AS TOTAL_DAYS FROM SHOOTING_SESSIONS", "TOTAL_DAYS");
				mu.addToModel("range_totalDays", Long.parseLong(totalDays));
				mu.addToModel("range_totalPercentage",
						Double.valueOf(Long.parseLong(totalDays) * 100 / Long.parseLong(totalPotentialRangeDays)));

			}

			mu.addToModel("totalGuns", Long.parseLong(totalGuns));

			conn.close();

		} else {
			mu.addToModel("totalGuns", Long.parseLong("0"));
		}

		mu.printJson();
		return "frame_main";
	}

	@GetMapping("/getImage")
	public ResponseEntity<byte[]> getImage(@RequestParam(name = "imageName", required = true) String imageName)
			throws IOException {
		File img = new File(env.getProperty("GUNFUN_APP_FOLDER") + "\\_images\\" + imageName);
		return ResponseEntity.ok()
				.contentType(MediaType.valueOf(FileTypeMap.getDefaultFileTypeMap().getContentType(img)))
				.body(Files.readAllBytes(img.toPath()));
	}

	@GetMapping("/getPdf")
	public ResponseEntity<byte[]> getPdf(@RequestParam(name = "pdfName", required = true) String pdfName)
			throws IOException {

		File pdf = null;
		byte[] pdfFileBytes = null;

		String gunFunAppManualLocation = env.getProperty("GUNFUN_APP_FOLDER") + "\\_manuals\\";
		try {
			pdf = new File(gunFunAppManualLocation + pdfName);
			pdfFileBytes = Files.readAllBytes(pdf.toPath());
		} catch (Exception e) {
			pdf = new File(gunFunAppManualLocation + "_NOT_FOUND.pdf");
			pdfFileBytes = Files.readAllBytes(pdf.toPath());
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("application/pdf"));
		headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
		headers.add("content-disposition", "inline;filename=" + pdfName);

		return ResponseEntity.ok().headers(headers)
				.contentType(MediaType.valueOf(FileTypeMap.getDefaultFileTypeMap().getContentType(pdf)))
				.body(pdfFileBytes);

	}

	@GetMapping("/getImageForQuestion")
	public ResponseEntity<byte[]> getImageForQuestion(
			@RequestParam(name = "questionPk", required = true) String questionPk,
			@RequestParam(name = "size", required = true) String size) throws IOException, SQLException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		String nickname = SystemUtils.getStringValueFromTable(conn,
				"SELECT NICKNAME FROM TRIVIA_ROUND_QUESTIONS WHERE QUESTION_PK = " + questionPk, "NICKNAME");
		conn.close();

		File img = new File(
				env.getProperty("GUNFUN_APP_FOLDER") + "\\_images\\" + "\\" + size + "\\" + nickname + ".jpg");
		return ResponseEntity.ok()
				.contentType(MediaType.valueOf(FileTypeMap.getDefaultFileTypeMap().getContentType(img)))
				.body(Files.readAllBytes(img.toPath()));

	}
	
	private static String getJsessionFromLoginPage(String host) throws IOException, URISyntaxException {
		
		String stdLoginPage = SystemUtils.readStringFromURL(host);
        String regex = "jsessionid=([^']*)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(stdLoginPage);

        if (matcher.find()) {
            String jsessionid = matcher.group(1);
            return jsessionid;
        }
        
        return null;
		
	}

}
