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
class RegisterScreenBasicUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockNavController = mockk<NavController>(relaxed = true)

    @Test
    fun registerScreen_displaysAllRequiredElements() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Verify all essential UI elements are displayed
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Default Profile Icon").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Select Image").assertIsDisplayed()
        composeTestRule.onNodeWithText("Username").assertIsDisplayed()
        composeTestRule.onNodeWithText("Phone").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
    }

    @Test
    fun registerScreen_textInputFields_acceptInput() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        val testUsername = "testuser"
        val testPhone = "1234567890"
        val testEmail = "test@example.com"
        val testPassword = "password123"

        // When - Enter text in fields
        composeTestRule.onNodeWithText("Username").performTextInput(testUsername)
        composeTestRule.onNodeWithText("Phone").performTextInput(testPhone)
        composeTestRule.onNodeWithText("Email").performTextInput(testEmail)
        composeTestRule.onNodeWithText("Password").performTextInput(testPassword)

        // Then - Verify text was entered
        composeTestRule.onNodeWithText("Username").assertTextContains(testUsername)
        composeTestRule.onNodeWithText("Phone").assertTextContains(testPhone)
        composeTestRule.onNodeWithText("Email").assertTextContains(testEmail)
        // Password field content is masked, so we just verify it exists
        composeTestRule.onNodeWithText("Password").assertExists()
    }

    @Test
    fun registerScreen_buttons_areClickable() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Verify all buttons are clickable (without performing clicks to avoid timing issues)
        composeTestRule.onNodeWithContentDescription("Back").assertHasClickAction()
        composeTestRule.onNodeWithContentDescription("Select Image").assertHasClickAction()
        composeTestRule.onNodeWithText("Create Account").assertHasClickAction()

        // Verify buttons exist and are displayed
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Select Image").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
    }

    @Test
    fun registerScreen_passwordField_masksInput() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
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
    fun registerScreen_fieldsAreEnabled() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Verify input fields are enabled and can receive input
        composeTestRule.onNodeWithText("Username").assertIsEnabled()
        composeTestRule.onNodeWithText("Phone").assertIsEnabled()
        composeTestRule.onNodeWithText("Email").assertIsEnabled()
        composeTestRule.onNodeWithText("Password").assertIsEnabled()
        
        // Create Account button is disabled initially (validation requirement)
        composeTestRule.onNodeWithText("Create Account").assertIsNotEnabled()
        
        // When - Fill all fields with valid data
        composeTestRule.onNodeWithText("Username").performTextInput("validuser")
        composeTestRule.onNodeWithText("Phone").performTextInput("1234567890")
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        
        // Then - Create Account button should be enabled
        composeTestRule.onNodeWithText("Create Account").assertIsEnabled()
    }

    @Test
    fun registerScreen_profileImageSection_isDisplayed() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Verify profile image section is displayed
        composeTestRule.onNodeWithContentDescription("Default Profile Icon").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Select Image").assertIsDisplayed()
        
        // Camera icon should be clickable
        composeTestRule.onNodeWithContentDescription("Select Image").assertHasClickAction()
    }

    @Test
    fun registerScreen_topAppBar_isDisplayed() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Verify top app bar with back button is displayed
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").assertHasClickAction()
    }

    @Test
    fun registerScreen_createAccountButton_requiresAllFields() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Button should be disabled initially
        composeTestRule.onNodeWithText("Create Account").assertIsNotEnabled()
        
        // When - Fill all fields with valid data
        composeTestRule.onNodeWithText("Username").performTextInput("testuser")
        composeTestRule.onNodeWithText("Phone").performTextInput("1234567890")
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        
        // Then - Button should be enabled
        composeTestRule.onNodeWithText("Create Account").assertIsEnabled()
        
        // When - Clear one field
        composeTestRule.onNodeWithText("Username").performTextClearance()
        
        // Then - Button should be disabled again
        composeTestRule.onNodeWithText("Create Account").assertIsNotEnabled()
    }
}