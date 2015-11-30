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
        String dbUrl = getProperty("seed.db.url", "jdbc:hsqldb:mem:seed-test");
        String dbUser = getProperty("seed.db.user", "SA");
        String dbPassword = getProperty("seed.db.password", "");
        return new InMemDb().getDataSource(dbUrl, dbUser, dbPassword);
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
