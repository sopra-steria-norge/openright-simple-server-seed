package net.openright.simpleserverseed.application;

import javax.sql.DataSource;


public interface SimpleseedAppConfig {

	DataSource createDataSource();

	DataSource createTestDataSource();

}
