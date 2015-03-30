package net.openright.simpleserverseed.application;

import javax.sql.DataSource;


public interface SeedAppConfig {

	DataSource createDataSource();

	int getHttpPort();

}
