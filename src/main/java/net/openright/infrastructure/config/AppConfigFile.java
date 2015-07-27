package net.openright.infrastructure.config;

import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Dictionary;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class AppConfigFile {

	private static Logger log = LoggerFactory.getLogger(AppConfigFile.class);

    private long nextCheckTime = 0;
    private long lastLoadTime = 0;
    private Properties properties = new Properties();
    private final Path configFile;

	public AppConfigFile(Path configFile) {
        this.configFile = configFile;
    }

    public Dictionary<Object, Object> getProperties() {
	    ensureConfigurationIsFresh();
        return properties;
    }

    protected DataSource createDataSourceFromEnv(String databaseUrl) {
        Pattern pattern = Pattern.compile("^([^:]+)://([^:]+):([^@]+)@(([-a-z0-9.]+):(\\d+)/(\\w+))$");
        Matcher matcher1 = pattern.matcher(databaseUrl);
        if (!matcher1.matches()) {
            throw new RuntimeException("Unexpected database URL " + databaseUrl);
        }
        Matcher matcher = matcher1;

        HikariDataSource dataSource = new HikariDataSource();
        if (matcher.group(1).equals("postgres")) {
            dataSource.setJdbcUrl("jdbc:postgresql://" + matcher.group(4));
        } else {
            throw new RuntimeException("Unexpected database type " + databaseUrl);
        }
        dataSource.setUsername(matcher.group(2));
        dataSource.setPassword(matcher.group(3));
        return dataSource;
    }

	protected DataSource createDataSource(String prefix) {
		DataSource dataSource = createDataSource(prefix, prefix);
        return migrateDataSource(prefix, dataSource);
	}

    protected DataSource migrateDataSource(String prefix, DataSource dataSource) {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("classpath:db/" + prefix);
        flyway.migrate();

        return dataSource;
    }

    protected DataSource createTestDataSource(String prefix) {
		DataSource dataSource = createDataSource(prefix, prefix + "_test");

		Flyway flyway = new Flyway();
		flyway.setDataSource(dataSource);
		flyway.setLocations("classpath:db/" + prefix);
		if ("true".equals(getProperty(prefix + ".db.test.clean", "false"))) {
			flyway.clean();
		}
		flyway.migrate();

		return dataSource;
	}

	private DataSource createDataSource(String prefix, String defaultName) {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setUsername(getProperty(prefix + ".db.username", defaultName));
		dataSource.setPassword(getProperty(prefix + ".db.password", dataSource.getUsername()));
		dataSource.setJdbcUrl(
				getProperty(prefix + ".db.url", "jdbc:postgresql://localhost:5432/" + dataSource.getUsername()));
		return dataSource;
	}

    public String getProperty(String propertyName, String defaultValue) {
        String result = getProperty(propertyName);
        if (result == null) {
            log.trace("Missing property {} in {}", propertyName, properties.keySet());
            return defaultValue;
        }
        return result;
    }

    public String getRequiredProperty(String propertyName) {
        String result = getProperty(propertyName);
        if (result == null) {
            throw new RuntimeException("Missing property " + propertyName);
        }
        return result;
    }

    private String getProperty(String propertyName) {
        if (System.getProperty(propertyName) != null) {
            log.trace("Reading {} from system properties", propertyName);
            return System.getProperty(propertyName);
        }
        if (System.getenv(propertyName.replace('.', '_')) != null) {
            log.trace("Reading {} from environment", propertyName);
            return System.getenv(propertyName.replace('.', '_'));
        }

        ensureConfigurationIsFresh();
        return properties.getProperty(propertyName);
    }

    private synchronized void ensureConfigurationIsFresh() {
        if (System.currentTimeMillis() < nextCheckTime) return;
        nextCheckTime = System.currentTimeMillis() + 10000;
        log.trace("Rechecking {}", configFile);

        if (!Files.exists(configFile)) {
            log.error("Missing configuration file {}", configFile);
            return;
        }


        try {
            if (lastLoadTime >= Files.getLastModifiedTime(configFile).toMillis()) return;
            lastLoadTime = Files.getLastModifiedTime(configFile).toMillis();
            log.debug("Reloading {}", configFile);

            try (FileInputStream inputStream = new FileInputStream(configFile.toFile())) {
                properties.clear();
                properties.load(inputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + configFile, e);
        }
    }

}
