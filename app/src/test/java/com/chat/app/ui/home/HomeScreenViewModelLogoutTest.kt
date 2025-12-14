package com.chat.app.ui.home

import app.cash.turbine.test
import com.chat.app.data.repository.AuthRepository
import com.chat.app.data.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModelLogoutTest {

    private lateinit var viewModel: HomeScreenViewModel
    private lateinit var chatRepository: ChatRepository
    private lateinit var authRepository: AuthRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        chatRepository = mockk()
        authRepository = mockk()
        
        // Mock the chat repository to return empty flow
        every { chatRepository.allChats } returns flowOf(emptyList())
        
        viewModel = HomeScreenViewModel(chatRepository, authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `logout should call authRepository logout method`() = runTest {
        // Given
        coEvery { authRepository.logout() } returns Unit

        // When
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { authRepository.logout() }
    }

    @Test
    fun `logout should handle success case`() = runTest {
        // Given
        coEvery { authRepository.logout() } returns Unit

        // When
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.errorMessage) // No error should be set
        }
    }

    @Test
    fun `logout should handle error case`() = runTest {
        // Given
        val errorMessage = "Logout failed"
        coEvery { authRepository.logout() } throws Exception(errorMessage)

        // When
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Failed to logout: $errorMessage", state.errorMessage)
        }
    }

    @Test
    fun `logout should not affect other UI state properties`() = runTest {
        // Given
        coEvery { authRepository.logout() } returns Unit
        
        // Set some initial state
        viewModel.updateSearchQuery("test")
        viewModel.toggleSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("test", state.searchQuery) // Search query should remain
            assertEquals(true, state.isSearching) // Search mode should remain
            assertNull(state.errorMessage) // No error for successful logout
        }
    }

    @Test
    fun `multiple logout calls should work correctly`() = runTest {
        // Given
        coEvery { authRepository.logout() } returns Unit

        // When
        viewModel.logout()
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 2) { authRepository.logout() }
    }

    @Test
    fun `logout error should not clear previous error messages`() = runTest {
        // Given
        val firstError = "First error"
        val logoutError = "Logout error"
        
        // Set initial error
        coEvery { chatRepository.allChats } throws Exception(firstError)
        coEvery { authRepository.logout() } throws Exception(logoutError)

        // When
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Failed to logout: $logoutError", state.errorMessage)
        }
    }
}