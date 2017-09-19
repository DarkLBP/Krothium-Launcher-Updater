package kml;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
	Downloads the update and overwrites the existing launcher file (if the name matches)
	Deletes itself using the scripts in the resources folder.
 */

public class MainController {

	private final String UPDATE_URL = "http://mc.krothium.com/content/download.php"; // Direct link

	@FXML private ProgressBar progressBar;
	@FXML private Label       currentProcess;

	@FXML private TextArea   textareaConsole;
	@FXML private TitledPane consolePane;
	@FXML private Accordion  consoleAccordion;

	public void startUpdate(File jarToUpdate) {
		// Autoextend console
		this.consoleAccordion.setExpandedPane(this.consolePane);

		this.updateStatus("Contacting update server...");
		this.initDownload(jarToUpdate);
	}

	private void initDownload(File jar) {
		new Thread(() -> {
			try {
				this.launchFile(this.downloadFile(jar));
				System.exit(0);
			} catch (Exception ex) {
				ex.printStackTrace();
				this.updateStatus("An error occurred while preforming update!");
			}
		}).start();
	}

	private File downloadFile(File jar) throws Exception {
		String absolutePath = jar.getAbsolutePath();
		String fileName = jar.getName();
		String oldExtension = "";
		if (fileName.lastIndexOf(".") != -1) {
			oldExtension = fileName.substring(fileName.lastIndexOf("."), fileName.length());
		}
		OS platform = getPlatform();
		String extension = "";
		if (platform == OS.WINDOWS) {
			extension = ".exe";
		} else if (platform == OS.UNKNOWN) {
			extension = ".jar";
		}
		String newFileName;
		if (oldExtension.isEmpty()) {
			newFileName = fileName + extension;
		} else {
			newFileName = fileName.replace(oldExtension, extension);
		}
		File outputFile = new File(jar.getParent(), newFileName + ".tmp");
		File newFile = new File(jar.getParent(), newFileName);
		String query = "?o=" + platform.name() + "&a=" + getOSArch().name();
		URL           url           = new URL(this.UPDATE_URL + query);
		URLConnection urlConnection = url.openConnection();
		InputStream   inputStream   = urlConnection.getInputStream();
		long          fileSize      = urlConnection.getContentLength();

		this.updateStatus("Downloading update file...");
		this.updateStatus("Update Size: " + fileSize + " bytes", true);

		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
		byte[]               buffer               = new byte[32 * 1024];
		int                  bytesRead;
		double               sumCount             = 0.0;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			bufferedOutputStream.write(buffer, 0, bytesRead);

			sumCount += bytesRead;
			double percent = (sumCount) / fileSize;
			Platform.runLater(() -> progressBar.setProgress(percent));
		}

		bufferedOutputStream.flush();
		bufferedOutputStream.close();
		inputStream.close();

		if (!jar.delete()) {
			throw new Exception("We couldn't delete the old version file.");
		}
		if (!outputFile.renameTo(newFile)) {
			throw new Exception("Failed to update the existing file.");
		}
		this.updateStatus("Finished downloading update.");
		return newFile;
	}

	private void launchFile(File jar) throws IOException{
		ProcessBuilder pb = new ProcessBuilder(getJavaDir(), "-jar", jar.getAbsolutePath());
		pb.start();
	}

	public static String getJavaDir() {
		final String separator = System.getProperty("file.separator");
		final String path = System.getProperty("java.home") + separator + "bin" + separator;
		if (getPlatform() == OS.WINDOWS && new File(path + "javaw.exe").isFile()) {
			return path + "javaw.exe";
		}
		return path + "java";
	}

	public static OS getPlatform() {
		final String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("win")) {
			return OS.WINDOWS;
		} else if (osName.contains("mac")) {
			return OS.OSX;
		} else if (osName.contains("linux") || osName.contains("unix")) {
			return OS.LINUX;
		}
		return OS.UNKNOWN;
	}

	public static OSArch getOSArch() {
		String arch = System.getProperty("os.arch");
		String realArch = arch.endsWith("64") ? "64" : "32";
		return "32".equals(realArch) ? OSArch.OLD : OSArch.NEW;
	}

	private void updateStatus(String status) {
		updateStatus(status, false);
	}

	private void updateStatus(String status, boolean dontChangeProcess) {
		if (!dontChangeProcess) {
			Platform.runLater(() -> this.currentProcess.setText(status));
		}

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalDateTime     now               = LocalDateTime.now();
		this.textareaConsole.appendText("[" + dateTimeFormatter.format(now) + "] " + status + "\n");
	}
}
