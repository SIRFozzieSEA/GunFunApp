package com.codef.gunfunapp.other;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class BuildUtils {

	// TODO:  A way to customize template questions
	// TODO:  Validation for preference page/calibers page?
	
	public static void buildSampleData(String pathToAppFolder) throws IOException {

		Path sampleAssetsPath = Paths.get("src", "main", "resources", "sample_app_assets");
		String pathToSampleResources = sampleAssetsPath.toFile().getAbsolutePath();
		
		buildBackupScript(pathToSampleResources, pathToAppFolder);
		buildFolderWithAssets(pathToSampleResources, pathToAppFolder, "\\_images\\");
		buildFolderWithAssets(pathToSampleResources, pathToAppFolder, "\\_images\\large\\");
		buildFolderWithAssets(pathToSampleResources, pathToAppFolder, "\\_images\\medium\\");
		buildFolderWithAssets(pathToSampleResources, pathToAppFolder, "\\_images\\small\\");
		buildFolderWithAssets(pathToSampleResources, pathToAppFolder, "\\_manuals\\");
		
	}

	private static void buildBackupScript(String pathToSampleResources, String pathToAppFolder) throws IOException {

		String backupScript = "\\_data\\BackupH2Data.bat";
		String sourceScriptPath = pathToSampleResources + backupScript;
		String targetScriptPath = pathToAppFolder + backupScript;
		File oDirectory = new File(targetScriptPath);
		if (!oDirectory.exists()) {
			SystemUtils.copyFile(sourceScriptPath, targetScriptPath);
		}
		
		backupScript = "\\_backup";
		targetScriptPath = pathToAppFolder + backupScript;
		oDirectory = new File(targetScriptPath);
		if (!oDirectory.exists()) {
			oDirectory.mkdirs();
		}

	}

	private static void buildFolderWithAssets(String pathToSampleResources, String pathToAppFolder,
			String resourceFolder) throws IOException {

		ArrayList<String> standards = new ArrayList<String>(Arrays.asList("_NEW", "_NOT_FOUND"));
		ArrayList<String> nickNames = new ArrayList<String>(Arrays.asList("Alex", "Teresa", "Valerie", "Harriet"));
		String extension = resourceFolder.contains("manual") ? ".pdf" : ".jpg";

		boolean directoryMade = false;
		String appFolder = pathToAppFolder + resourceFolder;
		File oDirectory = new File(appFolder);
		if (!oDirectory.exists()) {
			oDirectory.mkdirs();
			directoryMade = true;
		}

		if (directoryMade) {
			if (resourceFolder.equals("\\_images\\")) {
				for (String singleStandards : standards) {
					SystemUtils.copyFile(pathToSampleResources + resourceFolder + singleStandards + extension,
							appFolder + "\\" + singleStandards + extension);
				}
			} else {
				for (String singleNickname : nickNames) {
					SystemUtils.copyFile(pathToSampleResources + resourceFolder + singleNickname + extension,
							appFolder + "\\" + singleNickname + extension);
				}
				if (extension.equals(".pdf")) {
					SystemUtils.copyFile(pathToSampleResources + resourceFolder + "_NOT_FOUND" + extension,
							appFolder + "\\" + "_NOT_FOUND" + extension);
				}
			}

		}

	}

}
