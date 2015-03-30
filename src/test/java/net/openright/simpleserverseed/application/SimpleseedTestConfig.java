package net.openright.simpleserverseed.application;

import javax.sql.DataSource;

public class SimpleseedTestConfig extends SeedAppConfigFile {

	public SimpleseedTestConfig() {
		super("seedapp-test.properties");
	}

	@Override
	public DataSource createDataSource() {
		return createTestDataSource("seed");
	}

}
