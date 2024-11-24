package gettingstarted;

import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;

public class RtCampAssignment {

    // Constants for Selectors
    private static final String URL = "https://www.saucedemo.com/";
    private static final String USERNAME_INPUT = "input[placeholder='Username']";
    private static final String PASSWORD_INPUT = "input[placeholder='Password']";
    private static final String LOGIN_BUTTON = "xpath=//input[@id='login-button']";
    private static final String INVENTORY_ITEMS = "div.inventory_item";
    private static final String SORT_DROPDOWN = "select.product_sort_container";
    private static final String ITEM_PRICE = "div.inventory_item_price";
    private static final String ADD_TO_CART_PREFIX = "#add-to-cart-";
	private static final boolean True = false;

    @Test
    public void loginTest() {
        try (Playwright playwright = Playwright.create()) {
            // Launch the browser with logging enabled
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(True)
                    .setSlowMo(500));
            Page page = browser.newPage();

            // Enable logging for console messages
            page.onConsoleMessage(message -> {
                System.out.println("Console message: " + message.text());
            });

            // Enable logging for network requests
            page.onRequest(request -> {
                System.out.println("Network Request: " + request.method() + " " + request.url());
            });

            // Enable logging for network responses
            page.onResponse(response -> {
                System.out.println("Network Response: " + response.status() + " " + response.url());
            });

            // Navigate to the webpage
            page.navigate(URL);
            Assert.assertTrue(page.title().contains("Swag Labs"), "Page title mismatch!");
            System.out.println("Page title: " + page.title());

            // Perform login
            login(page);

            // Verify inventory items before sorting
            List<String> itemsBeforeSort = getItems(page, "before sorting");

            // Sort items Z-A and verify
            sortItemsAndVerify(page, "za", "Z-A");

            // Sort items by Price: High to Low and verify
            sortItemsAndVerify(page, "hilo", "Price High to Low");

            // Add multiple items to cart
            addItemsToCart(page);

            // Navigate to shopping cart and proceed to checkout
            checkout(page);

            // Assert all items are displayed and the total price
            verifyCheckoutItems(page);

            // Finish the purchase process
            finishPurchase(page);

            // Logout and verify navigation to login page
            logout(page);

            // Close the browser
            browser.close();
        }
    }

    // Utility method to perform login
    private void login(Page page) {
        page.locator(USERNAME_INPUT).fill("standard_user");
        page.locator(PASSWORD_INPUT).fill("secret_sauce");
        page.locator(LOGIN_BUTTON).click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    // Utility method to get items list
    private List<String> getItems(Page page, String action) {
        System.out.println("Items displayed " + action + ":");
        List<String> items = page.locator(INVENTORY_ITEMS).allTextContents();
        printItems(items);
        return items;
    }

    // Utility method to sort items and verify
    private void sortItemsAndVerify(Page page, String optionValue, String sortType) {
        page.locator(SORT_DROPDOWN).selectOption(optionValue);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        System.out.println("Items displayed after " + sortType + " sorting:");
        List<String> sortedItems = page.locator(INVENTORY_ITEMS).allTextContents();
        printItems(sortedItems);
    }

    // Utility method to add items to the cart
    private void addItemsToCart(Page page) {
        String[] itemIds = {"sauce-labs-fleece-jacket", "sauce-labs-backpack", "sauce-labs-bolt-t-shirt",
                "test\\.allthethings\\(\\)-t-shirt\\-\\(red\\)", "sauce-labs-bike-light", "sauce-labs-onesie"};

        for (String itemId : itemIds) {
            page.locator(ADD_TO_CART_PREFIX + itemId).click();
        }
    }

    // Utility method to proceed to checkout
    private void checkout(Page page) {
        page.locator("#shopping_cart_container").click();
        page.locator("#checkout").click();
        page.locator("#first-name").fill("Parag");
        page.locator("#last-name").fill("Trivedi");
        page.locator("#postal-code").fill("411028");
        page.locator("#continue").click();
    }

    // Utility method to verify checkout items
    private void verifyCheckoutItems(Page page) {
        List<String> checkoutItems = page.locator(INVENTORY_ITEMS).allTextContents();
        printItems(checkoutItems);
    }

    // Utility method to finish the purchase
    private void finishPurchase(Page page) {
        page.locator("#finish").click();
    }

    // Utility method to perform logout and verify login page
    private void logout(Page page) {
        page.locator("#back-to-products").click();
        page.locator("#react-burger-menu-btn").click();
        page.locator("#logout_sidebar_link").click();
        Assert.assertTrue(page.title().contains("Swag Labs"), "Page title mismatch!");
        System.out.println("Page title: " + page.title());
    }

    // Utility method to print items
    private void printItems(List<String> items) {
        System.out.println("Total items: " + items.size());
        for (String item : items) {
            System.out.println(item);
        }
    }
}
