package net.openright.simpleserverseed.application;

import net.openright.infrastructure.db.DBFunctions;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.SqlScript;
import org.flywaydb.core.internal.dbsupport.hsql.HsqlDbSupport;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;
import org.hsqldb.jdbc.JDBCDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InMemDb {
    public DataSource getDataSource(String dbUrl, String user, String password) {
        JDBCDataSource dataSource = createCleanDb(dbUrl, user, password);
        overRideSequenceCallsToHsqldbSyntax();

        try {
            runMigrationScriptsOnCleanDb(dataSource);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return dataSource;
    }

    private void overRideSequenceCallsToHsqldbSyntax() {
        DBFunctions.setInstance(new DBFunctions() {
            public String nextValue(String sequence) {
                return "call next value for " + sequence;
            }
        });
    }

    private JDBCDataSource createCleanDb(String dbUrl, String user, String password) {
        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUser(user);
        dataSource.setPassword(password);
        dataSource.setUrl(dbUrl);

        try {
            dataSource.getConnection().prepareCall("DROP SCHEMA PUBLIC CASCADE").execute();
            dataSource.getConnection().prepareCall("SET DATABASE SQL SYNTAX PGS TRUE").execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return dataSource;
    }


    private static String replaceSequences(String source) {
        Pattern pattern = Pattern.compile("CREATE SEQUENCE(\\b.+\\b)START (\\d+)");
        while (true) {
            Matcher matcher = pattern.matcher(source);
            if (!matcher.find()) {
                break;
            }
            source = matcher.replaceFirst("CREATE SEQUENCE" + matcher.group(1) + "START WITH " + matcher.group(2));
        }

        return source;
    }


    private static String replaceDecimals(String source) {
        Pattern pattern = Pattern.compile("\\s(decimal|DECIMAL)\\s");
        while (true) {
            Matcher matcher = pattern.matcher(source);
            if (!matcher.find()) {
                break;
            }
            source = matcher.replaceFirst(" DECIMAL(10,2) ");
        }

        return source;
    }


    private void runMigrationScriptsOnCleanDb(JDBCDataSource jdbcDataSource) throws SQLException {
        try (Connection connection = jdbcDataSource.getConnection()) {
            for (String location : locationsForMigration()) {
                runWithConnection(location, connection, true);
            }
        }
    }

    private static void runWithConnection(String location, Connection connection, boolean modifyScript) {
        Resource resource = new ClassPathResource(location, SimpleseedTestConfig.class.getClassLoader());
        HsqlDbSupport dbSupport = new HsqlDbSupport(connection);
        String sqlScriptSource = resource.loadAsString("UTF-8");

        if (modifyScript) {
            sqlScriptSource = modify(sqlScriptSource);
        }

        SqlScript sqlScript = new SqlScript(sqlScriptSource, dbSupport);
        sqlScript.execute(new JdbcTemplate(connection, 0));
    }

    private static String modify(String sqlScriptSource) {
        sqlScriptSource = sqlScriptSource.replaceAll("(?i)text", "longvarchar");
        sqlScriptSource = sqlScriptSource.replaceAll("(?i)bytea", "LONGVARBINARY");
        sqlScriptSource = replaceSequences(sqlScriptSource);
        sqlScriptSource = replaceDecimals(sqlScriptSource);
        sqlScriptSource = replaceAlterColoumn(sqlScriptSource);
        return sqlScriptSource;
    }

    private static String replaceAlterColoumn(String sqlScriptSource) {
        StringBuilder res = new StringBuilder(sqlScriptSource);
        for (int pos=res.indexOf("ALTER COLUMN");pos!=-1;pos=res.indexOf("ALTER COLUMN",pos+1)) {
            int delpos = res.indexOf("TYPE ",pos);
            res.delete(delpos,delpos+"TYPE ".length());
        }
        return res.toString();
    }

    private List<String> locationsForMigration() {
        URL migration = getClass().getClassLoader().getResource("db/seed/");
        File migrdir = new File(migration.getFile());
        File[] files = migrdir.listFiles();

        String[] sortert = new String[files.length];
        for (File file : files) {
            Long index = getPrefixNumber(file.getName());
            sortert[((int) (index - 1))] = "db/seed/" + file.getName();
        }
        return Arrays.asList(sortert);
    }

    private Long getPrefixNumber(String verdi) {
        Pattern pattern = Pattern.compile("\\d[\\s\\d]*");
        Matcher matcher = pattern.matcher(verdi);
        if (matcher.find()) {
            String treff = matcher.group();
            return Long.parseLong(treff.replace(" ", ""));
        }
        return null;
    }
}
