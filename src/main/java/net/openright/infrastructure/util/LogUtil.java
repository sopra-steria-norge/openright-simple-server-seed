package net.openright.infrastructure.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import net.openright.simpleserverseed.application.SeedAppConfigFile;

public class LogUtil {

    public static void setupLogging(SeedAppConfigFile config) throws JoranException, IOException {
        Files.createDirectories(Paths.get("logs"));

        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.reset();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        configurator.doConfigure(IOUtil.extractResourceFile(config.getLogConfig()).toFile());

        if (config.getSlackLogWebhook() != null) {
            loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(SlackAppender.start(config.getSlackLogWebhook(), loggerContext));
        }
    }
}
