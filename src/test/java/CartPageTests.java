import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.util.List;

public class CartPageTests extends BaseTest {
    private static final String CART_URL = "https://intershop5.skillbox.ru/cart/";
    private static final String CATALOG_URL = "https://intershop5.skillbox.ru/product-category/catalog/";

    @Before
    public void addProductToCart() {
        // Добавляем товар через каталог, чтобы корзина была не пуста
        driver.get(CATALOG_URL);
        WebElement addToCartButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a.add_to_cart_button"))
        );
        addToCartButton.click();
        try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    @Test
    public void testProductIsDisplayedInCart() {
        driver.get(CART_URL);
        List<WebElement> cartItems = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("tr.cart_item"))
        );
        Assert.assertFalse("Товар не отображается в корзине", cartItems.isEmpty());
    }

    @Test
    public void testApplyCouponCode() {
        driver.get(CART_URL);
        WebElement couponField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("coupon_code"))
        );
        couponField.clear();
        couponField.sendKeys("sert500");
        driver.findElement(By.cssSelector("button[name='apply_coupon']")).click();

        WebElement message = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".woocommerce-message"))
        );
        Assert.assertTrue("Купон не применён", message.getText().contains("Купон") || message.getText().contains("Coupon"));

        Assert.assertTrue("Скидка не отображается",
                driver.findElements(By.cssSelector(".cart-discount, .discount")).size() > 0);
    }

    @Test
    public void testRemoveProductFromCart() {
        driver.get(CART_URL);
        WebElement removeLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a.remove"))
        );
        removeLink.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart-empty")));
        Assert.assertTrue("Корзина не стала пустой",
                driver.findElement(By.cssSelector(".cart-empty")).getText().contains("Корзина пуста"));
    }

    @Test
    public void testCheckoutButtonRedirectsToCheckout() {
        driver.get(CART_URL);
        WebElement checkoutButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a.checkout-button"))
        );
        checkoutButton.click();
        wait.until(ExpectedConditions.urlContains("checkout"));
        Assert.assertTrue("Не открылась страница оформления заказа",
                driver.getCurrentUrl().contains("checkout"));
    }

    @Test
    public void testCartTotalsAreDisplayed() {
        driver.get(CART_URL);

        // Проверяем, что отображается строка с итоговой суммой (К оплате)
        // По скрину у неё стандартный класс .order-total
        Assert.assertTrue("Итоговая сумма не отображается",
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".order-total"))).isDisplayed());

        // Проверяем наличие кнопки "Оформить заказ"
        Assert.assertTrue("Кнопка 'Оформить заказ' не отображается",
                driver.findElement(By.cssSelector("a.checkout-button")).isDisplayed());
    }
}