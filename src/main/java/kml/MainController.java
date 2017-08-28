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

	private String UPDATE_URL = "http://mc.krothium.com/content/Krothium_Launcher"; // Direct link

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
				this.downloadFile(jar);
				this.launchFile(jar);
				this.cleanUp();
			} catch (Exception ex) {
				ex.printStackTrace();
				this.updateStatus("An error occurred while preforming update!");
			}
		}).start();
	}

	private void downloadFile(File jar) throws Exception {
		String absolutePath = jar.getAbsolutePath();
		String extension = absolutePath.substring(absolutePath.lastIndexOf("."), absolutePath.length());
		File outputFile = new File(absolutePath.replace(extension, ".tmp"));

		URL           url           = new URL(this.UPDATE_URL + extension);
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
		if (!outputFile.renameTo(jar)) {
			throw new Exception("Failed to update the existing file.");
		}
		this.updateStatus("Finished downloading update.");
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


	private void cleanUp() throws Exception {
		String  OS      = System.getProperty("os.name").toLowerCase();
		Runtime runtime = Runtime.getRuntime();
		if (OS.contains("win")) {
			this.copyResource("scripts/windows_cleanup.bat");
			runtime.exec("windows_cleanup.bat");
		} else if (OS.contains("mac") || OS.contains("nix") || OS.contains("nux") || OS.contains("aix")) {
			this.copyResource("scripts/linux_cleanup.sh");
			runtime.exec("sh linux_cleanup.sh");
		}
		System.exit(0);
	}

	private void copyResource(String name) {

		InputStream  inputStream  = this.getClass().getClassLoader().getResourceAsStream(name);
		OutputStream outputStream = null;
		try {
			String fileName = name.substring(name.lastIndexOf("/") + 1, name.length());
			outputStream = new FileOutputStream(fileName);
			int    read;
			byte[] bytes = new byte[1024];
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outputStream != null) {
				try {
					// outputStream.flush();
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
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
