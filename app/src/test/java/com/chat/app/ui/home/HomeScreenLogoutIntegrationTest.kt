package com.chat.app.ui.home

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

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenLogoutIntegrationTest {

    private lateinit var viewModel: HomeScreenViewModel
    private lateinit var chatRepository: ChatRepository
    private lateinit var authRepository: AuthRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        chatRepository = mockk()
        authRepository = mockk()
        
        // Mock repositories
        every { chatRepository.allChats } returns flowOf(emptyList())
        coEvery { chatRepository.createOrUpdateChat(any()) } returns Unit
        coEvery { authRepository.logout() } returns Unit
        
        viewModel = HomeScreenViewModel(chatRepository, authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `complete logout flow should work correctly`() = runTest {
        // Given - ViewModel is initialized and has some state
        viewModel.updateSearchQuery("test search")
        viewModel.toggleSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        // When - User performs logout
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - AuthRepository logout should be called
        coVerify { authRepository.logout() }
        
        // And the UI should handle this appropriately
        // (In a real app, navigation would be handled by the UI layer)
    }

    @Test
    fun `logout should work even when chat operations are ongoing`() = runTest {
        // Given - Add sample data (simulating ongoing operations)
        viewModel.addSampleData()
        
        // When - Logout during operations
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Logout should still work
        coVerify { authRepository.logout() }
        coVerify(atLeast = 1) { chatRepository.createOrUpdateChat(any()) }
    }

    @Test
    fun `logout after search operations should work`() = runTest {
        // Given - User has performed search operations
        viewModel.updateSearchQuery("Ahmed")
        viewModel.toggleSearch()
        viewModel.updateSearchQuery("meeting")
        testDispatcher.scheduler.advanceUntilIdle()

        // When - User logs out
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Logout should work regardless of search state
        coVerify { authRepository.logout() }
    }

    @Test
    fun `logout should work independently of chat loading state`() = runTest {
        // Given - Simulate chat loading error
        every { chatRepository.allChats } throws Exception("Network error")
        val viewModelWithError = HomeScreenViewModel(chatRepository, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - User tries to logout despite chat loading error
        viewModelWithError.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Logout should still work
        coVerify { authRepository.logout() }
    }
}