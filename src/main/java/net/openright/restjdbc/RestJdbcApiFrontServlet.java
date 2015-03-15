package net.openright.restjdbc;

import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.sql.DataSource;

import net.openright.infrastructure.db.Database;
import net.openright.infrastructure.rest.GetController;
import net.openright.infrastructure.rest.JsonGetController;
import net.openright.infrastructure.rest.JsonPostController;
import net.openright.infrastructure.rest.PostController;
import net.openright.infrastructure.rest.RestApiFrontController;
import net.openright.restjdbc.orders.OrdersApiController;

public class RestJdbcApiFrontServlet extends RestApiFrontController {
	
	private OrdersApiController ordersController;

	@Override
	public void init() throws ServletException {
		Database database ;
		try {
			database = new Database((DataSource) new InitialContext().lookup("jdbc/restjdbc"));
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}

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
