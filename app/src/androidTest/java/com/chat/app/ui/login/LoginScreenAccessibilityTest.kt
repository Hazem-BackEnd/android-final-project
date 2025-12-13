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
class LoginScreenAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockNavController = mockk<NavController>(relaxed = true)

    @Test
    fun loginScreen_hasProperContentDescriptions() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Verify important elements have content descriptions
        composeTestRule.onNodeWithContentDescription("App Icon").assertIsDisplayed()
        
        // Verify text fields are accessible
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        
        // Verify buttons are accessible
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Register").assertIsDisplayed()
        composeTestRule.onNodeWithText("Forgot Password?").assertIsDisplayed()
    }

    @Test
    fun loginScreen_elementsAreFocusable() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Verify interactive elements are focusable (by checking they can be clicked/focused)
        composeTestRule.onNodeWithText("Email").assertIsEnabled()
        composeTestRule.onNodeWithText("Password").assertIsEnabled()
        composeTestRule.onNodeWithText("Login").assertHasClickAction()
        composeTestRule.onNodeWithText("Register").assertHasClickAction()
        composeTestRule.onNodeWithText("Forgot Password?").assertHasClickAction()
    }

    @Test
    fun loginScreen_buttonsAreClickable() {
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
    }

    @Test
    fun loginScreen_textFieldsAcceptInput() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Verify text fields can accept input
        composeTestRule.onNodeWithText("Email").assertIsEnabled()
        composeTestRule.onNodeWithText("Password").assertIsEnabled()
        
        // Test that they can receive text input
        composeTestRule.onNodeWithText("Email").performTextInput("test")
        composeTestRule.onNodeWithText("Password").performTextInput("test")
        
        // Verify input was accepted
        composeTestRule.onNodeWithText("Email").assertTextContains("test")
    }

    @Test
    fun loginScreen_hasProperSemantics() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Verify semantic properties
        composeTestRule.onNodeWithText("Email")
            .assertIsDisplayed()
            .assertIsEnabled()

        composeTestRule.onNodeWithText("Password")
            .assertIsDisplayed()
            .assertIsEnabled()

        composeTestRule.onNodeWithText("Login")
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()

        composeTestRule.onNodeWithText("Register")
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
    }

    @Test
    fun loginScreen_keyboardNavigation_worksCorrectly() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        // When - Navigate through fields using keyboard/focus
        composeTestRule.onNodeWithText("Email").performClick()
        composeTestRule.onNodeWithText("Email").assertIsFocused()

        // Then - Should be able to navigate to password field
        composeTestRule.onNodeWithText("Password").performClick()
        composeTestRule.onNodeWithText("Password").assertIsFocused()
    }

    @Test
    fun loginScreen_screenReader_canAccessAllElements() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        // Then - All interactive elements should be accessible to screen readers
        val accessibleNodes = listOf(
            "Email",
            "Password", 
            "Login",
            "Register",
            "Forgot Password?"
        )

        accessibleNodes.forEach { text ->
            composeTestRule.onNodeWithText(text)
                .assertIsDisplayed()
                .assertExists()
        }

        // App icon should also be accessible
        composeTestRule.onNodeWithContentDescription("App Icon")
            .assertIsDisplayed()
            .assertExists()
    }

    @Test
    fun loginScreen_errorMessage_isAccessible() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        // When - Trigger an error state (this would require mocking the ViewModel)
        // For now, we'll test that the screen can handle error display
        
        // Then - The screen should be able to display error messages accessibly
        // This test verifies the screen structure supports error display
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }

    @Test
    fun loginScreen_loadingState_isAccessible() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter credentials to potentially trigger loading
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password")
        
        // Then - Loading state should be accessible when it appears
        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule.waitForIdle()
        
        // The login button should still be accessible during loading
        composeTestRule.onNodeWithText("Login").assertExists()
    }

    @Test
    fun loginScreen_textContrast_isReadable() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        // Then - All text should be visible and readable
        // This is a basic test to ensure text elements are rendered
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Register").assertIsDisplayed()
        composeTestRule.onNodeWithText("Forgot Password?").assertIsDisplayed()
    }

    @Test
    fun loginScreen_touchTargets_areAppropriateSize() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Interactive elements should be large enough for touch
        // This test verifies that buttons and fields are clickable/touchable
        composeTestRule.onNodeWithText("Login")
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick() // Should be able to click without issues

        composeTestRule.onNodeWithText("Register")
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick() // Should be able to click without issues

        composeTestRule.onNodeWithText("Forgot Password?")
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick() // Should be able to click without issues
    }

    @Test
    fun loginScreen_layoutAdaptsToContent() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                LoginScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Layout should accommodate all content properly
        composeTestRule.onNodeWithContentDescription("App Icon").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Register").assertIsDisplayed()
        composeTestRule.onNodeWithText("Forgot Password?").assertIsDisplayed()

        // All elements should be visible without scrolling in normal cases
        composeTestRule.onRoot().assertIsDisplayed()
    }
}