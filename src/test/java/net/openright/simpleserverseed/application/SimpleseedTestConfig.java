package net.openright.simpleserverseed.application;

import java.nio.file.Paths;

import javax.sql.DataSource;

import org.openqa.selenium.WebDriver;

public class SimpleseedTestConfig extends SeedAppConfigFile {

    private static SimpleseedTestConfig instance;

    private SimpleseedTestConfig() {
        super(Paths.get("seedapp-test.properties"));
    }

    @Override
    public DataSource createDataSource() {
        return createTestDataSource("seed");
    }

    public String getWebDriverName() {
        String webdriverClass = WebDriver.class.getName();
        return System.getProperty(webdriverClass, getProperty(webdriverClass, "org.openqa.selenium.chrome.ChromeDriver"));
    }

    public synchronized static SimpleseedTestConfig instance() {
        if (instance == null) {
            instance = new SimpleseedTestConfig();
            instance.start();
        }
        return instance;
    }
}
