package net.openright.simpleserverseed.application;

import javax.sql.DataSource;

import net.openright.infrastructure.config.AppConfigFile;

public class SeedAppConfigFile extends AppConfigFile implements SeedAppConfig {

	public SeedAppConfigFile(String filename) {
		super(filename);
	}

	@Override
	public DataSource createDataSource() {
		return createDataSource("seed");
	}

	@Override
	public int getHttpPort() {
		return Integer.parseInt(getProperty("seed.http.port", "8000"));
	}
}
