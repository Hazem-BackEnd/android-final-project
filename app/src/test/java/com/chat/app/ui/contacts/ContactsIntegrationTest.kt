package com.chat.app.ui.contacts

import app.cash.turbine.test
import com.chat.app.data.local.entities.ChatEntity
import com.chat.app.data.local.entities.UserEntity
import com.chat.app.data.repository.ChatRepository
import com.chat.app.data.repository.ContactsRepository
import com.chat.app.data.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ContactsIntegrationTest {

    private lateinit var viewModel: ContactsViewModel
    private lateinit var contactsRepository: ContactsRepository
    private lateinit var userRepository: UserRepository
    private lateinit var chatRepository: ChatRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        contactsRepository = mockk()
        userRepository = mockk()
        chatRepository = mockk()
        viewModel = ContactsViewModel(contactsRepository, userRepository, chatRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `complete contact click flow should work correctly`() = runTest {
        // Given
        val contact = Contact("123", "John Doe", "+1234567890", false)
        val userSlot = slot<UserEntity>()
        val chatSlot = slot<ChatEntity>()
        var navigationChatId = ""

        coEvery { userRepository.saveUserLocally(capture(userSlot)) } returns Unit
        coEvery { chatRepository.getChat(any()) } returns null
        coEvery { chatRepository.createOrUpdateChat(capture(chatSlot)) } returns Unit

        // When
        viewModel.onContactClick(contact) { chatId ->
            navigationChatId = chatId
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Verify user was saved
        coVerify { userRepository.saveUserLocally(any()) }
        assertEquals("123", userSlot.captured.uid)
        assertEquals("John Doe", userSlot.captured.fullName)
        assertEquals("+1234567890", userSlot.captured.phoneNumber)

        // Then - Verify chat was created
        coVerify { chatRepository.createOrUpdateChat(any()) }
        assertEquals("chat_123", chatSlot.captured.chatId)
        assertEquals("123", chatSlot.captured.otherUserId)

        // Then - Verify navigation callback was called
        assertEquals("chat_123", navigationChatId)
    }

    @Test
    fun `contact click with existing chat should update timestamp`() = runTest {
        // Given
        val contact = Contact("456", "Jane Smith", "+0987654321", true)
        val existingChat = ChatEntity(
            chatId = "chat_456",
            otherUserId = "456",
            lastMessage = "Previous message",
            timestamp = 1000L
        )
        val chatSlot = slot<ChatEntity>()

        coEvery { userRepository.saveUserLocally(any()) } returns Unit
        coEvery { chatRepository.getChat("chat_456") } returns existingChat
        coEvery { chatRepository.createOrUpdateChat(capture(chatSlot)) } returns Unit

        // When
        viewModel.onContactClick(contact) { }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(chatSlot.captured.timestamp > 1000L)
        assertEquals("Previous message", chatSlot.captured.lastMessage)
        assertEquals("456", chatSlot.captured.otherUserId)
    }

    @Test
    fun `load contacts and click flow integration`() = runTest {
        // Given
        val deviceContacts = listOf(
            UserEntity("1", "Alice Johnson", "+1111111111"),
            UserEntity("2", "Bob Smith", "+2222222222")
        )
        
        coEvery { contactsRepository.getDeviceContacts() } returns deviceContacts
        coEvery { userRepository.saveUserLocally(any()) } returns Unit
        coEvery { chatRepository.getChat(any()) } returns null
        coEvery { chatRepository.createOrUpdateChat(any()) } returns Unit

        // When - Load contacts
        viewModel.loadContacts()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Verify contacts are loaded
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.contacts.size)
            assertEquals("Alice Johnson", state.contacts[0].name)
            assertEquals("Bob Smith", state.contacts[1].name)
        }

        // When - Click on first contact
        var clickedChatId = ""
        val firstContact = Contact("1", "Alice Johnson", "+1111111111", false)
        viewModel.onContactClick(firstContact) { chatId ->
            clickedChatId = chatId
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Verify chat creation
        assertEquals("chat_1", clickedChatId)
        coVerify { userRepository.saveUserLocally(any()) }
        coVerify { chatRepository.createOrUpdateChat(any()) }
    }

    @Test
    fun `search and click integration should work`() = runTest {
        // Given
        val deviceContacts = listOf(
            UserEntity("1", "Alice Johnson", "+1111111111"),
            UserEntity("2", "Bob Smith", "+2222222222"),
            UserEntity("3", "Charlie Brown", "+3333333333")
        )
        
        coEvery { contactsRepository.getDeviceContacts() } returns deviceContacts
        coEvery { userRepository.saveUserLocally(any()) } returns Unit
        coEvery { chatRepository.getChat(any()) } returns null
        coEvery { chatRepository.createOrUpdateChat(any()) } returns Unit

        // When - Load and search
        viewModel.loadContacts()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.searchContacts("Alice")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Verify search results
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.contacts.size)
            assertEquals("Alice Johnson", state.contacts[0].name)
        }

        // When - Click on search result
        var searchClickChatId = ""
        val searchResult = Contact("1", "Alice Johnson", "+1111111111", false)
        viewModel.onContactClick(searchResult) { chatId ->
            searchClickChatId = chatId
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Verify chat creation from search result
        assertEquals("chat_1", searchClickChatId)
    }
}