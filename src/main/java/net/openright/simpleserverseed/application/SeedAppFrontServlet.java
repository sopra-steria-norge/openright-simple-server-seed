package net.openright.simpleserverseed.application;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import net.openright.infrastructure.db.PgsqlDatabase;
import net.openright.infrastructure.rest.GetController;
import net.openright.infrastructure.rest.JsonGetController;
import net.openright.infrastructure.rest.JsonPostController;
import net.openright.infrastructure.rest.PostController;
import net.openright.infrastructure.rest.RestApiFrontController;
import net.openright.simpleserverseed.domain.orders.OrdersApiController;

public class SeedAppFrontServlet extends RestApiFrontController {

	private static final long serialVersionUID = 6363140410513232499L;

	private OrdersApiController ordersController;

	@Override
	public void init() throws ServletException {
		PgsqlDatabase database = new PgsqlDatabase("jdbc/restjdbd");
		ordersController = new OrdersApiController(database);
	}

	@Override
	protected Map<String,GetController> getControllers() {
        Map<String, GetController> controllers = new HashMap<>();

        controllers.put("/orders", new JsonGetController(ordersController));

        return controllers;
    }

	@Override
	protected Map<String, PostController> postControllers() {
        Map<String, PostController> controllers = new HashMap<>();

        controllers.put("/orders", new JsonPostController(ordersController));

        return controllers;
	}

}
