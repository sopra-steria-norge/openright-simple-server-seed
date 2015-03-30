package net.openright.simpleserverseed.domain.orders;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.openright.infrastructure.db.PgsqlDatabase;
import net.openright.infrastructure.util.IOUtil;
import net.openright.simpleserverseed.application.SeedAppServer;
import net.openright.simpleserverseed.application.SeedAppConfig;
import net.openright.simpleserverseed.application.SimpleseedTestConfig;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class OrderWebTest {
	private static SeedAppConfig config = new SimpleseedTestConfig();
	private static SeedAppServer server = new SeedAppServer(config);
	private static WebDriver browser;
	private OrdersRepository repository = new OrdersRepository(new PgsqlDatabase("jdbc/seedappDs"));

	@BeforeClass
	public static void startServer() throws Exception {
		server.start(0);
	}

	@BeforeClass
	public static void startBrowser() throws Exception {
		File driverFile = new File("target/chromedriver.exe");
		if (!driverFile.exists()) {
			URL chromeDriverUrl = new URL("http://chromedriver.storage.googleapis.com/");
			String chromeDriverVersion = IOUtil.toString(new URL(chromeDriverUrl, "LATEST_RELEASE"));

			File zippedFile = new File("target/chromedriver.zip");

			IOUtil.copy(new URL(chromeDriverUrl, chromeDriverVersion + "/chromedriver_win32.zip"), zippedFile);
			try (ZipFile zipFile = new ZipFile(zippedFile)) {
				ZipEntry zipEntry = zipFile.getEntry("chromedriver.exe");
				IOUtil.copy(zipFile.getInputStream(zipEntry), driverFile);
			}
		}
		System.setProperty("webdriver.chrome.driver", driverFile.getPath());
		browser = new ChromeDriver();
		browser.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
	}

	@AfterClass
	public static void stopClient() {
		browser.quit();
	}

	@Test
	public void shouldSeeCurrentOrders() throws Exception {
		Order order = OrderRepositoryTest.sampleOrder();
		repository.insert(order);

		browser.get(server.getURI().toString());

		List<String> orders = browser.findElement(By.id("ordersList"))
			.findElements(By.tagName("li"))
			.stream().map(e -> e.getText()).collect(Collectors.toList());

		assertThat(orders).contains("Order: " + order.getTitle());
	}

	@Test
	public void shouldInsertNewOrders() throws Exception {
		browser.get(server.getURI().toString());
		browser.findElement(By.id("addOrder")).click();

		String orderTitle = OrderRepositoryTest.sampleOrder().getTitle();

		WebElement titleField = browser.findElement(By.name("order[title]"));
		titleField.clear();
		titleField.sendKeys(orderTitle);
		titleField.submit();

		browser.findElement(By.id("ordersList"));

		assertThat(repository.list().stream().map(o -> o.getTitle()).collect(Collectors.toList()))
			.contains(orderTitle);
	}

}
