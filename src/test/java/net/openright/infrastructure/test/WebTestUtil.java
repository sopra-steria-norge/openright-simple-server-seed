package net.openright.infrastructure.test;

import net.openright.infrastructure.util.IOUtil;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;

public class WebTestUtil {

    public static WebDriver createDriver(String driverName) throws IOException {
        switch (driverName) {
        case "org.openqa.selenium.firefox.FirefoxDriver":
            return createFirefoxDriver();
        case "org.openqa.selenium.chrome.ChromeDriver":
            return createChromeDriver();
        case "org.openqa.selenium.ie.InternetExplorerDriver":
            return createMsieDriver();
        default:
            return createFirefoxDriver();
        }
    }

    public static ChromeDriver createChromeDriver() throws IOException {
        Path driverFile = Paths.get("target", "chromedriver.exe");
        if (Files.notExists(driverFile)) {
            URL chromeDriverUrl = new URL("http://chromedriver.storage.googleapis.com/");
            String chromeDriverVersion = IOUtil.toString(new URL(chromeDriverUrl, "LATEST_RELEASE")).trim();

            URL latestDriverVersion = new URL(chromeDriverUrl, chromeDriverVersion + "/chromedriver_win32.zip");
            Path zipFile = IOUtil.copy(latestDriverVersion, Paths.get("target/"));
            extractZipEntry(zipFile, driverFile);
        }
        System.setProperty("webdriver.chrome.driver", driverFile.toString());
        return new ChromeDriver();
    }

    public static InternetExplorerDriver createMsieDriver() throws IOException {
        Path driverFile = Paths.get("target", "IEDriverServer.exe");
        if (Files.notExists(driverFile)) {
            URL msieDriverUrl = new URL("http://selenium-release.storage.googleapis.com/");

            /*
            String latestFile = Xml.read(IOUtil.toString(msieDriverUrl))
                    .find("ListBucketResult", "Contents", "Key")
                    .texts()
                    .stream()
                    .filter(s -> s.contains("IEDriver"))
                    .max(String::compareTo)
                    .get();*/
            String latestFile = "2.52/IEDriverServer_x64_2.52.1.zip";

            Path zipFile = IOUtil.copy(new URL(msieDriverUrl, latestFile), Paths.get("target/"));
            extractZipEntry(zipFile, driverFile);
        }
        System.setProperty("webdriver.ie.driver", driverFile.toString());
        return new InternetExplorerDriver();
    }

    public static WebDriver createFirefoxDriver() {
        return new FirefoxDriver();
    }

    private static void extractZipEntry(Path zippedFile, Path driverFile) throws IOException {
        try ( FileSystem zipFs = FileSystems.newFileSystem(zippedFile, null) ) {
            Files.copy(zipFs.getPath(driverFile.getFileName().toString()), driverFile);
        }
    }

}
