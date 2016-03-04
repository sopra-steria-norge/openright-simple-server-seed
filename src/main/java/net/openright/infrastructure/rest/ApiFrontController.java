package net.openright.infrastructure.rest;

import net.openright.infrastructure.db.Database;
import net.openright.infrastructure.util.ExceptionUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class ApiFrontController extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            getDatabase().doInTransaction(() -> {
                try {
                    getController(req).handle(req, resp);
                } catch (Exception e) {
                    throw ExceptionUtil.soften(e);
                }
            });
        } catch (RequestException e) {
            resp.sendError(e.getStatusCode(), e.getMessage());
        }
    }

    private Controller getController(HttpServletRequest req) {
        Controller defaultController = (request, res) -> super.service(request, res);
        Controller controller = getControllerForPath(req.getPathInfo().split("/")[1]);
        return controller != null ? controller : defaultController;
    }

    protected abstract Controller getControllerForPath(String prefix);

    protected abstract Database getDatabase();
}
