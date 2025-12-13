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
class LoginViewModelEdgeCasesTest {

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
    fun `signIn with null or empty credentials should still call repository`() = runTest {
        // Given
        val emptyEmail = ""
        val emptyPassword = ""
        coEvery { authRepository.login(emptyEmail, emptyPassword) } returns Result.failure(Exception("Empty credentials"))

        // When
        viewModel.signIn(emptyEmail, emptyPassword)

        // Then
        assertEquals(SignInState.Error, viewModel.state.value)
        coVerify { authRepository.login(emptyEmail, emptyPassword) }
    }

    @Test
    fun `signIn with very long credentials should work`() = runTest {
        // Given
        val longEmail = "a".repeat(1000) + "@example.com"
        val longPassword = "p".repeat(1000)
        coEvery { authRepository.login(longEmail, longPassword) } returns Result.success(true)

        // When
        viewModel.signIn(longEmail, longPassword)

        // Then
        assertEquals(SignInState.Success, viewModel.state.value)
        coVerify { authRepository.login(longEmail, longPassword) }
    }

    @Test
    fun `signIn with special characters should work`() = runTest {
        // Given
        val specialEmail = "test+tag@example-domain.co.uk"
        val specialPassword = "P@ssw0rd!#$%^&*()"
        coEvery { authRepository.login(specialEmail, specialPassword) } returns Result.success(true)

        // When
        viewModel.signIn(specialEmail, specialPassword)

        // Then
        assertEquals(SignInState.Success, viewModel.state.value)
        coVerify { authRepository.login(specialEmail, specialPassword) }
    }

    @Test
    fun `signIn with unicode characters should work`() = runTest {
        // Given
        val unicodeEmail = "тест@пример.рф"
        val unicodePassword = "пароль123"
        coEvery { authRepository.login(unicodeEmail, unicodePassword) } returns Result.success(true)

        // When
        viewModel.signIn(unicodeEmail, unicodePassword)

        // Then
        assertEquals(SignInState.Success, viewModel.state.value)
        coVerify { authRepository.login(unicodeEmail, unicodePassword) }
    }

    @Test
    fun `repository throwing different exception types should all result in error state`() = runTest {
        val email = "test@example.com"
        val password = "password"

        // Test different exception types
        val exceptions = listOf(
            RuntimeException("Runtime error"),
            IllegalArgumentException("Invalid argument"),
            NullPointerException("Null pointer"),
            Exception("Generic exception")
        )

        exceptions.forEach { exception ->
            // Given
            coEvery { authRepository.login(email, password) } throws exception

            // When
            viewModel.signIn(email, password)

            // Then
            assertEquals("Failed for exception: ${exception.javaClass.simpleName}", 
                        SignInState.Error, viewModel.state.value)
        }
    }

    @Test
    fun `signIn after previous error should reset state correctly`() = runTest {
        // Given - First call fails
        val email = "test@example.com"
        val password = "password"
        coEvery { authRepository.login(email, password) } returns Result.failure(Exception("First failure"))

        // When - First call
        viewModel.signIn(email, password)
        assertEquals(SignInState.Error, viewModel.state.value)

        // Given - Second call succeeds
        coEvery { authRepository.login(email, password) } returns Result.success(true)

        // When - Second call
        viewModel.signIn(email, password)

        // Then
        assertEquals(SignInState.Success, viewModel.state.value)
    }

    @Test
    fun `signIn after previous success should reset state correctly`() = runTest {
        // Given - First call succeeds
        val email = "test@example.com"
        val password = "password"
        coEvery { authRepository.login(email, password) } returns Result.success(true)

        // When - First call
        viewModel.signIn(email, password)
        assertEquals(SignInState.Success, viewModel.state.value)

        // Given - Second call fails
        coEvery { authRepository.login(email, password) } returns Result.failure(Exception("Second failure"))

        // When - Second call
        viewModel.signIn(email, password)

        // Then
        assertEquals(SignInState.Error, viewModel.state.value)
    }

    @Test
    fun `repository returning Result failure should emit error state`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"
        coEvery { authRepository.login(email, password) } returns Result.failure(Exception("Authentication failed"))

        // When
        viewModel.signIn(email, password)

        // Then
        assertEquals(SignInState.Error, viewModel.state.value)
        coVerify { authRepository.login(email, password) }
    }

    @Test
    fun `viewModel should handle rapid successive calls`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        coEvery { authRepository.login(email, password) } returns Result.success(true)

        // When - Make multiple rapid calls
        repeat(5) {
            viewModel.signIn(email, password)
        }

        // Then - Should end up in success state
        assertEquals(SignInState.Success, viewModel.state.value)
        // Verify repository was called multiple times
        coVerify(exactly = 5) { authRepository.login(email, password) }
    }

    @Test
    fun `signIn with whitespace-only credentials should be handled`() = runTest {
        // Given
        val whitespaceEmail = "   "
        val whitespacePassword = "\t\n  "
        coEvery { authRepository.login(whitespaceEmail, whitespacePassword) } returns Result.failure(Exception("Invalid credentials"))

        // When
        viewModel.signIn(whitespaceEmail, whitespacePassword)

        // Then
        assertEquals(SignInState.Error, viewModel.state.value)
        coVerify { authRepository.login(whitespaceEmail, whitespacePassword) }
    }
}