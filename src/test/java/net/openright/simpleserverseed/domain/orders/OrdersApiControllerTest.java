package net.openright.simpleserverseed.domain.orders;


import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import net.openright.simpleserverseed.application.AppConfigFile;
import net.openright.simpleserverseed.domain.orders.Order;
import net.openright.simpleserverseed.domain.orders.OrdersApiController;
import net.openright.simpleserverseed.infrastructure.db.Database;

import org.junit.Test;

public class OrdersApiControllerTest {
	
	@Test
	public void shouldRetrieveSavedOrders() throws Exception {
		AppConfigFile config = new AppConfigFile("src/test/resources/test-simpleserverseed.properties");
		DataSource dataSource = config.createDataSource("restjdbc", "restjdbc_test");
		Database database = new Database(dataSource);
		OrdersApiController controller = new OrdersApiController(database);
		
		Order order = sampleOrder();
		controller.postOrder(order);
		assertThat(controller.getOrders()).contains(order);
	}

	private Order sampleOrder() {
		return new Order(12, "Hello");
	}

}
