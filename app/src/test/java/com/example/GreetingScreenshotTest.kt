package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.Product
import com.example.ui.LoginUiState
import com.example.ui.details.ProductDetailsScreen
import com.example.ui.login.LoginScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun login_screen_screenshot() {
        composeTestRule.setContent {
            MyApplicationTheme {
                LoginScreen(
                    state = LoginUiState(userIdInput = "sanjay2007"),
                    onUserIdChanged = {},
                    onPasswordChanged = {},
                    onTogglePasswordMask = {},
                    onLoginSubmitted = {},
                    onFillDemoCredentials = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/login_screen.png")
    }

    @Test
    fun product_details_screenshot() {
        val demoProduct = Product(
            id = "1",
            model = "Nova2Ultra",
            color = "Black",
            memory = "6+128",
            ean = "8906202671265",
            sku = "AINT68BLA5",
            price = "29999",
            sarValue = "Body - 1.280, Head - 1.397"
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                ProductDetailsScreen(
                    product = demoProduct,
                    onScanNext = {},
                    onRefreshMaster = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/product_details.png")
    }
}

