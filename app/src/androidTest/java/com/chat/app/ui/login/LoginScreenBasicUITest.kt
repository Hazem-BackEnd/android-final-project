package com.chat.app.ui.login

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chat.app.ui.theme.ChatappTheme
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenBasicUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockNavController = mockk<NavController>(relaxed = true)

    @Test
    fun loginScreen_displaysAllRequiredElements() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Verify all essential UI elements are displayed
        composeTestRule.onNodeWithContentDescription("App Icon").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Register").assertIsDisplayed()
        composeTestRule.onNodeWithText("Forgot Password?").assertIsDisplayed()
    }

    @Test
    fun loginScreen_textInputFields_acceptInput() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        val testEmail = "test@example.com"
        val testPassword = "password123"

        // When - Enter text in fields
        composeTestRule.onNodeWithText("Email").performTextInput(testEmail)
        composeTestRule.onNodeWithText("Password").performTextInput(testPassword)

        // Then - Verify text was entered (email field should show the text)
        composeTestRule.onNodeWithText("Email").assertTextContains(testEmail)
        // Password field content is masked, so we just verify it exists
        composeTestRule.onNodeWithText("Password").assertExists()
    }

    @Test
    fun loginScreen_buttons_areClickable() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Verify all buttons are clickable
        composeTestRule.onNodeWithText("Login").assertHasClickAction()
        composeTestRule.onNodeWithText("Register").assertHasClickAction()
        composeTestRule.onNodeWithText("Forgot Password?").assertHasClickAction()

        // When - Click buttons to ensure they respond
        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule.onNodeWithText("Register").performClick()
        composeTestRule.onNodeWithText("Forgot Password?").performClick()

        // Then - Buttons should still exist after clicking
        composeTestRule.onNodeWithText("Login").assertExists()
        composeTestRule.onNodeWithText("Register").assertExists()
        composeTestRule.onNodeWithText("Forgot Password?").assertExists()
    }

    @Test
    fun loginScreen_passwordField_masksInput() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        val secretPassword = "mysecretpassword"

        // When - Enter password
        composeTestRule.onNodeWithText("Password").performTextInput(secretPassword)

        // Then - Password text should not be visible as plain text
        composeTestRule.onNodeWithText(secretPassword).assertDoesNotExist()
        // But the password field should exist
        composeTestRule.onNodeWithText("Password").assertExists()
    }

    @Test
    fun loginScreen_fieldsAreEnabled() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Verify fields are enabled and can receive input
        composeTestRule.onNodeWithText("Email").assertIsEnabled()
        composeTestRule.onNodeWithText("Password").assertIsEnabled()
        composeTestRule.onNodeWithText("Login").assertIsEnabled()
        composeTestRule.onNodeWithText("Register").assertIsEnabled()
    }
}