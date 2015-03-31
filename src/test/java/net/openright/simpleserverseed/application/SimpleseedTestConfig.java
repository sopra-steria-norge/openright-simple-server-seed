package net.openright.simpleserverseed.application;

import javax.sql.DataSource;

import org.openqa.selenium.WebDriver;

public class SimpleseedTestConfig extends SeedAppConfigFile {

    public SimpleseedTestConfig() {
        super("seedapp-test.properties");
    }

    @Override
    public DataSource createDataSource() {
        return createTestDataSource("seed");
    }

    public String getWebDriverName() {
        String webdriverClass = WebDriver.class.getName();
        return System.getProperty(webdriverClass, getProperty(webdriverClass, "org.openqa.selenium.firefox.FirefoxDriver"));
    }

}
