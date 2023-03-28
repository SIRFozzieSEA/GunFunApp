package com.codef.gunfunapp.controllers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.codef.gunfunapp.models.entities.CleaningSession;
import com.codef.gunfunapp.models.entities.Registry;
import com.codef.gunfunapp.models.entities.ShootingSession;
import com.codef.gunfunapp.other.ControllerUtils;
import com.codef.gunfunapp.other.ModelUtils;
import com.codef.gunfunapp.other.SystemUtils;
import com.codef.gunfunapp.repos.CleaningSessionRepo;
import com.codef.gunfunapp.repos.RegistryRepo;
import com.codef.gunfunapp.repos.ShootingSessionRepo;
import com.codef.gunfunapp.repos.TriviaQuestionTemplateRepo;
import com.codef.gunfunapp.repos.ValidCaliberRepo;

@Controller
public class RegistryController {

	@Autowired
	private Environment env;

	@Autowired
	private RegistryRepo gunRegistryRepo;

	@Autowired
	private CleaningSessionRepo gunCleaningSessionsRepo;

	@Autowired
	private ShootingSessionRepo gunShootingSessionsRepo;

	@Autowired
	private TriviaQuestionTemplateRepo gunTriviaTemplateQuestionsRepo;

	@Autowired
	private ValidCaliberRepo validCaliberRepo;

	@Autowired
	@Qualifier("jdbcMaster")
	private JdbcTemplate jdbcTemplateOne;

	@GetMapping("/registry")
	public String registry(Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		String sql = "SELECT GUN_PK, NICKNAME, MAKE, MODEL, CALIBER, GUN_IS_DIRTY FROM registry order by NICKNAME";

		ArrayList<HashMap<String, String>> gunSearch = SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sql);
		ArrayList<ArrayList<HashMap<String, String>>> allGuns = getGunsInTwoColumn(gunSearch);

		String totalGuns = SystemUtils.getStringValueFromTable(conn,
				"SELECT count(GUN_PK) as TOTAL_COUNT FROM registry", "TOTAL_COUNT");

		mu.addToModel("gunsFound", Integer.valueOf(totalGuns));
		mu.addToModel("report_ArrayListArrayListHashMapStringString", allGuns);
		mu.addToModel("reportTitle", "All Registry Entries");
		mu.addToModel("searchStats", totalGuns + " Entries Found");

		mu.addToModel("todaysDate", new Date(System.currentTimeMillis()));
		conn.close();

