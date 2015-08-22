package net.openright.simpleserverseed.application;

import javax.sql.DataSource;


public interface SeedAppConfig {

	int getHttpPort();

	DataSource getDataSource();

	void start();
}
