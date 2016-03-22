package net.openright.infrastructure.util;

import org.jsonbuddy.JsonObject;
import org.slf4j.event.Level;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * Logback appender to write to a Slack Webhook. I'm too lazy to maintain XML files,
 * so this appender can be installed directly on a {@link ch.qos.logback.classic.Logger} by
 * calling logger.addAppender(SlackAppender.start(webhookUrl, loggingContext).
 *
 * Get a webhook URL from slack by selecting "Add an app or custom integration" and
 * select Build your own, Make a Custom Integration and Incoming Webhook.
 */
public class SlackAppender extends AsyncAppender {

    private String slackLogWebhook;
    private Layout<ILoggingEvent> layout;

    public SlackAppender() {
        UnsynchronizedAppenderBase<ILoggingEvent> postAppender = new UnsynchronizedAppenderBase<ILoggingEvent>() {
            @Override
            protected void append(ILoggingEvent eventObject) {
                sendToSlack(eventObject);
            }
        };
        postAppender.start();
        addAppender(postAppender);
    }

    protected void sendToSlack(ILoggingEvent eventObject) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(slackLogWebhook).openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            IOUtil.copy(createSlackMessage(eventObject).toString(), conn);

            IOUtil.toString(conn);
            conn.getResponseMessage();
        } catch (IOException e) {
            throw ExceptionUtil.soften(e);
        }

    }

    protected JsonObject createSlackMessage(ILoggingEvent eventObject) {
        return new JsonObject()
                .put("text", layout.doLayout(eventObject));
    }

    public static SlackAppender start(String slackLogWebhook, LoggerContext loggerContext) {
        SlackAppender appender = new SlackAppender();
        appender.setSlackLogWebhook(slackLogWebhook);
        appender.setContext(loggerContext);

        ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel(Level.WARN.name());
        filter.start();
        appender.addFilter(filter);

        PatternLayout layout = new PatternLayout();
        layout.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        layout.setContext(loggerContext);
        layout.start();
        appender.setLayout(layout);

        appender.start();
        return appender;
    }

    private void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

    public void setSlackLogWebhook(String slackLogWebhook) {
        this.slackLogWebhook = slackLogWebhook;
    }

}
