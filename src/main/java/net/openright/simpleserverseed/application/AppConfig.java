package net.openright.simpleserverseed.application;

import javax.sql.DataSource;


public interface AppConfig {

	DataSource createDataSource();

}
