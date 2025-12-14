package com.chat.app.ui.register

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chat.app.data.repository.AuthRepository
import com.chat.app.data.repository.StorageRepository
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
class RegisterViewModelEdgeCasesTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val authRepository = mockk<AuthRepository>()
    private val storageRepository = mockk<StorageRepository>()
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RegisterViewModel(authRepository, storageRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signUp with very long input fields should work`() = runTest {
        // Given
        val longName = "A".repeat(1000)
        val longPhone = "1".repeat(50)
        val longEmail = "a".repeat(500) + "@example.com"
        val longPassword = "p".repeat(1000)
        val imageUri: Uri? = null
        
        coEvery { authRepository.register(longName, longPhone, longEmail, longPassword, null) } returns Result.success(true)

        // When
        viewModel.signUp(longName, longPhone, longEmail, longPassword, imageUri)

        // Then
        assertEquals(SignUpState.Success, viewModel.state.value)
        coVerify { authRepository.register(longName, longPhone, longEmail, longPassword, null) }
    }

    @Test
    fun `signUp with special characters should work`() = runTest {
        // Given
        val specialName = "José María O'Connor-Smith"
        val specialPhone = "+1 (555) 123-4567"
        val specialEmail = "test+tag@example-domain.co.uk"
        val specialPassword = "P@ssw0rd!#$%^&*()"
        val imageUri: Uri? = null
        
        coEvery { authRepository.register(specialName, specialPhone, specialEmail, specialPassword, null) } returns Result.success(true)

        // When
        viewModel.signUp(specialName, specialPhone, specialEmail, specialPassword, imageUri)

        // Then
        assertEquals(SignUpState.Success, viewModel.state.value)
        coVerify { authRepository.register(specialName, specialPhone, specialEmail, specialPassword, null) }
    }

    @Test
    fun `signUp with unicode characters should work`() = runTest {
        // Given
        val unicodeName = "张三李四"
        val unicodePhone = "١٢٣٤٥٦٧٨٩٠"
        val unicodeEmail = "тест@пример.рф"
        val unicodePassword = "пароль123"
        val imageUri: Uri? = null
        
        coEvery { authRepository.register(unicodeName, unicodePhone, unicodeEmail, unicodePassword, null) } returns Result.success(true)

        // When
        viewModel.signUp(unicodeName, unicodePhone, unicodeEmail, unicodePassword, imageUri)

        // Then
        assertEquals(SignUpState.Success, viewModel.state.value)
        coVerify { authRepository.register(unicodeName, unicodePhone, unicodeEmail, unicodePassword, null) }
    }

    @Test
    fun `different exception types should all result in error state`() = runTest {
        val fullName = "John Doe"
        val phoneNumber = "1234567890"
        val email = "john@example.com"
        val password = "password123"
        val imageUri: Uri? = null

        // Test different exception types
        val exceptions = listOf(
            RuntimeException("Runtime error"),
            IllegalArgumentException("Invalid argument"),
            NullPointerException("Null pointer"),
            Exception("Generic exception")
        )

        exceptions.forEach { exception ->
            // Given
            coEvery { authRepository.register(fullName, phoneNumber, email, password, null) } throws exception

            // When
            viewModel.signUp(fullName, phoneNumber, email, password, imageUri)

            // Then
            assertEquals("Failed for exception: ${exception.javaClass.simpleName}", 
                        SignUpState.Error, viewModel.state.value)
        }
    }

    @Test
    fun `signUp after previous error should reset state correctly`() = runTest {
        // Given - First call fails
        val fullName = "John Doe"
        val phoneNumber = "1234567890"
        val email = "john@example.com"
        val password = "password123"
        val imageUri: Uri? = null
        
        coEvery { authRepository.register(fullName, phoneNumber, email, password, null) } returns Result.failure(Exception("First failure"))

        // When - First call
        viewModel.signUp(fullName, phoneNumber, email, password, imageUri)
        assertEquals(SignUpState.Error, viewModel.state.value)

        // Given - Second call succeeds
        coEvery { authRepository.register(fullName, phoneNumber, email, password, null) } returns Result.success(true)

        // When - Second call
        viewModel.signUp(fullName, phoneNumber, email, password, imageUri)

        // Then
        assertEquals(SignUpState.Success, viewModel.state.value)
    }

    @Test
    fun `signUp after previous success should reset state correctly`() = runTest {
        // Given - First call succeeds
        val fullName = "John Doe"
        val phoneNumber = "1234567890"
        val email = "john@example.com"
        val password = "password123"
        val imageUri: Uri? = null
        
        coEvery { authRepository.register(fullName, phoneNumber, email, password, null) } returns Result.success(true)

        // When - First call
        viewModel.signUp(fullName, phoneNumber, email, password, imageUri)
        assertEquals(SignUpState.Success, viewModel.state.value)

        // Given - Second call fails
        coEvery { authRepository.register(fullName, phoneNumber, email, password, null) } returns Result.failure(Exception("Second failure"))

        // When - Second call
        viewModel.signUp(fullName, phoneNumber, email, password, imageUri)

        // Then
        assertEquals(SignUpState.Error, viewModel.state.value)
    }

    @Test
    fun `signUp with whitespace-only fields should be handled`() = runTest {
        // Given
        val whitespaceName = "   "
        val whitespacePhone = "\t\n  "
        val whitespaceEmail = "  \t  "
        val whitespacePassword = "\n\n  "
        val imageUri: Uri? = null
        
        coEvery { authRepository.register(whitespaceName, whitespacePhone, whitespaceEmail, whitespacePassword, null) } returns Result.failure(Exception("Invalid fields"))

        // When
        viewModel.signUp(whitespaceName, whitespacePhone, whitespaceEmail, whitespacePassword, imageUri)

        // Then
        assertEquals(SignUpState.Error, viewModel.state.value)
        coVerify { authRepository.register(whitespaceName, whitespacePhone, whitespaceEmail, whitespacePassword, null) }
    }

    @Test
    fun `viewModel should handle rapid successive calls`() = runTest {
        // Given
        val fullName = "John Doe"
        val phoneNumber = "1234567890"
        val email = "john@example.com"
        val password = "password123"
        val imageUri: Uri? = null
        
        coEvery { authRepository.register(fullName, phoneNumber, email, password, null) } returns Result.success(true)

        // When - Make multiple rapid calls
        repeat(5) {
            viewModel.signUp(fullName, phoneNumber, email, password, imageUri)
        }

        // Then - Should end up in success state
        assertEquals(SignUpState.Success, viewModel.state.value)
        // Verify repository was called multiple times
        coVerify(exactly = 5) { authRepository.register(fullName, phoneNumber, email, password, null) }
    }

    @Test
    fun `signUp with image upload exception should handle gracefully`() = runTest {
        // Given
        val fullName = "John Doe"
        val phoneNumber = "1234567890"
        val email = "john@example.com"
        val password = "password123"
        val imageUri = mockk<Uri>()
        
        coEvery { storageRepository.uploadProfileImage(imageUri, email) } throws RuntimeException("Storage service unavailable")

        // When
        viewModel.signUp(fullName, phoneNumber, email, password, imageUri)

        // Then
        assertEquals(SignUpState.Error, viewModel.state.value)
        coVerify { storageRepository.uploadProfileImage(imageUri, email) }
        // AuthRepository should not be called if image upload throws exception
        coVerify(exactly = 0) { authRepository.register(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `signUp with null fields should still call repository`() = runTest {
        // Given - Using empty strings to simulate null-like behavior
        val nullName = ""
        val nullPhone = ""
        val nullEmail = ""
        val nullPassword = ""
        val imageUri: Uri? = null
        
        coEvery { authRepository.register(nullName, nullPhone, nullEmail, nullPassword, null) } returns Result.failure(Exception("Null fields"))

        // When
        viewModel.signUp(nullName, nullPhone, nullEmail, nullPassword, imageUri)

        // Then
        assertEquals(SignUpState.Error, viewModel.state.value)
        coVerify { authRepository.register(nullName, nullPhone, nullEmail, nullPassword, null) }
    }

    @Test
    fun `signUp with mixed success and failure scenarios should work correctly`() = runTest {
        // Given
        val fullName = "John Doe"
        val phoneNumber = "1234567890"
        val email = "john@example.com"
        val password = "password123"
        val imageUri = mockk<Uri>()
        val imageUrl = "https://example.com/profile.jpg"
        
        // Image upload succeeds but registration fails
        coEvery { storageRepository.uploadProfileImage(imageUri, email) } returns imageUrl
        coEvery { authRepository.register(fullName, phoneNumber, email, password, imageUrl) } returns Result.failure(Exception("Email already exists"))

        // When
        viewModel.signUp(fullName, phoneNumber, email, password, imageUri)

        // Then
        assertEquals(SignUpState.Error, viewModel.state.value)
        coVerify { storageRepository.uploadProfileImage(imageUri, email) }
        coVerify { authRepository.register(fullName, phoneNumber, email, password, imageUrl) }
    }
}