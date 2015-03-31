package net.openright.infrastructure.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.openright.infrastructure.util.IOUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

public class WebTestUtil {

    public static ChromeDriver createChromeDriver() throws Exception {
        File driverFile = new File("target/chromedriver.exe");
        if (!driverFile.exists()) {
            URL chromeDriverUrl = new URL("http://chromedriver.storage.googleapis.com/");
            String chromeDriverVersion = IOUtil.toString(new URL(chromeDriverUrl, "LATEST_RELEASE"));

            URL latestDriverVersion = new URL(chromeDriverUrl, chromeDriverVersion + "/chromedriver_win32.zip");
            File zipFile = IOUtil.copy(latestDriverVersion, new File("target/"));
            extractZipEntry(zipFile, driverFile);
        }
        System.setProperty("webdriver.chrome.driver", driverFile.getPath());
        return new ChromeDriver();
    }

    public static InternetExplorerDriver createMsieDriver() throws Exception {
        File driverFile = new File("target/IEDriverServer.exe");
        if (!driverFile.exists()) {
            URL msieDriverUrl = new URL("http://selenium-release.storage.googleapis.com/");

            List<String> msieVersions = new ArrayList<>();
            JSONObject storageContents = XML.toJSONObject(IOUtil.toString(msieDriverUrl));
            JSONArray jsonArray = storageContents.getJSONObject("ListBucketResult").getJSONArray("Contents");
            for (int i = 0; i < jsonArray.length(); i++) {
                String file = jsonArray.getJSONObject(i).getString("Key");
                if (file.contains("IEDriver")) {
                    msieVersions.add(file);
                }
            }
            String latestFile = msieVersions.stream().max(String::compareTo).get();

            File zipFile = IOUtil.copy(new URL(msieDriverUrl, latestFile), new File("target/"));
            extractZipEntry(zipFile, driverFile);
        }
        System.setProperty("webdriver.ie.driver", driverFile.getPath());
        return new InternetExplorerDriver();
    }

    public static WebDriver createFirefoxDriver() {
        return new FirefoxDriver();
    }

    private static void extractZipEntry(File zippedFile, File driverFile) throws IOException {
        try (ZipFile zipFile = new ZipFile(zippedFile)) {
            ZipEntry zipEntry = zipFile.getEntry(driverFile.getName());
            IOUtil.copy(zipFile.getInputStream(zipEntry), driverFile);
        }
    }

}
