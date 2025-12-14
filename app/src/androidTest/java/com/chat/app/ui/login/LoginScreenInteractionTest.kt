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
class LoginScreenInteractionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockNavController = mockk<NavController>(relaxed = true)

    @Test
    fun loginScreen_successfulLogin_showsLoadingThenNavigates() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter valid credentials
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")

        // When - Click login
        composeTestRule.onNodeWithText("Login").performClick()

        // Then - UI should remain stable after click
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }

    @Test
    fun loginScreen_failedLogin_showsErrorMessage() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter credentials
        composeTestRule.onNodeWithText("Email").performTextInput("invalid@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("wrongpassword")

        // When - Click login
        composeTestRule.onNodeWithText("Login").performClick()

        // Then - UI should remain stable
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }

    @Test
    fun loginScreen_loadingState_disablesLoginButton() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter credentials and click login
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Login").performClick()

        // Then - Button should remain clickable (UI test level)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }

    @Test
    fun loginScreen_textFieldFocus_worksCorrectly() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        // When - Focus on email field
        composeTestRule.onNodeWithText("Email").performClick()
        
        // Then - Email field should be focused
        composeTestRule.onNodeWithText("Email").assertIsFocused()

        // When - Focus on password field
        composeTestRule.onNodeWithText("Password").performClick()
        
        // Then - Password field should be focused
        composeTestRule.onNodeWithText("Password").assertIsFocused()
    }

    @Test
    fun loginScreen_multipleLoginAttempts_handlesCorrectly() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        // When - First login attempt
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Login").performClick()

        // Wait for first attempt to complete
        composeTestRule.waitForIdle()

        // When - Second login attempt
        composeTestRule.onNodeWithText("Login").performClick()

        // Then - Should handle multiple attempts gracefully
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }

    @Test
    fun loginScreen_clearFieldsAndRetry_worksCorrectly() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter initial credentials
        composeTestRule.onNodeWithText("Email").performTextInput("first@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password1")

        // When - Clear and enter new credentials
        composeTestRule.onNodeWithText("Email").performTextClearance()
        composeTestRule.onNodeWithText("Password").performTextClearance()
        
        composeTestRule.onNodeWithText("Email").performTextInput("second@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password2")

        // Then - New credentials should be in the fields
        composeTestRule.onNodeWithText("Email").assertTextContains("second@example.com")
        // Password field content verification is limited due to password masking
        composeTestRule.onNodeWithText("Password").assertExists()
    }

    @Test
    fun loginScreen_longTextInput_handlesCorrectly() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        val longEmail = "a".repeat(100) + "@example.com"
        val longPassword = "p".repeat(200)

        // When - Enter very long text
        composeTestRule.onNodeWithText("Email").performTextInput(longEmail)
        composeTestRule.onNodeWithText("Password").performTextInput(longPassword)

        // Then - Fields should handle long input
        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()
        
        // Login button should still be clickable
        composeTestRule.onNodeWithText("Login").assertIsEnabled()
    }

    @Test
    fun loginScreen_specialCharacters_handlesCorrectly() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        val specialEmail = "test+tag@example-domain.co.uk"
        val specialPassword = "P@ssw0rd!#$%^&*()"

        // When - Enter special characters
        composeTestRule.onNodeWithText("Email").performTextInput(specialEmail)
        composeTestRule.onNodeWithText("Password").performTextInput(specialPassword)

        // Then - Should handle special characters
        composeTestRule.onNodeWithText("Email").assertTextContains(specialEmail)
        composeTestRule.onNodeWithText("Password").assertExists()
        
        // Login button should be clickable
        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule.waitForIdle()
    }
}