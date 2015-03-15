package net.openright.restjdbc.orders;


import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import net.openright.infrastructure.db.Database;
import net.openright.restjdbc.RestJdbcAppConfigFile;

import org.junit.Test;

public class OrdersApiControllerTest {
	
	@Test
	public void shouldRetrieveSavedOrders() throws Exception {
		RestJdbcAppConfigFile config = new RestJdbcAppConfigFile("test-restjdbc.properties");
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
