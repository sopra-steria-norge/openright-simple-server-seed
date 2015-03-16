package net.openright.simpleserverseed.application;

import javax.sql.DataSource;

import net.openright.simpleserverseed.infrastructure.config.SeedAppConfigFile;

public class AppConfigFile extends SeedAppConfigFile implements AppConfig {

	public AppConfigFile(String filename) {
		super(filename);
	}

	@Override
	public DataSource createDataSource() {
		return createDataSource("restjdbc", "restjdbc");
	}
	

}
