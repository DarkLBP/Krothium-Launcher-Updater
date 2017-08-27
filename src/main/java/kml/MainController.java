package kml;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.commons.io.FilenameUtils;

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

	// Dummy link
	private final String UPDATE_URL = "http://speedtest.ftp.otenet.gr/files/test1Mb.db"; // Direct link

	@FXML private ProgressBar progressBar;
	@FXML private Label       currentProcess;

	@FXML private TextArea   textareaConsole;
	@FXML private TitledPane consolePane;
	@FXML private Accordion  consoleAccordion;

	public void startUpdate() {
		// Autoextend console
		this.consoleAccordion.setExpandedPane(this.consolePane);

		this.updateStatus("Contacting update server...");
		this.initDownload();
	}

	private void initDownload() {
		new Thread(() -> {
			try {
				this.downloadFile();
				this.cleanUp();
			} catch (Exception ex) {
				ex.printStackTrace();
				this.updateStatus("An error occurred while preforming update!");
			}
		}).start();
	}

	private void downloadFile() throws IOException {
		URL           url           = new URL(this.UPDATE_URL);
		URLConnection urlConnection = url.openConnection();
		InputStream   inputStream   = urlConnection.getInputStream();
		long          fileSize      = urlConnection.getContentLength();

		this.updateStatus("Downloading update file...");
		this.updateStatus("Update Size: " + fileSize + " bytes", true);

		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(new File(FilenameUtils.getName(this.UPDATE_URL))));
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

		this.updateStatus("Finished downloading update.");
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
			outputStream = new FileOutputStream(FilenameUtils.getName(name));
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
