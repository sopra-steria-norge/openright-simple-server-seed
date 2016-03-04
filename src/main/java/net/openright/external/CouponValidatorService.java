package net.openright.external;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CouponValidatorService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CouponValidatorService.class);
    private static Server server;

    public static void main(String[] argv) throws Exception {
        server = new Server(8080);
        server.setHandler(getHandler());
        server.start();

        log.info("Started server " + server.getURI());
    }

    private static WebAppContext getHandler() {
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        webAppContext.setContextPath("/");
        webAppContext.setResourceBase("/");
        webAppContext.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                String coupon = req.getParameter("coupon");
                resp.getWriter().print(coupon.hashCode() % 2 == 0);
            }
        }), "/");

        return webAppContext;
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void stop() throws Exception {
        server.stop();
    }
}
