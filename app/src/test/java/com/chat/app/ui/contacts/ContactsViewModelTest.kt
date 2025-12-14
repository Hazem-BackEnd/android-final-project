package com.chat.app.ui.contacts

import app.cash.turbine.test
import com.chat.app.data.local.entities.UserEntity
import com.chat.app.data.repository.ContactsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ContactsViewModelTest {

    private lateinit var viewModel: ContactsViewModel
    private lateinit var contactsRepository: ContactsRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        contactsRepository = mockk()
        viewModel = ContactsViewModel(contactsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadContacts should update UI state with contacts from repository`() = runTest {
        // Given
        val mockContacts = listOf(
            UserEntity("1", "Alice Johnson", "+1234567890"),
            UserEntity("2", "Bob Smith", "+1234567891")
        )
        coEvery { contactsRepository.getDeviceContacts() } returns mockContacts

        // When
        viewModel.loadContacts()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(2, state.contacts.size)
            assertEquals("Alice Johnson", state.contacts[0].name)
            assertEquals("Bob Smith", state.contacts[1].name)
        }
    }

    @Test
    fun `loadContacts should show loading state initially`() = runTest {
        // Given
        coEvery { contactsRepository.getDeviceContacts() } returns emptyList()

        // When
        viewModel.loadContacts()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isLoading)
        }
    }

    @Test
    fun `loadContacts should handle repository error`() = runTest {
        // Given
        val errorMessage = "Failed to read contacts"
        coEvery { contactsRepository.getDeviceContacts() } throws Exception(errorMessage)

        // When
        viewModel.loadContacts()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.errorMessage?.contains(errorMessage) == true)
            assertTrue(state.contacts.isEmpty())
        }
    }

    @Test
    fun `refreshContacts should call loadContacts`() = runTest {
        // Given
        coEvery { contactsRepository.getDeviceContacts() } returns emptyList()

        // When
        viewModel.refreshContacts()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { contactsRepository.getDeviceContacts() }
    }

    @Test
    fun `clearErrorMessage should remove error from UI state`() = runTest {
        // Given
        coEvery { contactsRepository.getDeviceContacts() } throws Exception("Error")
        viewModel.loadContacts()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.clearErrorMessage()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(null, state.errorMessage)
        }
    }

    @Test
    fun `searchContacts should filter contacts by name`() = runTest {
        // Given
        val mockContacts = listOf(
            UserEntity("1", "Alice Johnson", "+1234567890"),
            UserEntity("2", "Bob Smith", "+1234567891"),
            UserEntity("3", "Charlie Brown", "+1234567892")
        )
        coEvery { contactsRepository.getDeviceContacts() } returns mockContacts
        
        // Load contacts first
        viewModel.loadContacts()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.searchContacts("Alice")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.contacts.size)
            assertEquals("Alice Johnson", state.contacts[0].name)
            assertEquals("Alice", state.searchQuery)
        }
    }

    @Test
    fun `searchContacts should filter contacts by phone number`() = runTest {
        // Given
        val mockContacts = listOf(
            UserEntity("1", "Alice Johnson", "+1234567890"),
            UserEntity("2", "Bob Smith", "+1234567891")
        )
        coEvery { contactsRepository.getDeviceContacts() } returns mockContacts
        
        // Load contacts first
        viewModel.loadContacts()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.searchContacts("890")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.contacts.size)
            assertEquals("Alice Johnson", state.contacts[0].name)
        }
    }

    @Test
    fun `searchContacts with empty query should reload all contacts`() = runTest {
        // Given
        val mockContacts = listOf(
            UserEntity("1", "Alice Johnson", "+1234567890"),
            UserEntity("2", "Bob Smith", "+1234567891")
        )
        coEvery { contactsRepository.getDeviceContacts() } returns mockContacts

        // When
        viewModel.searchContacts("")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.contacts.size)
        }
    }

    @Test
    fun `clearSearch should reset search query and reload contacts`() = runTest {
        // Given
        val mockContacts = listOf(
            UserEntity("1", "Alice Johnson", "+1234567890"),
            UserEntity("2", "Bob Smith", "+1234567891")
        )
        coEvery { contactsRepository.getDeviceContacts() } returns mockContacts

        // When
        viewModel.clearSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.searchQuery)
            assertEquals(2, state.contacts.size)
        }
    }

    @Test
    fun `contacts should be sorted by name`() = runTest {
        // Given
        val mockContacts = listOf(
            UserEntity("1", "Charlie Brown", "+1234567892"),
            UserEntity("2", "Alice Johnson", "+1234567890"),
            UserEntity("3", "Bob Smith", "+1234567891")
        )
        coEvery { contactsRepository.getDeviceContacts() } returns mockContacts

        // When
        viewModel.loadContacts()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Alice Johnson", state.contacts[0].name)
            assertEquals("Bob Smith", state.contacts[1].name)
            assertEquals("Charlie Brown", state.contacts[2].name)
        }
    }

    @Test
    fun `search should be case insensitive`() = runTest {
        // Given
        val mockContacts = listOf(
            UserEntity("1", "Alice Johnson", "+1234567890"),
            UserEntity("2", "Bob Smith", "+1234567891")
        )
        coEvery { contactsRepository.getDeviceContacts() } returns mockContacts
        
        // Load contacts first
        viewModel.loadContacts()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.searchContacts("alice")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.contacts.size)
            assertEquals("Alice Johnson", state.contacts[0].name)
        }
    }
}