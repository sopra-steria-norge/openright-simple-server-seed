package net.openright.simpleserverseed.domain.products;

import static org.assertj.core.api.Assertions.assertThat;
import net.openright.infrastructure.db.PgsqlDatabase;
import net.openright.infrastructure.test.SampleData;
import net.openright.simpleserverseed.application.SeedAppConfig;
import net.openright.simpleserverseed.application.SimpleseedTestConfig;

import org.junit.Test;

public class ProductRepositoryTest {

    private SeedAppConfig config = new SimpleseedTestConfig();
    private PgsqlDatabase database = new PgsqlDatabase(config.createDataSource());
    private ProductRepository repository = new ProductRepository(database);

    @Test
    public void shouldRetrieveSavedProduct() throws Exception {
        Product product = sampleProduct();
        repository.insert(product);

        assertThat(repository.retrieve(product.getId()))
            .isEqualToComparingFieldByField(product);
    }

    @Test
    public void shouldListActiveProducts() throws Exception {
        Product product1 = sampleProduct("z");
        Product product2 = sampleProduct("a");
        Product inactiveProduct = sampleProduct();
        inactiveProduct.setInactive();

        repository.insert(product1);
        repository.insert(product2);
        repository.insert(inactiveProduct);

        assertThat(repository.list())
            .containsSubsequence(product2, product1)
            .doesNotContain(inactiveProduct);
    }

    private static Product sampleProduct(String prefix) {
        Product product = new Product();
        product.setTitle(prefix + SampleData.sampleString(3));
        product.setDescription(SampleData.sampleString(10));
        product.setPrice(SampleData.randomAmount());
        return product;
    }

    public static Product sampleProduct() {
        return sampleProduct("");
    }

}
