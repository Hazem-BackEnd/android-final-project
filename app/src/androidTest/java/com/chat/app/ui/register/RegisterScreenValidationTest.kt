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
class RegisterScreenValidationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockNavController = mockk<NavController>(relaxed = true)

    @Test
    fun registerScreen_emptyUsername_showsValidationError() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter and clear username
        composeTestRule.onNodeWithText("Username").performTextInput("test")
        composeTestRule.onNodeWithText("Username").performTextClearance()

        // Then - Validation error should appear
        composeTestRule.onNodeWithText("Username is required").assertIsDisplayed()
    }

    @Test
    fun registerScreen_shortUsername_showsValidationError() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter short username
        composeTestRule.onNodeWithText("Username").performTextInput("ab")

        // Then - Validation error should appear
        composeTestRule.onNodeWithText("Username must be at least 3 characters").assertIsDisplayed()
    }

    @Test
    fun registerScreen_validUsername_noValidationError() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter valid username
        composeTestRule.onNodeWithText("Username").performTextInput("validuser")

        // Then - No validation error should appear
        composeTestRule.onNodeWithText("Username is required").assertDoesNotExist()
        composeTestRule.onNodeWithText("Username must be at least 3 characters").assertDoesNotExist()
    }

    @Test
    fun registerScreen_emptyPhone_showsValidationError() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter and clear phone
        composeTestRule.onNodeWithText("Phone").performTextInput("123")
        composeTestRule.onNodeWithText("Phone").performTextClearance()

        // Then - Validation error should appear
        composeTestRule.onNodeWithText("Phone number is required").assertIsDisplayed()
    }

    @Test
    fun registerScreen_shortPhone_showsValidationError() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter short phone number
        composeTestRule.onNodeWithText("Phone").performTextInput("123456789")

        // Then - Validation error should appear
        composeTestRule.onNodeWithText("Phone number must be at least 10 digits").assertIsDisplayed()
    }

    @Test
    fun registerScreen_validPhone_noValidationError() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter valid phone number
        composeTestRule.onNodeWithText("Phone").performTextInput("1234567890")

        // Then - No validation error should appear
        composeTestRule.onNodeWithText("Phone number is required").assertDoesNotExist()
        composeTestRule.onNodeWithText("Phone number must be at least 10 digits").assertDoesNotExist()
    }

    @Test
    fun registerScreen_emptyEmail_showsValidationError() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter and clear email
        composeTestRule.onNodeWithText("Email").performTextInput("test")
        composeTestRule.onNodeWithText("Email").performTextClearance()

        // Then - Validation error should appear
        composeTestRule.onNodeWithText("Email is required").assertIsDisplayed()
    }

    @Test
    fun registerScreen_invalidEmail_showsValidationError() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter invalid email
        composeTestRule.onNodeWithText("Email").performTextInput("invalidemail")

        // Then - Validation error should appear
        composeTestRule.onNodeWithText("Invalid email format").assertIsDisplayed()
    }

    @Test
    fun registerScreen_validEmail_noValidationError() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter valid email
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")

        // Then - No validation error should appear
        composeTestRule.onNodeWithText("Email is required").assertDoesNotExist()
        composeTestRule.onNodeWithText("Invalid email format").assertDoesNotExist()
    }

    @Test
    fun registerScreen_emptyPassword_showsValidationError() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter and clear password
        composeTestRule.onNodeWithText("Password").performTextInput("test")
        composeTestRule.onNodeWithText("Password").performTextClearance()

        // Then - Validation error should appear
        composeTestRule.onNodeWithText("Password is required").assertIsDisplayed()
    }

    @Test
    fun registerScreen_shortPassword_showsValidationError() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter short password
        composeTestRule.onNodeWithText("Password").performTextInput("12345")

        // Then - Validation error should appear
        composeTestRule.onNodeWithText("Password must be at least 6 characters").assertIsDisplayed()
    }

    @Test
    fun registerScreen_validPassword_noValidationError() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter valid password
        composeTestRule.onNodeWithText("Password").performTextInput("password123")

        // Then - No validation error should appear
        composeTestRule.onNodeWithText("Password is required").assertDoesNotExist()
        composeTestRule.onNodeWithText("Password must be at least 6 characters").assertDoesNotExist()
    }

    @Test
    fun registerScreen_createAccountButton_disabledWithValidationErrors() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter invalid data
        composeTestRule.onNodeWithText("Username").performTextInput("ab") // Too short
        composeTestRule.onNodeWithText("Phone").performTextInput("123") // Too short
        composeTestRule.onNodeWithText("Email").performTextInput("invalid") // Invalid format
        composeTestRule.onNodeWithText("Password").performTextInput("123") // Too short

        // Then - Create Account button should be disabled
        composeTestRule.onNodeWithText("Create Account").assertIsNotEnabled()
    }

    @Test
    fun registerScreen_createAccountButton_enabledWithValidData() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter valid data
        composeTestRule.onNodeWithText("Username").performTextInput("validuser")
        composeTestRule.onNodeWithText("Phone").performTextInput("1234567890")
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")

        // Then - Create Account button should be enabled
        composeTestRule.onNodeWithText("Create Account").assertIsEnabled()
    }

    @Test
    fun registerScreen_realTimeValidation_worksCorrectly() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter invalid username, then fix it
        composeTestRule.onNodeWithText("Username").performTextInput("ab")
        composeTestRule.onNodeWithText("Username must be at least 3 characters").assertIsDisplayed()

        composeTestRule.onNodeWithText("Username").performTextInput("c") // Now "abc"
        composeTestRule.onNodeWithText("Username must be at least 3 characters").assertDoesNotExist()

        // When - Enter invalid email, then fix it
        composeTestRule.onNodeWithText("Email").performTextInput("invalid")
        composeTestRule.onNodeWithText("Invalid email format").assertIsDisplayed()

        composeTestRule.onNodeWithText("Email").performTextClearance()
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Invalid email format").assertDoesNotExist()
    }

    @Test
    fun registerScreen_multipleValidationErrors_displaySimultaneously() {
        // Given
        composeTestRule.setContent {
            ChatappTheme {
                RegisterScreenForTesting(navController = mockNavController)
            }
        }

        // When - Enter invalid data in multiple fields
        composeTestRule.onNodeWithText("Username").performTextInput("ab") // Too short
        composeTestRule.onNodeWithText("Phone").performTextInput("123") // Too short
        composeTestRule.onNodeWithText("Email").performTextInput("invalid") // Invalid format
        composeTestRule.onNodeWithText("Password").performTextInput("123") // Too short

        // Then - All validation errors should be displayed
        composeTestRule.onNodeWithText("Username must be at least 3 characters").assertIsDisplayed()
        composeTestRule.onNodeWithText("Phone number must be at least 10 digits").assertIsDisplayed()
        composeTestRule.onNodeWithText("Invalid email format").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password must be at least 6 characters").assertIsDisplayed()
    }
}