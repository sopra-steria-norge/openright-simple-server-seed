package net.openright.simpleserverseed.application;

import javax.sql.DataSource;

import net.openright.infrastructure.config.AppConfigFile;

public class SimpleseedAppConfigFile extends AppConfigFile implements SimpleseedAppConfig {

	public SimpleseedAppConfigFile(String filename) {
		super(filename);
	}

	@Override
	public DataSource createDataSource() {
		return createDataSource("seed");
	}

	@Override
	public DataSource createTestDataSource() {
		return createTestDataSource("seed");
	}

	@Override
	public int getHttpPort() {
		return Integer.parseInt(getProperty("seed.http.port", "8000"));
	}
}
