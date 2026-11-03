import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.util.List;

public class AuthTests extends BaseTest {
    private static final String REGISTER_URL = "https://intershop5.skillbox.ru/register/";
    private static final String LOGIN_URL = "https://intershop5.skillbox.ru/my-account/";

    private String generateUniqueEmail() {
        return "t" + (System.currentTimeMillis() % 1000000) + "@t.com";
    }

    private String generateUniqueUsername() {
        return "U" + (System.currentTimeMillis() % 10000);
    }

    private void registerUser(String email, String password) {
        driver.get(REGISTER_URL);

        WebElement regUsername = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("reg_username")));
        regUsername.clear();
        regUsername.sendKeys(generateUniqueUsername());

        WebElement regEmail = driver.findElement(By.id("reg_email"));
        regEmail.clear();
        regEmail.sendKeys(email);

        WebElement regPassword = driver.findElement(By.id("reg_password"));
        regPassword.clear();
        regPassword.sendKeys(password);

        List<WebElement> privacyCheckbox = driver.findElements(By.id("reg_privacy_policy"));
        if (!privacyCheckbox.isEmpty()) {
            privacyCheckbox.get(0).click();
        }

        driver.findElement(By.cssSelector("button[name='register']")).click();

        try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }

        List<WebElement> errors = driver.findElements(By.cssSelector("ul.woocommerce-error"));
        if (!errors.isEmpty()) {
            Assert.fail("Ошибка при регистрации: " + errors.get(0).getText());
        }
    }

    @Test
    public void testSuccessfulRegistration() {
        String email = generateUniqueEmail();
        String password = "StrongPass123!";

        registerUser(email, password);

        driver.get("https://intershop5.skillbox.ru/");
        Assert.assertFalse("Регистрация не удалась: ссылка 'Выйти' не найдена",
                driver.findElements(By.cssSelector("a.logout")).isEmpty());
    }

    @Test
    public void testSuccessfulLogin() {
        String email = generateUniqueEmail();
        String password = "StrongPass123!";

        registerUser(email, password);
        driver.manage().deleteAllCookies(); // Выходим, очищая куки
        driver.get(LOGIN_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameField.clear();
        usernameField.sendKeys(email);

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.clear();
        passwordField.sendKeys(password);

        driver.findElement(By.cssSelector("button[name='login']")).click();

        driver.get("https://intershop5.skillbox.ru/");
        Assert.assertFalse("Вход не выполнен: ссылка 'Выйти' не найдена",
                driver.findElements(By.cssSelector("a.logout")).isEmpty());
    }

    @Test
    public void testLoginWithInvalidPassword() {
        String email = generateUniqueEmail();
        String correctPassword = "StrongPass123!";

        registerUser(email, correctPassword);
        driver.manage().deleteAllCookies(); // Выходим, очищая куки
        driver.get(LOGIN_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));

        WebElement usernameField = driver.findElement(By.id("username"));
        usernameField.clear();
        usernameField.sendKeys(email);

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.clear();
        passwordField.sendKeys("WrongPassword");

        driver.findElement(By.cssSelector("button[name='login']")).click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.woocommerce-error")));

        // Ищем просто слово "неверный" или "ошибка" в тексте, так как порядок слов может быть разным
        Assert.assertTrue("Сообщение об ошибке не содержит текст о неверном пароле. Текст ошибки: " + errorMessage.getText(),
                errorMessage.getText().toLowerCase().contains("неверный") ||
                        errorMessage.getText().toLowerCase().contains("ошибка"));
    }

    @Test
    public void testRegistrationWithEmptyFields() {
        driver.get(REGISTER_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("reg_username")));

        driver.findElement(By.cssSelector("button[name='register']")).click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.woocommerce-error")));
        Assert.assertTrue("Нет сообщения об ошибке при пустых полях", errorMessage.isDisplayed());
    }
}