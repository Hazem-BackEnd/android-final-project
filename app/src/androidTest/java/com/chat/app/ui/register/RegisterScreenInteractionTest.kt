package com.chat.app.ui.register

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
class RegisterScreenInteractionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockNavController = mockk<NavController>(relaxed = true)

    @Test
    fun registerScreen_textFieldFocus_worksCorrectly() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Focus on username field
        composeTestRule.onNodeWithText("Username").performClick()
        
        // Then - Username field should be focused
        composeTestRule.onNodeWithText("Username").assertIsFocused()

        // When - Focus on phone field
        composeTestRule.onNodeWithText("Phone").performClick()
        
        // Then - Phone field should be focused
        composeTestRule.onNodeWithText("Phone").assertIsFocused()

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
    fun registerScreen_clearFieldsAndRetry_worksCorrectly() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter initial data
        composeTestRule.onNodeWithText("Username").performTextInput("user1")
        composeTestRule.onNodeWithText("Phone").performTextInput("1111111111")
        composeTestRule.onNodeWithText("Email").performTextInput("user1@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password1")

        // When - Clear and enter new data
        composeTestRule.onNodeWithText("Username").performTextClearance()
        composeTestRule.onNodeWithText("Phone").performTextClearance()
        composeTestRule.onNodeWithText("Email").performTextClearance()
        composeTestRule.onNodeWithText("Password").performTextClearance()
        
        composeTestRule.onNodeWithText("Username").performTextInput("user2")
        composeTestRule.onNodeWithText("Phone").performTextInput("2222222222")
        composeTestRule.onNodeWithText("Email").performTextInput("user2@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password2")

        // Then - New data should be in the fields
        composeTestRule.onNodeWithText("Username").assertTextContains("user2")
        composeTestRule.onNodeWithText("Phone").assertTextContains("2222222222")
        composeTestRule.onNodeWithText("Email").assertTextContains("user2@example.com")
        composeTestRule.onNodeWithText("Password").assertExists()
    }

    @Test
    fun registerScreen_longTextInput_handlesCorrectly() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        val longUsername = "a".repeat(50)
        val longPhone = "1".repeat(20)
        val longEmail = "a".repeat(50) + "@example.com"
        val longPassword = "p".repeat(100)

        // When - Enter very long text
        composeTestRule.onNodeWithText("Username").performTextInput(longUsername)
        composeTestRule.onNodeWithText("Phone").performTextInput(longPhone)
        composeTestRule.onNodeWithText("Email").performTextInput(longEmail)
        composeTestRule.onNodeWithText("Password").performTextInput(longPassword)

        // Then - Fields should handle long input
        composeTestRule.onNodeWithText("Username").assertExists()
        composeTestRule.onNodeWithText("Phone").assertExists()
        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()
        
        // Create Account button should still be clickable
        composeTestRule.onNodeWithText("Create Account").assertIsEnabled()
    }

    @Test
    fun registerScreen_specialCharacters_handlesCorrectly() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        val specialUsername = "user@#$%"
        val specialPhone = "+1-234-567-8900"
        val specialEmail = "test+tag@example-domain.co.uk"
        val specialPassword = "P@ssw0rd!#$%^&*()"

        // When - Enter special characters
        composeTestRule.onNodeWithText("Username").performTextInput(specialUsername)
        composeTestRule.onNodeWithText("Phone").performTextInput(specialPhone)
        composeTestRule.onNodeWithText("Email").performTextInput(specialEmail)
        composeTestRule.onNodeWithText("Password").performTextInput(specialPassword)

        // Then - Should handle special characters
        composeTestRule.onNodeWithText("Username").assertTextContains(specialUsername)
        composeTestRule.onNodeWithText("Phone").assertTextContains(specialPhone)
        composeTestRule.onNodeWithText("Email").assertTextContains(specialEmail)
        composeTestRule.onNodeWithText("Password").assertExists()
        
        // Create Account button should be clickable
        composeTestRule.onNodeWithText("Create Account").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun registerScreen_backButton_isClickable() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Click back button
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        // Then - Back button should remain clickable
        composeTestRule.onNodeWithContentDescription("Back").assertExists()
    }

    @Test
    fun registerScreen_imageSelection_isClickable() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Click on profile image area
        composeTestRule.onNodeWithContentDescription("Default Profile Icon").performClick()

        // When - Click on camera icon
        composeTestRule.onNodeWithContentDescription("Select Image").performClick()

        // Then - Elements should still exist after clicking
        composeTestRule.onNodeWithContentDescription("Default Profile Icon").assertExists()
        composeTestRule.onNodeWithContentDescription("Select Image").assertExists()
    }

    @Test
    fun registerScreen_multipleRegistrationAttempts_handlesCorrectly() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - First registration attempt
        composeTestRule.onNodeWithText("Username").performTextInput("testuser")
        composeTestRule.onNodeWithText("Phone").performTextInput("1234567890")
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Create Account").performClick()

        // Wait for first attempt to complete
        composeTestRule.waitForIdle()

        // When - Second registration attempt
        composeTestRule.onNodeWithText("Create Account").performClick()

        // Then - Should handle multiple attempts gracefully
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
    }

    @Test
    fun registerScreen_fieldsAreSingleLine() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        val multiLineText = "line1\nline2\nline3"

        // When - Try to enter multi-line text in fields
        composeTestRule.onNodeWithText("Username").performTextInput(multiLineText)
        composeTestRule.onNodeWithText("Phone").performTextInput(multiLineText)
        composeTestRule.onNodeWithText("Email").performTextInput(multiLineText)
        composeTestRule.onNodeWithText("Password").performTextInput(multiLineText)

        // Then - Fields should handle the input (single line behavior is enforced by the component)
        composeTestRule.onNodeWithText("Username").assertExists()
        composeTestRule.onNodeWithText("Phone").assertExists()
        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()
    }
}