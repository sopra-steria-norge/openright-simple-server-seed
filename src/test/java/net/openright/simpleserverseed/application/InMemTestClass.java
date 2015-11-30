package net.openright.simpleserverseed.application;

import org.junit.Before;

public class InMemTestClass {
    protected SeedAppConfig config = SimpleseedTestConfig.instance();

    @Before
    public void cleanDb(){
        config.getDatabase().executeOperation("TRUNCATE SCHEMA public AND COMMIT");
    }
}
