package net.openright.simpleserverseed.application;

import javax.sql.DataSource;

import net.openright.simpleserverseed.infrastructure.config.AppConfigFile;

public class SimpleseedAppConfigFile extends AppConfigFile implements SimpleseedAppConfig {

	public SimpleseedAppConfigFile(String filename) {
		super(filename);
	}

	@Override
	public DataSource createDataSource() {
		return createDataSource("restjdbc", "restjdbc");
	}
	

}
