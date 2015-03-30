package net.openright.simpleserverseed.domain.orders;

import static org.assertj.core.api.Assertions.assertThat;
import net.openright.infrastructure.db.PgsqlDatabase;
import net.openright.infrastructure.test.SampleData;
import net.openright.simpleserverseed.application.SeedAppConfig;
import net.openright.simpleserverseed.application.SimpleseedTestConfig;

import org.junit.Test;

public class OrderRepositoryTest {

	private SeedAppConfig config = new SimpleseedTestConfig();
	private PgsqlDatabase database = new PgsqlDatabase(config.createDataSource());
	private OrdersRepository repository = new OrdersRepository(database);

	@Test
	public void shouldRetrieveSavedOrdersWithoutOrderLines() throws Exception {
		Order order = sampleOrder();
		repository.insert(order);
		assertThat(repository.list()).contains(order);

		assertThat(repository.retrieve(order.getId()))
			.isEqualToComparingFieldByField(order);
	}

	@Test
	public void shouldRetrieveSavedOrdersWithOrderLines() throws Exception {
		Order order = sampleOrder();

		order.addOrderLine("test");
		order.addOrderLine("test 2");

		repository.insert(order);

		assertThat(repository.retrieve(order.getId()))
			.isEqualToComparingFieldByField(order);
	}

	public static Order sampleOrder() {
		return new Order(SampleData.sampleString(4));
	}

}