		mu.printJson();
		return "registry_all";
	}

	@PostMapping("/range/packsheet")
	public String rangePackSheetFinal(HttpServletRequest request, Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		ArrayList<Long> gunsToPackList = new ArrayList<Long>();

		Map<String, String[]> requestParameterMap = request.getParameterMap();
		for (Object key : requestParameterMap.keySet()) {
			String keyStr = (String) key;
			if (keyStr.startsWith("GUN_PK_") && !keyStr.startsWith("GUN_PK_NICKNAME_")) {
				String gunPkToPack = keyStr.replaceAll("GUN_PK_", "");
				gunsToPackList.add(Long.parseLong(gunPkToPack));

				// lets make 0 entries on shot log to be edited
				ShootingSession shootingSession = new ShootingSession();
				shootingSession.setNickname(request.getParameter("GUN_PK_NICKNAME_" + gunPkToPack));
				shootingSession.setNoOfRounds(Long.parseLong("0"));
				shootingSession.setCaliber("");
				shootingSession.setFiredDate(SystemUtils.parseDate(request.getParameter("rangeDate")));
				gunShootingSessionsRepo.save(shootingSession);

			}
		}

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		mu.addToModel("reportTitle", "Pack List");
		String sql = "SELECT GUN_PK, NICKNAME, MAKE, MODEL, CALIBER FROM registry WHERE GUN_PK IN "
				+ gunsToPackList.toString().replace('[', '(').replace(']', ')') + " ORDER by CALIBER, NICKNAME";
		mu.addToModel("report_ArrayListHashMapStringString", SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sql));
		conn.close();

		mu.printJson();
		return "report_packsheet";
	}

	@GetMapping("/registry_search")
	public String registrySearch(Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		mu.addToModel("reportTitle", "Search Registry Entries");
		mu.addToModel("allMakesSet_TreeSetString", ControllerUtils.getAllMakesValues(gunRegistryRepo));
		mu.addToModel("allCaliberSet_TreeSetString", ControllerUtils.getAllCaliberValues(validCaliberRepo));
		conn.close();

		mu.printJson();
		return "registry_search";
	}

	@PostMapping("/registry_search")
	public String registrySearchFinal(HttpServletRequest request, Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		ArrayList<String> subQueries = new ArrayList<String>();
		TreeMap<Integer, String> orderBys = new TreeMap<Integer, String>();

		String gunNickNameValue = request.getParameter("Nickname");
		String gunNickNameOrAnd = request.getParameter("NicknameRadio");
		String gunNickNameOrder = request.getParameter("NicknameOrder");
		if (!gunNickNameValue.equals("")) {
			subQueries.add("(LOWER(NICKNAME) like '%" + gunNickNameValue.toLowerCase() + "%') " + gunNickNameOrAnd);
		}
		if (gunNickNameOrder != null && !gunNickNameOrder.equals("")) {
			orderBys.put(Integer.valueOf(gunNickNameOrder), "NICKNAME");
		}

		String gunMakeValue = request.getParameter("MakeDrop");
		String gunMakeOrAnd = request.getParameter("MakeRadio");
		String gunMakeOrder = request.getParameter("MakeOrder");
		if (!gunMakeValue.equals("")) {
			subQueries.add("(MAKE = '" + gunMakeValue + "') " + gunMakeOrAnd);
		}
		if (gunMakeOrder != null && !gunMakeOrder.equals("")) {
			orderBys.put(Integer.valueOf(gunMakeOrder), "MAKE");
		}

		String gunModelValue = request.getParameter("Model");
		String gunModelOrAnd = request.getParameter("ModelRadio");
		String gunModelOrder = request.getParameter("ModelOrder");
		if (!gunModelValue.equals("")) {
			subQueries.add("(LOWER(MODEL) like '%" + gunModelValue.toLowerCase() + "%') " + gunModelOrAnd);
		}
		if (gunModelOrder != null && !gunModelOrder.equals("")) {
			orderBys.put(Integer.valueOf(gunModelOrder), "MODEL");
		}

		String gunCaliberValue = request.getParameter("CaliberDrop");
		String gunCaliberOrAnd = request.getParameter("CaliberRadio");
		String gunCaliberOrder = request.getParameter("CaliberOrder");
		if (!gunCaliberValue.equals("")) {
			subQueries.add("(CALIBER = '" + gunCaliberValue + "') " + gunCaliberOrAnd);
		}
		if (gunCaliberOrder != null && !gunCaliberOrder.equals("")) {
			orderBys.put(Integer.valueOf(gunCaliberOrder), "CALIBER");
		}

		String gunSerialValue = request.getParameter("SerialNo");
		String gunSerialOrAnd = request.getParameter("SerialRadio");
		String gunSerialOrder = request.getParameter("SerialOrder");
		if (!gunSerialValue.equals("")) {
			subQueries.add("(LOWER(SERIAL) like '%" + gunSerialValue.toLowerCase() + "%') " + gunSerialOrAnd);
		}
		if (gunSerialOrder != null && !gunSerialOrder.equals("")) {
			orderBys.put(Integer.valueOf(gunSerialOrder), "SERIAL");
		}

		String gunBarrelLengthMinValue = request.getParameter("BarrelLengthMin");
		String gunBarrelLengthMaxValue = request.getParameter("BarrelLengthMax");
		String gunBarrelLengthOrAnd = request.getParameter("BarrelLengthRadio");
		String gunBarrelLengthOrder = request.getParameter("BarrelLengthOrder");
		if ((gunBarrelLengthMinValue != null && gunBarrelLengthMaxValue != null)
				&& (!gunBarrelLengthMinValue.equals("") && !gunBarrelLengthMaxValue.equals(""))) {
			subQueries.add("(BARREL_LENGTH BETWEEN '" + gunBarrelLengthMinValue + "' AND '" + gunBarrelLengthMaxValue
					+ "') " + gunBarrelLengthOrAnd);
		}
		if (gunBarrelLengthOrder != null && !gunBarrelLengthOrder.equals("")) {
			orderBys.put(Integer.valueOf(gunBarrelLengthOrder), "BARREL_LENGTH");
		}

		String gunPurchaseCostMinValue = request.getParameter("PurchaseCostMin");
		String gunPurchaseCostMaxValue = request.getParameter("PurchaseCostMax");
		String gunPurchaseCostOrAnd = request.getParameter("PurchaseCostRadio");
		String gunPurchaseCostOrder = request.getParameter("PurchaseCostOrder");
		if ((gunPurchaseCostMinValue != null && gunPurchaseCostMaxValue != null)
				&& (!gunPurchaseCostMinValue.equals("") && !gunPurchaseCostMaxValue.equals(""))) {
			subQueries.add("(PURCHASE_COST BETWEEN '" + gunPurchaseCostMinValue + "' AND '" + gunPurchaseCostMaxValue
					+ "') " + gunPurchaseCostOrAnd);
		}
		if (gunPurchaseCostOrder != null && !gunPurchaseCostOrder.equals("")) {
			orderBys.put(Integer.valueOf(gunPurchaseCostOrder), "PURCHASE_COST");
		}

		String gunPurchaseDateMinValue = request.getParameter("PurchaseDateMin");
		String gunPurchaseDateMaxValue = request.getParameter("PurchaseDateMax");
		String gunPurchaseDateOrAnd = request.getParameter("PurchaseDateRadio");
		String gunPurchaseDateOrder = request.getParameter("PurchaseDateOrder");
		if ((gunPurchaseDateMinValue != null && gunPurchaseDateMaxValue != null)
				&& (!gunPurchaseDateMinValue.equals("") && !gunPurchaseDateMaxValue.equals(""))) {
			subQueries.add("(PURCHASE_DATE BETWEEN '" + gunPurchaseDateMinValue + "' AND '" + gunPurchaseDateMaxValue
					+ "') " + gunPurchaseDateOrAnd);
		}
		if (gunPurchaseDateOrder != null && !gunPurchaseDateOrder.equals("")) {
			orderBys.put(Integer.valueOf(gunPurchaseDateOrder), "PURCHASE_DATE");
		}

		String dirtyClause = "";
		if (request.getParameter("ShowDirtyOnly") != null) {
			dirtyClause = " AND GUN_IS_DIRTY = true";
		}

		String whereClause = " 1=1 ";
		if (subQueries.size() > 0) {
			whereClause = subQueries.toString().replaceAll(",", "").replaceAll("\\[", "").replaceAll("\\]", "");
			whereClause = whereClause.substring(0, whereClause.length() - 3);
		}

		String orderByClause = "ORDER BY NICKNAME";
		if (orderBys.size() > 0) {
			orderByClause = "ORDER BY " + orderBys.values().toString().replaceAll("\\[", "").replaceAll("\\]", "");
		}

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		String sql = "SELECT *, (MARKET_COST - PURCHASE_COST) AS CURRENT_VALUE_CHANGE FROM registry WHERE "
				+ whereClause + " " + dirtyClause + " " + orderByClause;
		ArrayList<HashMap<String, String>> gunSearch = SystemUtils.makeSQLAsArrayListHashMapPlain(conn, sql);

		String whereClauseDirtyClause = whereClause + " " + dirtyClause;
		whereClauseDirtyClause = whereClauseDirtyClause.replaceAll("\s+", "\s").trim().replaceAll("1=1 AND ", "");

		mu.addToModel("searchStats", gunSearch.size() + " Entries Found: " + whereClauseDirtyClause);
		mu.addToModel("gunsFound", Integer.valueOf(gunSearch.size()));

		ArrayList<ArrayList<HashMap<String, String>>> allGuns = getGunsInTwoColumn(gunSearch);
		mu.addToModel("report_ArrayListArrayListHashMapStringString", allGuns);

		mu.addToModel("reportTitle", "Search Entries");
		mu.addToModel("todaysDate", new Date(System.currentTimeMillis()));
		conn.close();

		mu.printJson();
		return "registry_all";
	}

	@GetMapping("/registry_edit")
	public String registryEdit(@RequestParam(name = "gun_pk", required = false) Long gunPk, Model model)
			throws SQLException {

		ModelUtils mu = new ModelUtils(model);
		Registry registryEdit = gunRegistryRepo.findById(gunPk).get();
		mu.addToModel("gunRegistry", registryEdit);
		mu.addToModel("reportTitle", "Edit Entry");
		mu.addToModel("allCaliberSet_TreeSetString", ControllerUtils.getAllCaliberValues(validCaliberRepo));

		mu.printJson();
		return "registry_edit";
	}

	@PostMapping("/registry_edit")
	public String registryEditFinal(HttpServletRequest request, @RequestParam("gunPhoto") MultipartFile multipartFile,
			@ModelAttribute Registry gunRegistry, Model model) throws SQLException, IOException {

		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		String gunFunAppPhotoLocation = env.getProperty("GUNFUN_APP_FOLDER") + "\\_images\\";
		if (request.getParameter("password").equals(ControllerUtils.getDeleteMasterPassword(conn))) {

			long gunPkToDelete = gunRegistry.getGunPk();
			String nickname = gunRegistry.getNickname();

			deleteRegistry(conn, gunFunAppPhotoLocation, gunPkToDelete, nickname);
			mu.addToModel("MESSAGE", "'" + nickname + "' removed from Registry.");

		} else {

			String priorGunNickname = request.getParameter("PRIOR_NICKNAME");
			String currentGunNickname = gunRegistry.getNickname();

			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			if (!fileName.equals("")) {
				handleNewGunUpload(gunFunAppPhotoLocation, priorGunNickname, multipartFile);
			}

			if (!priorGunNickname.equals(currentGunNickname)) {
				handleReplacementGunUpload(conn, gunFunAppPhotoLocation, priorGunNickname, currentGunNickname);
			}

			gunRegistryRepo.save(gunRegistry);
			conn.close();

			mu.addToModel("MESSAGE", "'" + currentGunNickname + "' edited in Registry.");

		}

		mu.printJson();
		return "frame_main";
	}

	@GetMapping("/registry_add")
	public String registryAdd(Model model) throws SQLException {
		ModelUtils mu = new ModelUtils(model);
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		mu.addToModel("gunRegistry", new Registry());
		mu.addToModel("allCaliberSet_TreeSetString", ControllerUtils.getAllCaliberValues(validCaliberRepo));
		mu.addToModel("reportTitle", "Add Registry Entry");
		conn.close();

		mu.printJson();
		return "registry_add";
	}

	@PostMapping("/registry_add")
	public String registryAddToDb(@RequestParam("gunPhoto") MultipartFile multipartFile,
			@ModelAttribute Registry gunRegistry, Model model) throws IOException {

		ModelUtils mu = new ModelUtils(model);
		gunRegistry.setMarketCostDate(gunRegistry.getPurchaseDate());
		gunRegistry.setMarketCost(gunRegistry.getPurchaseCost());
		gunRegistry.setMarketUrl("Original Purchase");
		gunRegistryRepo.save(gunRegistry);

		String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());

		String gunFunAppPhotoLocation = env.getProperty("GUNFUN_APP_FOLDER") + "\\_images\\";
		if (fileName.equals("")) {
			handleNewPlaceholderGunUpload(gunRegistry, gunFunAppPhotoLocation);
		} else {
			handleNewGunUpload(gunFunAppPhotoLocation, gunRegistry.getNickname(), multipartFile);
		}

		// make some quiz questions
		List<Registry> singleGunList = new ArrayList<Registry>();
		singleGunList.add(gunRegistry);
		ControllerUtils.addStandardQuestionsForGuns(gunTriviaTemplateQuestionsRepo, singleGunList);

		// Add a cleaning record, I should be cleaning it before ever firing it.
		gunCleaningSessionsRepo
				.save(new CleaningSession(gunRegistry.getNickname(), new Date(System.currentTimeMillis())));

		mu.addToModel("MESSAGE", "'" + gunRegistry.getNickname() + "' added to Registry.");

		mu.printJson();
		return "frame_main";
	}

	// FUNCTIONS ------------------------------------------------

	private ArrayList<ArrayList<HashMap<String, String>>> getGunsInTwoColumn(
			ArrayList<HashMap<String, String>> gunsFound) {

		ArrayList<ArrayList<HashMap<String, String>>> twoColumnGuns = new ArrayList<ArrayList<HashMap<String, String>>>();

		if (gunsFound.size() % 2 != 0) {
			HashMap<String, String> blankEntry = new HashMap<String, String>();
			blankEntry.put("GUN_PK", "");
			gunsFound.add(blankEntry);
		}

		ArrayList<HashMap<String, String>> insertLine = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < gunsFound.size(); i++) {
			if (i % 2 == 0) {
				insertLine.add(gunsFound.get(i));
			} else {
				insertLine.add(gunsFound.get(i));
				twoColumnGuns.add(insertLine);
				insertLine = new ArrayList<HashMap<String, String>>();
			}
		}
		return twoColumnGuns;
	}

	private void deleteRegistry(Connection conn, String gunFunAppPhotoLocation, long gunPkToDelete, String nickname)
			throws SQLException {

		try {
			SystemUtils.deleteFile(gunFunAppPhotoLocation + "large\\" + nickname + ".jpg");
		} catch (IOException e) {
		}

		try {
			SystemUtils.deleteFile(gunFunAppPhotoLocation + "medium\\" + nickname + ".jpg");
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			SystemUtils.deleteFile(gunFunAppPhotoLocation + "small\\" + nickname + ".jpg");
		} catch (IOException e) {
		}

		SystemUtils.executeSQL(conn, "DELETE FROM cleaning_reports where NICKNAME='" + nickname + "'");
		SystemUtils.executeSQL(conn, "DELETE FROM carry_sessions where NICKNAME='" + nickname + "'");
		SystemUtils.executeSQL(conn, "DELETE FROM cleaning_sessions where NICKNAME='" + nickname + "'");
		SystemUtils.executeSQL(conn, "DELETE FROM shooting_sessions where NICKNAME='" + nickname + "'");
		SystemUtils.executeSQL(conn, "DELETE FROM trivia_round_questions where NICKNAME='" + nickname + "'");
		SystemUtils.executeSQL(conn, "DELETE FROM trivia_question_templates where NICKNAME='" + nickname + "'");
		SystemUtils.executeSQL(conn, "DELETE FROM trivia_question_templates_custom where NICKNAME='" + nickname + "'");

		gunRegistryRepo.deleteById(gunPkToDelete);
	}

	private void handleNewGunUpload(String gunFunAppPhotoLocation, String priorGunNickname, MultipartFile multipartFile)
			throws IOException {

		SystemUtils.saveFile(gunFunAppPhotoLocation + "large\\", priorGunNickname + ".jpg", multipartFile);

		// Rescale the original image
		BufferedImage imageIn = ImageIO.read(new File(gunFunAppPhotoLocation + "large\\" + priorGunNickname + ".jpg"));
		ImageIO.write(SystemUtils.resizeImage(imageIn, 550, 412), "jpeg",
				new File(gunFunAppPhotoLocation + "medium\\" + priorGunNickname + ".jpg"));
		ImageIO.write(SystemUtils.resizeImage(imageIn, 350, 260), "jpeg",
				new File(gunFunAppPhotoLocation + "small\\" + priorGunNickname + ".jpg"));
	}

	private void handleNewPlaceholderGunUpload(Registry gunRegistry, String gunFunAppPhotoLocation) throws IOException {

		SystemUtils.copyFile(gunFunAppPhotoLocation + "\\_NEW.jpg",
				gunFunAppPhotoLocation + "large\\" + gunRegistry.getNickname() + ".jpg");

		BufferedImage imageIn = ImageIO
				.read(new File(gunFunAppPhotoLocation + "large\\" + gunRegistry.getNickname() + ".jpg"));
		ImageIO.write(SystemUtils.resizeImage(imageIn, 550, 412), "jpeg",
				new File(gunFunAppPhotoLocation + "medium\\" + gunRegistry.getNickname() + ".jpg"));
		ImageIO.write(SystemUtils.resizeImage(imageIn, 350, 260), "jpeg",
				new File(gunFunAppPhotoLocation + "small\\" + gunRegistry.getNickname() + ".jpg"));
	}

	private void handleReplacementGunUpload(Connection conn, String gunFunAppPhotoLocation, String priorGunNickname,
			String currentGunNickname) throws IOException, SQLException {

		// change anything with this nickname with the new value
		// Copy image for the gun
		SystemUtils.copyFile(gunFunAppPhotoLocation + "large\\" + priorGunNickname + ".jpg",
				gunFunAppPhotoLocation + "large\\" + currentGunNickname + ".jpg");
		SystemUtils.copyFile(gunFunAppPhotoLocation + "medium\\" + priorGunNickname + ".jpg",
				gunFunAppPhotoLocation + "medium\\" + currentGunNickname + ".jpg");
		SystemUtils.copyFile(gunFunAppPhotoLocation + "small\\" + priorGunNickname + ".jpg",
				gunFunAppPhotoLocation + "small\\" + currentGunNickname + ".jpg");

		// Delete the old one
		SystemUtils.deleteFile(gunFunAppPhotoLocation + "large\\" + priorGunNickname + ".jpg");
		SystemUtils.deleteFile(gunFunAppPhotoLocation + "medium\\" + priorGunNickname + ".jpg");
		SystemUtils.deleteFile(gunFunAppPhotoLocation + "small\\" + priorGunNickname + ".jpg");

		SystemUtils.executeSQL(conn, "UPDATE cleaning_reports SET NICKNAME = '" + currentGunNickname
				+ "' where NICKNAME='" + priorGunNickname + "'");
		SystemUtils.executeSQL(conn, "UPDATE carry_sessions SET NICKNAME = '" + currentGunNickname
				+ "' where NICKNAME='" + priorGunNickname + "'");
		SystemUtils.executeSQL(conn, "UPDATE cleaning_sessions SET NICKNAME = '" + currentGunNickname
				+ "' where NICKNAME='" + priorGunNickname + "'");
		SystemUtils.executeSQL(conn, "UPDATE shooting_sessions SET NICKNAME = '" + currentGunNickname
				+ "' where NICKNAME='" + priorGunNickname + "'");
		SystemUtils.executeSQL(conn, "UPDATE trivia_round_questions SET NICKNAME = '" + currentGunNickname
				+ "' where NICKNAME='" + priorGunNickname + "'");
		SystemUtils.executeSQL(conn, "UPDATE trivia_question_templates SET NICKNAME = '" + currentGunNickname
				+ "' where NICKNAME='" + priorGunNickname + "'");
		SystemUtils.executeSQL(conn, "UPDATE trivia_question_templates_custom SET NICKNAME = '" + currentGunNickname
				+ "' where NICKNAME='" + priorGunNickname + "'");
	}

}
