package net.openright.simpleserverseed.application;

import java.util.Dictionary;

import javax.sql.DataSource;


public interface SeedAppConfig {

	DataSource createDataSource();

	int getHttpPort();

    Dictionary<?, ?> getProperties();

}
