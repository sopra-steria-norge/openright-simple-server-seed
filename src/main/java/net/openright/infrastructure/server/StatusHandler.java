package net.openright.simpleserverseed.infrastructure.server;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;

public class StatusHandler extends ServletContextHandler {
	
	public StatusHandler() {
		setContextPath("/status");
		
		addServlet(new ServletHolder(new AdminServlet()), "/admin/*");

		setAttribute(MetricsServlet.METRICS_REGISTRY, createMetricsRegistry());
        setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, createHealthRegistry());
	}

	private Object createMetricsRegistry() {
        MetricRegistry metricRegistry = new MetricRegistry();
        metricRegistry.register("jvm/gc", new GarbageCollectorMetricSet());
        metricRegistry.register("jvm/memory", new MemoryUsageGaugeSet());
        metricRegistry.register("jvm/thread-states", new ThreadStatesGaugeSet());
        metricRegistry.register("jvm/fd/usage", new FileDescriptorRatioGauge());
		return metricRegistry;
	}

	private Object createHealthRegistry() {
        HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
		return healthCheckRegistry;
	}
	
	
}
