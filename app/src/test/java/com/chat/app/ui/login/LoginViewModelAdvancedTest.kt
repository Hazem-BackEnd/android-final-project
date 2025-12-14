package com.chat.app.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.chat.app.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class LoginViewModelAdvancedTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val authRepository = mockk<AuthRepository>()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signIn should emit loading then success states in correct order`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        coEvery { authRepository.login(email, password) } coAnswers {
            delay(100) // Simulate network delay
            Result.success(true)
        }

        // When & Then
        viewModel.state.test {
            assertEquals(SignInState.Nothing, awaitItem()) // Initial state

            viewModel.signIn(email, password)
            assertEquals(SignInState.Loading, awaitItem()) // Loading state
            
            testScheduler.advanceUntilIdle() // Process all coroutines
            assertEquals(SignInState.Success, awaitItem()) // Success state
        }
    }

    @Test
    fun `signIn should emit loading then error states when login fails`() = runTest {
        // Given
        val email = "invalid@example.com"
        val password = "wrongpassword"
        coEvery { authRepository.login(email, password) } coAnswers {
            delay(100) // Simulate network delay
            Result.failure(Exception("Authentication failed"))
        }

        // When & Then
        viewModel.state.test {
            assertEquals(SignInState.Nothing, awaitItem()) // Initial state

            viewModel.signIn(email, password)
            assertEquals(SignInState.Loading, awaitItem()) // Loading state
            
            testScheduler.advanceUntilIdle() // Process all coroutines
            assertEquals(SignInState.Error, awaitItem()) // Error state
        }
    }

    @Test
    fun `signIn should handle repository exception and emit error state`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        coEvery { authRepository.login(email, password) } coAnswers {
            delay(50)
            throw RuntimeException("Network connection failed")
        }

        // When & Then
        viewModel.state.test {
            assertEquals(SignInState.Nothing, awaitItem()) // Initial state

            viewModel.signIn(email, password)
            assertEquals(SignInState.Loading, awaitItem()) // Loading state
            
            testScheduler.advanceUntilIdle() // Process all coroutines
            assertEquals(SignInState.Error, awaitItem()) // Error state
        }
    }

    @Test
    fun `multiple signIn calls should eventually reach success state`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        coEvery { authRepository.login(email, password) } returns Result.success(true)

        // When
        viewModel.signIn(email, password)
        viewModel.signIn(email, password) // Second call
        
        testScheduler.advanceUntilIdle() // Process all coroutines

        // Then - Final state should be Success
        assertEquals(SignInState.Success, viewModel.state.value)
    }

    @Test
    fun `signIn with successful result true should emit success`() = runTest {
        // Given
        val email = "user@test.com"
        val password = "validpass"
        coEvery { authRepository.login(email, password) } returns Result.success(true)

        // When & Then
        viewModel.state.test {
            assertEquals(SignInState.Nothing, awaitItem())

            viewModel.signIn(email, password)
            assertEquals(SignInState.Loading, awaitItem())
            
            testScheduler.advanceUntilIdle()
            assertEquals(SignInState.Success, awaitItem())
        }
    }

    @Test
    fun `signIn with failure result should emit error`() = runTest {
        // Given
        val email = "user@test.com"
        val password = "invalidpass"
        coEvery { authRepository.login(email, password) } returns Result.failure(Exception("Invalid credentials"))

        // When & Then
        viewModel.state.test {
            assertEquals(SignInState.Nothing, awaitItem())

            viewModel.signIn(email, password)
            assertEquals(SignInState.Loading, awaitItem())
            
            testScheduler.advanceUntilIdle()
            assertEquals(SignInState.Error, awaitItem())
        }
    }
}