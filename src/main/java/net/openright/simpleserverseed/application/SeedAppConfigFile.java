package net.openright.simpleserverseed.application;

import net.openright.infrastructure.config.AppConfigFile;
import net.openright.infrastructure.util.ExceptionUtil;
import net.openright.infrastructure.util.IOUtil;
import org.eclipse.jetty.plus.jndi.EnvEntry;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Path;

public class SeedAppConfigFile extends AppConfigFile implements SeedAppConfig {

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
	public DataSource getDataSource() {
		try {
			return (DataSource) new InitialContext().lookup("jdbc/seedappDs");
		} catch (NamingException e) {
			throw ExceptionUtil.soften(e);
		}
	}

	@Override
    public void start() {
        try {
            new EnvEntry("jdbc/seedappDs", createDataSource());
            new EnvEntry("seedapp/config", this);
        } catch (NamingException e) {
            throw ExceptionUtil.soften(e);
        }
    }
}
