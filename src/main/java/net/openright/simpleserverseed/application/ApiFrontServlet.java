package net.openright.simpleserverseed.application;

import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.sql.DataSource;

import net.openright.simpleserverseed.domain.orders.OrdersApiController;
import net.openright.simpleserverseed.infrastructure.db.PgsqlDatabase;
import net.openright.simpleserverseed.infrastructure.rest.GetController;
import net.openright.simpleserverseed.infrastructure.rest.JsonGetController;
import net.openright.simpleserverseed.infrastructure.rest.JsonPostController;
import net.openright.simpleserverseed.infrastructure.rest.PostController;
import net.openright.simpleserverseed.infrastructure.rest.RestApiFrontController;

public class ApiFrontServlet extends RestApiFrontController {
	
	private static final long serialVersionUID = 6363140410513232499L;
	
	private OrdersApiController ordersController;

	@Override
	public void init() throws ServletException {
		PgsqlDatabase database ;
		try {
			database = new PgsqlDatabase((DataSource) new InitialContext().lookup("jdbc/restjdbc"));
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
