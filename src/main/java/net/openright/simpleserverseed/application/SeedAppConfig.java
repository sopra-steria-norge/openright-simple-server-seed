package net.openright.simpleserverseed.application;

import net.openright.infrastructure.db.Database;


public interface SeedAppConfig {

	int getHttpPort();

	Database getDatabase();

    void start();
}
