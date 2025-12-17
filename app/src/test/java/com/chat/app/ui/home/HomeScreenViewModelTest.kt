package com.chat.app.ui.home

import com.chat.app.data.local.entities.ChatEntity
import org.junit.Assert.*
import org.junit.Test

class HomeScreenViewModelTest {

    private val sampleChats = listOf(
        ChatEntity(
            chatId = "chat_1",
            otherUserId = "user_1",
            otherUserName = "Ahmed Ali",
            lastMessage = "Hey, how are you doing?",
            timestamp = System.currentTimeMillis() - 3600000
        ),
        ChatEntity(
            chatId = "chat_2",
            otherUserId = "user_2",
            otherUserName = "Mariam Hassan",
            lastMessage = "Let's meet tomorrow for coffee",
            timestamp = System.currentTimeMillis() - 7200000
        )
    )

    @Test
    fun `CurrentUserInfo default values should be empty`() {
        val userInfo = CurrentUserInfo()
        assertEquals("", userInfo.uid)
        assertEquals("", userInfo.fullName)
        assertNull(userInfo.profilePictureUrl)
    }

    @Test
    fun `CurrentUserInfo with values should store correctly`() {
        val userInfo = CurrentUserInfo(
            uid = "user123",
            fullName = "John Doe",
            profilePictureUrl = "https://example.com/photo.jpg"
        )
        assertEquals("user123", userInfo.uid)
        assertEquals("John Doe", userInfo.fullName)
        assertEquals("https://example.com/photo.jpg", userInfo.profilePictureUrl)
    }

    @Test
    fun `HomeUiState default values should be correct`() {
        val state = HomeUiState()
        assertTrue(state.chats.isEmpty())
        assertFalse(state.isLoading)
        assertEquals("", state.searchQuery)
        assertFalse(state.isSearching)
        assertNull(state.errorMessage)
        assertEquals(CurrentUserInfo(), state.currentUser)
    }

    @Test
    fun `shouldShowLoading should return true when loading and no chats`() {
        val state = HomeUiState(isLoading = true, chats = emptyList())
        assertTrue(state.shouldShowLoading)
        assertFalse(state.shouldShowEmpty)
        assertFalse(state.shouldShowChats)
    }

    @Test
    fun `shouldShowChats should return true when not loading and has chats`() {
        val state = HomeUiState(isLoading = false, chats = sampleChats)
        assertFalse(state.shouldShowLoading)
        assertFalse(state.shouldShowEmpty)
        assertTrue(state.shouldShowChats)
    }

    @Test
    fun `manual search filtering should work correctly`() {
        val searchQuery = "Ahmed"
        val filteredChats = sampleChats.filter { chat ->
            chat.otherUserName.contains(searchQuery, ignoreCase = true) ||
                    (chat.lastMessage?.contains(searchQuery, ignoreCase = true) == true)
        }
        assertEquals(1, filteredChats.size)
        assertEquals("Ahmed Ali", filteredChats.first().otherUserName)
    }
}
