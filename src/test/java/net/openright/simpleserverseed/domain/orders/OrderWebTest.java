package net.openright.simpleserverseed.domain.orders;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.openright.infrastructure.db.PgsqlDatabase;
import net.openright.infrastructure.test.SampleData;
import net.openright.infrastructure.util.IOUtil;
import net.openright.simpleserverseed.application.SeedAppServer;
import net.openright.simpleserverseed.application.SeedAppConfig;
import net.openright.simpleserverseed.application.SimpleseedTestConfig;
import net.openright.simpleserverseed.domain.products.Product;
import net.openright.simpleserverseed.domain.products.ProductRepository;
import net.openright.simpleserverseed.domain.products.ProductRepositoryTest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class OrderWebTest {
    private static SeedAppConfig config = new SimpleseedTestConfig();
    private static SeedAppServer server = new SeedAppServer(config);
    private static WebDriver browser;
    private static WebDriverWait wait;
    private PgsqlDatabase database = new PgsqlDatabase("jdbc/seedappDs");
    private OrdersRepository orderRepository = new OrdersRepository(database);
    private ProductRepository productRepository = new ProductRepository(database);

    @BeforeClass
    public static void startServer() throws Exception {
        server.start(0);
    }

    @BeforeClass
    public static void startBrowser() throws Exception {
        browser = createFirefoxDriver();
        browser.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        wait = new WebDriverWait(browser, 2);
    }

    private static WebDriver createFirefoxDriver() {
        return new FirefoxDriver();
    }

    private static InternetExplorerDriver createMsieDriver() throws Exception {
        File driverFile = new File("target/IEDriverServer.exe");
        if (!driverFile.exists()) {
            URL msieDriverUrl = new URL("http://selenium-release.storage.googleapis.com/");

            List<String> msieVersions = new ArrayList<>();
            JSONObject storageContents = XML.toJSONObject(IOUtil.toString(msieDriverUrl));
            JSONArray jsonArray = storageContents.getJSONObject("ListBucketResult").getJSONArray("Contents");
            for (int i = 0; i < jsonArray.length(); i++) {
                String file = jsonArray.getJSONObject(i).getString("Key");
                if (file.contains("IEDriver")) {
                    msieVersions.add(file);
                }
            }
            String latestFile = msieVersions.stream().max(String::compareTo).get();

            File zippedFile = IOUtil.copy(new URL(msieDriverUrl, latestFile), new File("target/"));
            try (ZipFile zipFile = new ZipFile(zippedFile)) {
                ZipEntry zipEntry = zipFile.getEntry(driverFile.getName());
                IOUtil.copy(zipFile.getInputStream(zipEntry), driverFile);
            }
        }
        System.setProperty("webdriver.ie.driver", driverFile.getPath());
        return new InternetExplorerDriver();
    }

    public static ChromeDriver createChromeDriver() throws Exception {
        File driverFile = new File("target/chromedriver.exe");
        if (!driverFile.exists()) {
            URL chromeDriverUrl = new URL("http://chromedriver.storage.googleapis.com/");
            String chromeDriverVersion = IOUtil.toString(new URL(chromeDriverUrl, "LATEST_RELEASE"));

            URL latestDriverVersion = new URL(chromeDriverUrl, chromeDriverVersion + "/chromedriver_win32.zip");
            File zippedFile = IOUtil.copy(latestDriverVersion, new File("target/"));
            try (ZipFile zipFile = new ZipFile(zippedFile)) {
                ZipEntry zipEntry = zipFile.getEntry(driverFile.getName());
                IOUtil.copy(zipFile.getInputStream(zipEntry), driverFile);
            }
        }
        System.setProperty("webdriver.chrome.driver", driverFile.getPath());
        return new ChromeDriver();
    }

    @AfterClass
    public static void stopClient() {
        browser.quit();
    }

    @Before
    public void goToFrontPage() {
        browser.manage().deleteAllCookies();
        browser.get(server.getURI().toString());
        //wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Products")));
    }

    @Test
    public void shouldSeeCurrentOrders() throws Exception {
        Order order = OrderRepositoryTest.sampleOrder();
        orderRepository.insert(order);

        List<String> orders = browser.findElement(By.id("ordersList"))
            .findElements(By.tagName("li"))
            .stream().map(e -> e.getText()).collect(Collectors.toList());

        assertThat(orders).contains("Order: " + order.getTitle());
    }

    @Test
    public void shouldAddProduct() throws Exception {
        browser.findElement(By.linkText("Products")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("addProduct")));
        browser.findElement(By.id("addProduct")).click();

        browser.findElement(By.name("product[price]")).sendKeys("123");

        String productTitle = SampleData.sampleString(4);
        WebElement titleField = browser.findElement(By.name("product[title]"));
        titleField.clear();
        titleField.sendKeys(productTitle);
        titleField.submit();

        browser.findElement(By.id("products"));

        assertThat(productRepository.list()).extracting("title")
            .contains(productTitle);
    }

    @Test
    public void shouldInsertNewOrders() throws Exception {
        Product product = ProductRepositoryTest.sampleProduct();
        productRepository.insert(product);

        browser.findElement(By.id("addOrder")).click();

        String orderTitle = OrderRepositoryTest.sampleOrder().getTitle();

        WebElement titleField = browser.findElement(By.name("order[title]"));
        titleField.clear();
        titleField.sendKeys(orderTitle);

        browser.findElement(By.id("addOrderLine")).click();

        browser.findElement(By.cssSelector("#orderLines .productSelect"))
            .findElement(optionWithText(product.getTitle()))
            .click();
        browser.findElement(By.cssSelector("#orderLines input[name='order[orderlines][][amount]']"))
            .sendKeys("120");

        titleField.submit();

        browser.findElement(By.id("ordersList"));

        assertThat(orderRepository.list().stream().map(o -> o.getTitle()).collect(Collectors.toList()))
            .contains(orderTitle);
    }

    private By optionWithText(String optionText) {
        return By.xpath("option[text() = '" + optionText + "']");
    }

}
