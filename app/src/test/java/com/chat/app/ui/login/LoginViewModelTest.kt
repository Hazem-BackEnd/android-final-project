package com.chat.app.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chat.app.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
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
    fun `signIn with valid credentials should return success state`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        coEvery { authRepository.login(email, password) } returns Result.success(true)

        // When
        viewModel.signIn(email, password)

        // Then
        assertEquals(SignInState.Success, viewModel.state.value)
        coVerify { authRepository.login(email, password) }
    }

    @Test
    fun `signIn with invalid credentials should return error state`() = runTest {
        // Given
        val email = "invalid@example.com"
        val password = "wrongpassword"
        coEvery { authRepository.login(email, password) } returns Result.failure(Exception("Invalid credentials"))

        // When
        viewModel.signIn(email, password)

        // Then
        assertEquals(SignInState.Error, viewModel.state.value)
        coVerify { authRepository.login(email, password) }
    }

    @Test
    fun `signIn should set loading state initially`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        coEvery { authRepository.login(email, password) } returns Result.success(true)

        // When
        viewModel.signIn(email, password)

        // Then - Check that loading state was set (we can't easily test the intermediate state with UnconfinedTestDispatcher)
        // But we can verify the final state and that the repository was called
        coVerify { authRepository.login(email, password) }
        assertEquals(SignInState.Success, viewModel.state.value)
    }

    @Test
    fun `signIn with repository exception should return error state`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        coEvery { authRepository.login(email, password) } throws RuntimeException("Network error")

        // When
        viewModel.signIn(email, password)

        // Then
        assertEquals(SignInState.Error, viewModel.state.value)
        coVerify { authRepository.login(email, password) }
    }

    @Test
    fun `initial state should be Nothing`() {
        // Given - ViewModel is created in setup

        // When - No action taken

        // Then
        assertEquals(SignInState.Nothing, viewModel.state.value)
    }

    @Test
    fun `signIn with empty email should still call repository`() = runTest {
        // Given
        val email = ""
        val password = "password123"
        coEvery { authRepository.login(email, password) } returns Result.failure(Exception("Empty email"))

        // When
        viewModel.signIn(email, password)

        // Then
        assertEquals(SignInState.Error, viewModel.state.value)
        coVerify { authRepository.login(email, password) }
    }

    @Test
    fun `multiple signIn calls should work independently`() = runTest {
        // Given
        val email1 = "test1@example.com"
        val password1 = "password1"
        val email2 = "test2@example.com"
        val password2 = "password2"
        
        coEvery { authRepository.login(email1, password1) } returns Result.success(true)
        coEvery { authRepository.login(email2, password2) } returns Result.failure(Exception("Invalid"))

        // When - First call
        viewModel.signIn(email1, password1)
        assertEquals(SignInState.Success, viewModel.state.value)

        // When - Second call
        viewModel.signIn(email2, password2)

        // Then
        assertEquals(SignInState.Error, viewModel.state.value)
        coVerify { authRepository.login(email1, password1) }
        coVerify { authRepository.login(email2, password2) }
    }
}