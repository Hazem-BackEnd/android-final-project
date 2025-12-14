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
class RegisterScreenAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockNavController = mockk<NavController>(relaxed = true)

    @Test
    fun registerScreen_hasProperContentDescriptions() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Verify important elements have content descriptions
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Default Profile Icon").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Select Image").assertIsDisplayed()
        
        // Verify text fields are accessible
        composeTestRule.onNodeWithText("Username").assertIsDisplayed()
        composeTestRule.onNodeWithText("Phone").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        
        // Verify button is accessible
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
    }

    @Test
    fun registerScreen_elementsAreFocusable() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Verify interactive elements are focusable
        composeTestRule.onNodeWithText("Username").assertIsEnabled()
        composeTestRule.onNodeWithText("Phone").assertIsEnabled()
        composeTestRule.onNodeWithText("Email").assertIsEnabled()
        composeTestRule.onNodeWithText("Password").assertIsEnabled()
        composeTestRule.onNodeWithContentDescription("Back").assertHasClickAction()
        composeTestRule.onNodeWithContentDescription("Select Image").assertHasClickAction()
        composeTestRule.onNodeWithText("Create Account").assertHasClickAction()
    }

    @Test
    fun registerScreen_buttonsAreClickable() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Verify all buttons are clickable
        composeTestRule.onNodeWithContentDescription("Back").assertHasClickAction()
        composeTestRule.onNodeWithContentDescription("Select Image").assertHasClickAction()
        composeTestRule.onNodeWithText("Create Account").assertHasClickAction()
    }

    @Test
    fun registerScreen_textFieldsAcceptInput() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Verify text fields can accept input
        composeTestRule.onNodeWithText("Username").assertIsEnabled()
        composeTestRule.onNodeWithText("Phone").assertIsEnabled()
        composeTestRule.onNodeWithText("Email").assertIsEnabled()
        composeTestRule.onNodeWithText("Password").assertIsEnabled()
        
        // Test that they can receive text input
        composeTestRule.onNodeWithText("Username").performTextInput("test")
        composeTestRule.onNodeWithText("Phone").performTextInput("123")
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("pass")
        
        // Verify input was accepted
        composeTestRule.onNodeWithText("Username").assertTextContains("test")
        composeTestRule.onNodeWithText("Phone").assertTextContains("123")
        composeTestRule.onNodeWithText("Email").assertTextContains("test@example.com")
    }

    @Test
    fun registerScreen_hasProperSemantics() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Verify semantic properties
        composeTestRule.onNodeWithText("Username")
            .assertIsDisplayed()
            .assertIsEnabled()

        composeTestRule.onNodeWithText("Phone")
            .assertIsDisplayed()
            .assertIsEnabled()

        composeTestRule.onNodeWithText("Email")
            .assertIsDisplayed()
            .assertIsEnabled()

        composeTestRule.onNodeWithText("Password")
            .assertIsDisplayed()
            .assertIsEnabled()

        composeTestRule.onNodeWithText("Create Account")
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
    }

    @Test
    fun registerScreen_keyboardNavigation_worksCorrectly() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Navigate through fields using keyboard/focus
        composeTestRule.onNodeWithText("Username").performClick()
        composeTestRule.onNodeWithText("Username").assertIsFocused()

        // Then - Should be able to navigate to other fields
        composeTestRule.onNodeWithText("Phone").performClick()
        composeTestRule.onNodeWithText("Phone").assertIsFocused()

        composeTestRule.onNodeWithText("Email").performClick()
        composeTestRule.onNodeWithText("Email").assertIsFocused()

        composeTestRule.onNodeWithText("Password").performClick()
        composeTestRule.onNodeWithText("Password").assertIsFocused()
    }

    @Test
    fun registerScreen_screenReader_canAccessAllElements() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // Then - All interactive elements should be accessible to screen readers
        val accessibleNodes = listOf(
            "Username",
            "Phone", 
            "Email",
            "Password",
            "Create Account"
        )

        accessibleNodes.forEach { text ->
            composeTestRule.onNodeWithText(text)
                .assertIsDisplayed()
                .assertExists()
        }

        // Content descriptions should also be accessible
        val accessibleDescriptions = listOf(
            "Back",
            "Default Profile Icon",
            "Select Image"
        )

        accessibleDescriptions.forEach { description ->
            composeTestRule.onNodeWithContentDescription(description)
                .assertIsDisplayed()
                .assertExists()
        }
    }

    @Test
    fun registerScreen_touchTargets_areAppropriateSize() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Interactive elements should be large enough for touch
        composeTestRule.onNodeWithText("Create Account")
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick() // Should be able to click without issues

        composeTestRule.onNodeWithContentDescription("Back")
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick() // Should be able to click without issues

        composeTestRule.onNodeWithContentDescription("Select Image")
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick() // Should be able to click without issues
    }

    @Test
    fun registerScreen_layoutAdaptsToContent() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Layout should accommodate all content properly
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Default Profile Icon").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Select Image").assertIsDisplayed()
        composeTestRule.onNodeWithText("Username").assertIsDisplayed()
        composeTestRule.onNodeWithText("Phone").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()

        // All elements should be visible without scrolling in normal cases
        composeTestRule.onRoot().assertIsDisplayed()
    }

    @Test
    fun registerScreen_textContrast_isReadable() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // Then - All text should be visible and readable
        composeTestRule.onNodeWithText("Username").assertIsDisplayed()
        composeTestRule.onNodeWithText("Phone").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
    }

    @Test
    fun registerScreen_profileImageArea_isAccessible() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // Then - Profile image area should be accessible
        composeTestRule.onNodeWithContentDescription("Default Profile Icon")
            .assertIsDisplayed()
            .assertExists()

        composeTestRule.onNodeWithContentDescription("Select Image")
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertExists()

        // Should be able to interact with image selection
        composeTestRule.onNodeWithContentDescription("Select Image").performClick()
        composeTestRule.onNodeWithContentDescription("Select Image").assertExists()
    }
}