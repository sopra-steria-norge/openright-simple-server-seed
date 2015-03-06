package net.openright.restjdbc;

import java.util.HashMap;
import java.util.Map;

import net.openright.infrastructure.rest.GetController;
import net.openright.infrastructure.rest.JsonGetController;
import net.openright.infrastructure.rest.JsonPostController;
import net.openright.infrastructure.rest.PostController;
import net.openright.infrastructure.rest.RestApiFrontController;
import net.openright.restjdbc.orders.OrdersApiController;

public class RestJdbcApiFrontServlet extends RestApiFrontController {
	
	private OrdersApiController ordersController = new OrdersApiController();

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
