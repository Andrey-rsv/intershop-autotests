import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

public class CheckoutPageTests extends BaseTest {
    private static final String REGISTER_URL = "https://intershop5.skillbox.ru/register/";
    private static final String CART_URL = "https://intershop5.skillbox.ru/cart/";
    private static final String CATALOG_URL = "https://intershop5.skillbox.ru/product-category/catalog/";
    private static final String CHECKOUT_URL = "https://intershop5.skillbox.ru/checkout/";

    private String userEmail;

    @Before
    public void setupCheckout() {
        userEmail = "t" + (System.currentTimeMillis() % 1000000) + "@t.com";
        String uniqueUsername = "U" + (System.currentTimeMillis() % 10000);

        // 1. Авторизуемся
        driver.get(REGISTER_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("reg_username")));

        driver.findElement(By.id("reg_username")).sendKeys(uniqueUsername);
        driver.findElement(By.id("reg_email")).sendKeys(userEmail);
        driver.findElement(By.id("reg_password")).sendKeys("StrongPass123!");
        driver.findElement(By.cssSelector("button[name='register']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.logout")));

        // 2. Добавляем товар в корзину
        driver.get(CATALOG_URL);
        WebElement addToCartButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a.add_to_cart_button"))
        );
        addToCartButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".added_to_cart, .woocommerce-message")));
    }

    @Test
    public void testSuccessfulCheckout() {
        driver.get(CHECKOUT_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("billing_first_name")));

        driver.findElement(By.id("billing_first_name")).sendKeys("Иван");
        driver.findElement(By.id("billing_last_name")).sendKeys("Петров");

        // Выбираем Россию
        Select countrySelect = new Select(driver.findElement(By.id("billing_country")));
        countrySelect.selectByValue("RU");
        try { Thread.sleep(1000); } catch (InterruptedException e) {} // Ждем обновления регионов

        driver.findElement(By.id("billing_address_1")).sendKeys("ул. Пушкина, д. 10");
        driver.findElement(By.id("billing_city")).sendKeys("Москва");

        // ВОТ ОНО - недостающее поле Области!
        driver.findElement(By.id("billing_state")).sendKeys("Московская");

        driver.findElement(By.id("billing_postcode")).sendKeys("123456");
        driver.findElement(By.id("billing_phone")).sendKeys("+79991234567");

        WebElement emailField = driver.findElement(By.id("billing_email"));
        emailField.clear();
        emailField.sendKeys(userEmail);

        // Ставим галочку согласия (если есть)
        try {
            WebElement termsCheckbox = driver.findElement(By.id("terms"));
            if (!termsCheckbox.isSelected()) {
                termsCheckbox.click();
            }
        } catch (org.openqa.selenium.NoSuchElementException e) {}

        // Кликаем через JavaScript, чтобы обойти StaleElementException
        WebElement placeOrderButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("place_order")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", placeOrderButton);

        // Ждем появления сообщения об успешном заказе
        WebElement successMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".woocommerce-thankyou-order-received"))
        );
        Assert.assertTrue("Заказ не был оформлен", successMessage.isDisplayed());
    }

    @Test
    public void testCheckoutWithEmptyFields() {
        driver.get(CHECKOUT_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("billing_first_name")));

        // Кликаем через JavaScript
        WebElement placeOrderButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("place_order")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", placeOrderButton);

        WebElement errorBlock = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.woocommerce-error"))
        );
        Assert.assertTrue("Не появилось сообщение об ошибке при пустых полях", errorBlock.isDisplayed());
    }

    @Test
    public void testOrderTotalIsDisplayed() {
        driver.get(CHECKOUT_URL);

        WebElement orderTotal = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("tr.order-total"))
        );
        Assert.assertTrue("Итоговая сумма не отображается на странице оформления заказа", orderTotal.isDisplayed());
    }

    @Test
    public void testCheckoutButtonRedirectsFromCart() {
        driver.get(CART_URL);

        WebElement checkoutButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a.checkout-button"))
        );
        checkoutButton.click();

        wait.until(ExpectedConditions.urlContains("checkout"));
        Assert.assertTrue("Не открылась страница оформления заказа",
                driver.getCurrentUrl().contains("checkout"));
    }
}