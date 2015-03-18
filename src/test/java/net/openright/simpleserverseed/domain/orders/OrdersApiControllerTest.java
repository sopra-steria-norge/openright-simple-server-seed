package net.openright.simpleserverseed.domain.orders;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import javax.sql.DataSource;

import net.openright.simpleserverseed.application.AppConfigFile;
import net.openright.simpleserverseed.infrastructure.db.Database;

import org.junit.Test;

public class OrdersApiControllerTest {

	@Test
	public void shouldRetrieveSavedOrders() throws Exception {
		AppConfigFile config = new AppConfigFile(
				"src/test/resources/test-simpleserverseed.properties");
		DataSource dataSource = config.createDataSource("restjdbc",
				"restjdbc_test");
		Database database = new Database(dataSource);
		OrdersApiController controller = new OrdersApiController(database);

		cleanTestData(database);

		Order order = sampleOrder();
		controller.postOrder(order);
		assertThat(controller.getOrders()).contains(order);
	}

	private void cleanTestData(Database database) {
		database.executeOperation("delete from orderlines where id=1 or id=2");
		database.executeOperation("delete from orders where id=13");
	}

	private Order sampleOrder() {
		OrderLine orderLine1 = new OrderLine(1, "Gaming Laptop");
		OrderLine orderLine2 = new OrderLine(2, "Gaming Mouse");
		return new Order(13, "Travelling gamer", Arrays.asList(orderLine1,
				orderLine2));
	}
}
