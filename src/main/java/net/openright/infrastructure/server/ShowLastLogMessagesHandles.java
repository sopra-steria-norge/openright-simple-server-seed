package net.openright.infrastructure.server;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.CyclicBufferAppender;

public class ShowLastLogMessagesHandles extends HandlerWrapper {


    private CyclicBufferAppender<ILoggingEvent> cyclicBufferAppender = new CyclicBufferAppender<>();
    private String path;

    public ShowLastLogMessagesHandles(String path) {
        this.path = path;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

        ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel("INFO");
        filter.start();
        cyclicBufferAppender.start();
        cyclicBufferAppender.addFilter(filter);

        rootLogger.addAppender(cyclicBufferAppender);
    }


    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (!target.equals(path)) {
            super.handle(target, baseRequest, request, response);
            return;
        }

        try(PrintWriter writer = response.getWriter()) {
            for (int i=0; i<cyclicBufferAppender.getLength(); i++) {
                writer.write(cyclicBufferAppender.get(i).toString());
                writer.write("\n");
            }
        }
    }




}
