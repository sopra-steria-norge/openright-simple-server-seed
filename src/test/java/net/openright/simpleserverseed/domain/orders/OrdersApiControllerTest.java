package net.openright.simpleserverseed.domain.orders;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.sql.DataSource;

import net.openright.infrastructure.db.PgsqlDatabase;
import net.openright.simpleserverseed.application.SimpleseedAppConfigFile;

import org.junit.Test;

public class OrdersApiControllerTest {

	private Random random = new Random();
	private SimpleseedAppConfigFile config = new SimpleseedAppConfigFile("test-restjdbc.properties");
	private DataSource dataSource = config.createDataSource("restjdbc", "restjdbc_test");
	private PgsqlDatabase database = new PgsqlDatabase(dataSource);

	@Test
	public void shouldRetrieveSavedOrdersWithoutOrderLines() throws Exception {
		OrdersRepository repository = new OrdersRepository(database);

		Order order = sampleOrder();
		repository.insert(order);
		assertThat(repository.list()).contains(order);

		assertThat(repository.retrieve(order.getId()))
			.isEqualToComparingFieldByField(order);
	}

	private Order sampleOrder() {
		return new Order(sampleString(4), new ArrayList<OrderLine>());
	}

	private String sampleString(int numberOfWords) {
		List<String> words = new ArrayList<String>();
		for (int i = 0; i < numberOfWords; i++) {
			words.add(sampleWord());
		}
		return String.join(" ", words);
	}

	private String sampleWord() {
		return random(new String[] { "foo", "bar", "baz", "qux", "quux", "quuuux" });
	}

	private <T> T random(@SuppressWarnings("unchecked") T... alternatives) {
		return alternatives[random.nextInt(alternatives.length)];
	}

}
