package net.openright.simpleserverseed.application;

import net.openright.infrastructure.config.AppConfigFile;

import javax.sql.DataSource;

public class SeedAppConfigFile extends AppConfigFile implements SeedAppConfig {

	public SeedAppConfigFile(String filename) {
		super(filename);
	}

	@Override
	public DataSource createDataSource() {
		if (System.getenv("DATABASE_URL") != null) {
			return migrateDataSource("parental", createDataSourceFromEnv(System.getenv("DATABASE_URL")));
		}
		return createDataSource("seed");
	}

	@Override
	public int getHttpPort() {
		if (System.getenv("PORT") != null) {
			return Integer.parseInt(System.getenv("PORT"));
		}
		return Integer.parseInt(getProperty("seed.http.port", "8000"));
	}
}
