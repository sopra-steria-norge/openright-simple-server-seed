package net.openright.restjdbc;

import javax.sql.DataSource;


public interface RestJdbcAppConfig {

	DataSource createDataSource();

}
