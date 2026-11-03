import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class MainPageTests extends BaseTest {
    private static final String BASE_URL = "https://intershop5.skillbox.ru/";

    @Test
    public void testMainPageKeyElementsAreDisplayed() {
        driver.get(BASE_URL);
        Assert.assertTrue("Логотип не отображается",
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.site-logo"))).isDisplayed());
        Assert.assertTrue("Поле поиска не отображается",
                driver.findElement(By.cssSelector("input.search-field")).isDisplayed());
        Assert.assertTrue("Кнопка поиска не отображается",
                driver.findElement(By.cssSelector("button.searchsubmit")).isDisplayed());
        Assert.assertTrue("Меню навигации не отображается",
                driver.findElement(By.id("site-navigation")).isDisplayed());
        Assert.assertTrue("Ссылка на корзину не отображается",
                driver.findElement(By.cssSelector("a[href*='cart/']")).isDisplayed());
    }

    @Test
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        String query = "гитара";
        WebElement searchField = driver.findElement(By.cssSelector("input.search-field"));
        searchField.clear();
        searchField.sendKeys(query);
        driver.findElement(By.cssSelector("button.searchsubmit")).click();
        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(BASE_URL)));
        String currentUrl = driver.getCurrentUrl().toLowerCase();
        Assert.assertTrue("Результаты поиска не отображаются",
                currentUrl.contains("product") || currentUrl.contains("?s="));
    }

    @Test
    public void testNavigateToCatalog() {
        driver.get(BASE_URL);
        WebElement catalogLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='product-category/catalog/']"))
        );
        catalogLink.click();
        wait.until(ExpectedConditions.urlContains("product-category"));
        Assert.assertFalse("Заголовок каталога не отображается",
                driver.findElement(By.cssSelector("h1.entry-title")).getText().isEmpty());
    }

    @Test
    public void testAddProductToCartFromCatalog() {
        driver.get("https://intershop5.skillbox.ru/product-category/catalog/");
        WebElement addToCartButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".add_to_cart_button, .single_add_to_cart_button"))
        );
        addToCartButton.click();
        try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
        driver.get("https://intershop5.skillbox.ru/cart/");
        Assert.assertTrue("Товар не добавился в корзину",
                driver.findElements(By.cssSelector("tr.cart_item, .cart_item")).size() > 0);
    }

    @Test
    public void testCartPageOpens() {
        driver.get("https://intershop5.skillbox.ru/cart/");
        wait.until(ExpectedConditions.urlContains("cart"));
        WebElement cartTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".entry-title")));
        Assert.assertTrue("Страница корзины не загрузилась", cartTitle.getText().contains("Корзина"));
    }

    @Test
    public void testSaleSectionIsDisplayed() {
        driver.get(BASE_URL);
        Assert.assertTrue("Блок распродажи не отображается",
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accesspress_store_product-2"))).isDisplayed());
    }
}