package net.openright.simpleserverseed.application;

import net.openright.infrastructure.rest.ApiFrontController;
import net.openright.infrastructure.rest.Controller;
import net.openright.infrastructure.rest.JsonResourceController;
import net.openright.infrastructure.util.ExceptionUtil;
import net.openright.simpleserverseed.domain.orders.OrdersApiController;
import net.openright.simpleserverseed.domain.products.ProductsApiController;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;

public class SeedAppFrontServlet extends ApiFrontController {

    private static final long serialVersionUID = -7849698810964144396L;
    private SeedAppConfig config;

    @Override
    public void init() throws ServletException {
        try {
            this.config = (SeedAppConfig)new InitialContext().lookup("seedapp/config");
        } catch (NamingException e) {
            throw ExceptionUtil.soften(e);
        }
    }

    @Override
    protected Controller getControllerForPath(String prefix) {
        switch (prefix) {
            case "orders": return new JsonResourceController(new OrdersApiController(config));
            case "products": return new JsonResourceController(new ProductsApiController(config));
            default: return null;
        }
    }
}
