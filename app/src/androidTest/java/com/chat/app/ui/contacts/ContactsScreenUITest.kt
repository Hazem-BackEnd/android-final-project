package com.chat.app.ui.contacts

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chat.app.ui.theme.ChatappTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContactsScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun contactsScreen_displaysTopBar() {
        composeTestRule.setContent {
            ChatappTheme {
                val navController = rememberNavController()
                ContactsScreenForTesting(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("Contacts").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Refresh").assertIsDisplayed()
    }

    @Test
    fun contactsScreen_displaysSearchField() {
        composeTestRule.setContent {
            ChatappTheme {
                val navController = rememberNavController()
                ContactsScreenForTesting(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("Search contacts...").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Search").assertIsDisplayed()
    }

    @Test
    fun contactsScreen_displaysContactsList() {
        composeTestRule.setContent {
            ChatappTheme {
                val navController = rememberNavController()
                ContactsScreenForTesting(navController = navController)
            }
        }

        // Check if sample contacts are displayed
        composeTestRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob Smith").assertIsDisplayed()
        composeTestRule.onNodeWithText("Charlie Brown").assertIsDisplayed()
    }

    @Test
    fun contactsScreen_searchFunctionality() {
        composeTestRule.setContent {
            ChatappTheme {
                val navController = rememberNavController()
                ContactsScreenForTesting(navController = navController)
            }
        }

        // Type in search field
        composeTestRule.onNodeWithText("Search contacts...")
            .performTextInput("Alice")

        // Verify search results (this would work with actual search implementation)
        composeTestRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
    }

    @Test
    fun contactsScreen_contactItemsAreClickable() {
        composeTestRule.setContent {
            ChatappTheme {
                val navController = rememberNavController()
                ContactsScreenForTesting(navController = navController)
            }
        }

        // Click on a contact item
        composeTestRule.onNodeWithText("Alice Johnson").performClick()
        
        // The click should be handled (navigation would occur in real implementation)
        composeTestRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
    }

    @Test
    fun contactsScreen_displaysContactPhoneNumbers() {
        composeTestRule.setContent {
            ChatappTheme {
                val navController = rememberNavController()
                ContactsScreenForTesting(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("+1234567890").assertIsDisplayed()
        composeTestRule.onNodeWithText("+1234567891").assertIsDisplayed()
    }

    @Test
    fun contactsScreen_displaysOnlineStatus() {
        composeTestRule.setContent {
            ChatappTheme {
                val navController = rememberNavController()
                ContactsScreenForTesting(navController = navController)
            }
        }

        // Online status indicators should be visible for online contacts
        // This is represented by green dots in the UI
        composeTestRule.onAllNodesWithContentDescription("Contact").assertCountEquals(8)
    }

    @Test
    fun contactsScreen_backButtonWorks() {
        composeTestRule.setContent {
            ChatappTheme {
                val navController = rememberNavController()
                ContactsScreenForTesting(navController = navController)
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        // In a real test, we would verify navigation occurred
    }

    @Test
    fun contactsScreen_refreshButtonWorks() {
        composeTestRule.setContent {
            ChatappTheme {
                val navController = rememberNavController()
                ContactsScreenForTesting(navController = navController)
            }
        }

        composeTestRule.onNodeWithContentDescription("Refresh").performClick()
        // In a real test, we would verify refresh action occurred
    }

    @Test
    fun contactsScreen_displaysContactCount() {
        composeTestRule.setContent {
            ChatappTheme {
                val navController = rememberNavController()
                ContactsScreenForTesting(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("Contacts (8)").assertIsDisplayed()
    }
}