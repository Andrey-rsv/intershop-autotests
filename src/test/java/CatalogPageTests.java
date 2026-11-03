import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.util.List;

public class CatalogPageTests extends BaseTest {
    private static final String CATALOG_URL = "https://intershop5.skillbox.ru/product-category/catalog/";

    @Test
    public void testCatalogKeyElementsAreDisplayed() {
        driver.get(CATALOG_URL);
        Assert.assertTrue("Заголовок каталога не отображается",
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.entry-title"))).isDisplayed());
        List<WebElement> products = driver.findElements(By.cssSelector("li.product"));
        Assert.assertFalse("Товары не отображаются", products.isEmpty());
        Assert.assertTrue("Пагинация не отображается",
                driver.findElement(By.cssSelector("nav.woocommerce-pagination")).isDisplayed());
        Assert.assertTrue("Сортировка не отображается",
                driver.findElement(By.cssSelector("select.orderby")).isDisplayed());
    }

    @Test
    public void testPaginationWorks() {
        driver.get(CATALOG_URL);
        String firstProductName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("li.product a.collection_title"))).getText();
        WebElement page2 = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("nav.woocommerce-pagination a.page-numbers[href*='/page/2/']")));
        page2.click();
        wait.until(ExpectedConditions.urlContains("/page/2/"));
        String newFirstProductName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("li.product a.collection_title"))).getText();
        Assert.assertNotEquals("Товары на второй странице совпадают с первой", firstProductName, newFirstProductName);
    }

    @Test
    public void testNavigateToProductCard() {
        driver.get(CATALOG_URL);
        WebElement firstProductLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li.product a.collection_title")));
        String productName = firstProductLink.getText();
        firstProductLink.click();
        wait.until(ExpectedConditions.urlContains("/product/"));
        Assert.assertTrue("Название товара на странице не совпадает",
                driver.findElement(By.cssSelector("h1.product_title")).getText().contains(productName));
    }

    @Test
    public void testAddProductToCartFromCatalog() {
        driver.get(CATALOG_URL);
        WebElement addToCartButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a.add_to_cart_button"))
        );
        addToCartButton.click();
        try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
        driver.get("https://intershop5.skillbox.ru/cart/");
        List<WebElement> cartItems = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("tr.cart_item")));
        Assert.assertFalse("Товар не добавился в корзину", cartItems.isEmpty());
    }

    @Test
    public void testBreadcrumbsAreDisplayed() {
        driver.get(CATALOG_URL);
        Assert.assertTrue("Хлебные крошки не отображаются",
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.woocommerce-breadcrumb"))).isDisplayed());
        String breadcrumbText = driver.findElement(By.cssSelector("div.woocommerce-breadcrumb")).getText();
        Assert.assertTrue("В крошках нет слова 'Каталог'", breadcrumbText.contains("Каталог"));
    }

    @Test
    public void testNavigateToSubcategory() {
        driver.get(CATALOG_URL);
        WebElement categoryLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("ul.product-categories a[href*='/appliances/']")));
        categoryLink.click();
        wait.until(ExpectedConditions.urlContains("appliances"));
        boolean isCategoryPage = driver.getTitle().toLowerCase().contains("бытовая техника")
                || driver.findElement(By.cssSelector("div.woocommerce-breadcrumb")).getText().toLowerCase().contains("бытовая техника");
        Assert.assertTrue("Мы не попали на страницу категории 'Бытовая техника'", isCategoryPage);
    }
}