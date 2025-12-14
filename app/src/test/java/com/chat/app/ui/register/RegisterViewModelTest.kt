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
class RegisterViewModelTest {

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
    fun `signUp with valid data without image should return success state`() = runTest {
        // Given
        val fullName = "John Doe"
        val phoneNumber = "1234567890"
        val email = "john@example.com"
        val password = "password123"
        val imageUri: Uri? = null
        
        coEvery { authRepository.register(fullName, phoneNumber, email, password, null) } returns Result.success(true)

        // When
        viewModel.signUp(fullName, phoneNumber, email, password, imageUri)

        // Then
        assertEquals(SignUpState.Success, viewModel.state.value)
        coVerify { authRepository.register(fullName, phoneNumber, email, password, null) }
        coVerify(exactly = 0) { storageRepository.uploadProfileImage(any(), any()) }
    }

    @Test
    fun `signUp with valid data and image should upload image then register`() = runTest {
        // Given
        val fullName = "Jane Doe"
        val phoneNumber = "0987654321"
        val email = "jane@example.com"
        val password = "password456"
        val imageUri = mockk<Uri>()
        val imageUrl = "https://example.com/profile.jpg"
        
        coEvery { storageRepository.uploadProfileImage(imageUri, email) } returns imageUrl
        coEvery { authRepository.register(fullName, phoneNumber, email, password, imageUrl) } returns Result.success(true)

        // When
        viewModel.signUp(fullName, phoneNumber, email, password, imageUri)

        // Then
        assertEquals(SignUpState.Success, viewModel.state.value)
        coVerify { storageRepository.uploadProfileImage(imageUri, email) }
        coVerify { authRepository.register(fullName, phoneNumber, email, password, imageUrl) }
    }

    @Test
    fun `signUp with invalid data should return error state`() = runTest {
        // Given
        val fullName = "John Doe"
        val phoneNumber = "1234567890"
        val email = "invalid@example.com"
        val password = "wrongpassword"
        val imageUri: Uri? = null
        
        coEvery { authRepository.register(fullName, phoneNumber, email, password, null) } returns Result.failure(Exception("Registration failed"))

        // When
        viewModel.signUp(fullName, phoneNumber, email, password, imageUri)

        // Then
        assertEquals(SignUpState.Error, viewModel.state.value)
        coVerify { authRepository.register(fullName, phoneNumber, email, password, null) }
    }

    @Test
    fun `signUp with repository exception should return error state`() = runTest {
        // Given
        val fullName = "John Doe"
        val phoneNumber = "1234567890"
        val email = "john@example.com"
        val password = "password123"
        val imageUri: Uri? = null
        
        coEvery { authRepository.register(fullName, phoneNumber, email, password, null) } throws RuntimeException("Network error")

        // When
        viewModel.signUp(fullName, phoneNumber, email, password, imageUri)

        // Then
        assertEquals(SignUpState.Error, viewModel.state.value)
        coVerify { authRepository.register(fullName, phoneNumber, email, password, null) }
    }

    @Test
    fun `initial state should be Nothing`() {
        // Given - ViewModel is created in setup

        // When - No action taken

        // Then
        assertEquals(SignUpState.Nothing, viewModel.state.value)
    }

    @Test
    fun `signUp with image upload failure should still proceed with registration`() = runTest {
        // Given
        val fullName = "John Doe"
        val phoneNumber = "1234567890"
        val email = "john@example.com"
        val password = "password123"
        val imageUri = mockk<Uri>()
        
        coEvery { storageRepository.uploadProfileImage(imageUri, email) } returns null // Upload fails
        coEvery { authRepository.register(fullName, phoneNumber, email, password, null) } returns Result.success(true)

        // When
        viewModel.signUp(fullName, phoneNumber, email, password, imageUri)

        // Then
        assertEquals(SignUpState.Success, viewModel.state.value)
        coVerify { storageRepository.uploadProfileImage(imageUri, email) }
        coVerify { authRepository.register(fullName, phoneNumber, email, password, null) }
    }

    @Test
    fun `signUp with empty fields should still call repository`() = runTest {
        // Given
        val fullName = ""
        val phoneNumber = ""
        val email = ""
        val password = ""
        val imageUri: Uri? = null
        
        coEvery { authRepository.register(fullName, phoneNumber, email, password, null) } returns Result.failure(Exception("Empty fields"))

        // When
        viewModel.signUp(fullName, phoneNumber, email, password, imageUri)

        // Then
        assertEquals(SignUpState.Error, viewModel.state.value)
        coVerify { authRepository.register(fullName, phoneNumber, email, password, null) }
    }

    @Test
    fun `multiple signUp calls should work independently`() = runTest {
        // Given
        val fullName1 = "John Doe"
        val phoneNumber1 = "1234567890"
        val email1 = "john@example.com"
        val password1 = "password1"
        
        val fullName2 = "Jane Doe"
        val phoneNumber2 = "0987654321"
        val email2 = "jane@example.com"
        val password2 = "password2"
        
        coEvery { authRepository.register(fullName1, phoneNumber1, email1, password1, null) } returns Result.success(true)
        coEvery { authRepository.register(fullName2, phoneNumber2, email2, password2, null) } returns Result.failure(Exception("Second registration failed"))

        // When - First call
        viewModel.signUp(fullName1, phoneNumber1, email1, password1, null)
        assertEquals(SignUpState.Success, viewModel.state.value)

        // When - Second call
        viewModel.signUp(fullName2, phoneNumber2, email2, password2, null)

        // Then
        assertEquals(SignUpState.Error, viewModel.state.value)
        coVerify { authRepository.register(fullName1, phoneNumber1, email1, password1, null) }
        coVerify { authRepository.register(fullName2, phoneNumber2, email2, password2, null) }
    }
}