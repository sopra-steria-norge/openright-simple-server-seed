package net.openright.restjdbc;

import javax.sql.DataSource;

import net.openright.infrastructure.config.AppConfigFile;

public class RestJdbcAppConfigFile extends AppConfigFile implements RestJdbcAppConfig {

	public RestJdbcAppConfigFile(String filename) {
		super(filename);
	}

	@Override
	public DataSource createDataSource() {
		return createDataSource("restjdbc", "restjdbc");
	}
	

}
