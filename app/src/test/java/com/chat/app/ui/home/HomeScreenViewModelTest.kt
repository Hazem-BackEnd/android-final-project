package com.chat.app.ui.home

import com.chat.app.data.local.entities.ChatEntity
import org.junit.Test
import org.junit.Assert.*

class HomeScreenViewModelTest {

    // شوية داتا نجرب عليها - هتعمل الكلام ده برضو انت يحودا متقلقش ده بسيط
    private val sampleChats = listOf(
        ChatEntity(
            chatId = "chat_1",
            otherUserId = "Ahmed Ali",
            lastMessage = "Hey, how are you doing?",
            timestamp = System.currentTimeMillis() - 3600000
        ),
        ChatEntity(
            chatId = "chat_2",
            otherUserId = "Mariam Hassan",
            lastMessage = "Let's meet tomorrow for coffee",
            timestamp = System.currentTimeMillis() - 7200000
        ),
        ChatEntity(
            chatId = "chat_3",
            otherUserId = "Omar Khaled",
            lastMessage = "Thanks for your help!",
            timestamp = System.currentTimeMillis() - 86400000
        )
    )
    @Test
    fun `HomeUiState default values should be correct`() {
        val state = HomeUiState()
        
        assertTrue(state.chats.isEmpty())
        assertFalse(state.isLoading)
        assertEquals("", state.searchQuery)
        assertFalse(state.isSearching)
        assertNull(state.errorMessage)
    }

    @Test
    fun `HomeUiState with custom values should be correct`() {
        val state = HomeUiState(
            chats = sampleChats,
            isLoading = true,
            searchQuery = "test",
            isSearching = true,
            errorMessage = "error"
        )
        assertEquals(sampleChats, state.chats)
        assertTrue(state.isLoading)
        assertEquals("test", state.searchQuery)
        assertTrue(state.isSearching)
        assertEquals("error", state.errorMessage)
    }

    // Loading
    @Test
    fun `shouldShowLoading should return true when loading and no chats`() {
        val state = HomeUiState(
            chats = emptyList(),
            isLoading = true,
            errorMessage = null
        )
        assertTrue(state.shouldShowLoading)
        assertFalse(state.shouldShowEmpty)
        assertFalse(state.shouldShowChats)
    }

    @Test
    fun `shouldShowChats should return true when not loading and has chats`() {
        val state = HomeUiState(
            chats = sampleChats,
            isLoading = false,
            errorMessage = null
        )
        assertFalse(state.shouldShowLoading)
        assertFalse(state.shouldShowEmpty)
        assertTrue(state.shouldShowChats)
    }

    @Test
    fun `shouldShowChats should return false when loading even with chats`() {
        val state = HomeUiState(
            chats = sampleChats,
            isLoading = true,
            errorMessage = null
        )
        assertFalse(state.shouldShowLoading) // Has chats
        assertFalse(state.shouldShowEmpty)
        assertFalse(state.shouldShowChats) // Still loading
    }

    @Test
    fun `shouldShowChats should return false when no chats`() {
        val state = HomeUiState(
            chats = emptyList(),
            isLoading = false,
            errorMessage = null
        )
        assertFalse(state.shouldShowLoading)
        assertTrue(state.shouldShowEmpty)
        assertFalse(state.shouldShowChats) // No chats
    }

    @Test
    fun `only shouldShowChats should be true when loaded with chats and no error`() {
        val state = HomeUiState(
            chats = sampleChats,
            isLoading = false,
            errorMessage = null
        )
        assertFalse(state.shouldShowLoading)
        assertFalse(state.shouldShowEmpty)
        assertTrue(state.shouldShowChats)
    }

    @Test
    fun `manual search filtering should work correctly`() {
        val searchQuery = "Ahmed"
        val filteredChats = sampleChats.filter { chat ->
            chat.otherUserId.contains(searchQuery, ignoreCase = true) ||
            (chat.lastMessage?.contains(searchQuery, ignoreCase = true) == true)
        }
        assertEquals(1, filteredChats.size)
        assertEquals("Ahmed Ali", filteredChats.first().otherUserId)
    }

    @Test
    fun `manual search filtering should be case insensitive`() {
        val searchQuery = "AHMED"
        val filteredChats = sampleChats.filter { chat ->
            chat.otherUserId.contains(searchQuery, ignoreCase = true) ||
            (chat.lastMessage?.contains(searchQuery, ignoreCase = true) == true)
        }
        assertEquals(1, filteredChats.size)
        assertEquals("Ahmed Ali", filteredChats.first().otherUserId)
    }


}