package net.openright.simpleserverseed.application;

import java.io.IOException;
import java.nio.file.Path;

import javax.sql.DataSource;

import net.openright.infrastructure.config.AppConfigFile;
import net.openright.infrastructure.db.Database;
import net.openright.infrastructure.util.IOUtil;

public class SeedAppConfigFile extends AppConfigFile implements SeedAppConfig {

	private Database database;

	public SeedAppConfigFile() throws IOException {
	    super(IOUtil.extractResourceFile("seedapp.properties"));
    }

	public SeedAppConfigFile(Path configFile) {
		super(configFile);
	}

	protected DataSource createDataSource() {
		if (System.getenv("DATABASE_URL") != null) {
			return migrateDataSource("seed", createDataSourceFromEnv(System.getenv("DATABASE_URL")));
		}
		return createDataSource("seed");
	}

	@Override
	public int getHttpPort() {
		if (System.getenv("PORT") != null) {
			return Integer.parseInt(System.getenv("PORT"));
		}
		return Integer.parseInt(getProperty("seed.http.port", "8000"));
	}

    @Override
    public synchronized Database getDatabase() {
        if (database == null) {
            this.database = new Database("jdbc/seedappDs");
        }
        return database;
    }

    @Override
    public void start() {
        this.database = new Database(createDataSource());
    }
}
