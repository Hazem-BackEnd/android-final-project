package com.chat.app.ui.register

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.chat.app.data.repository.AuthRepository
import com.chat.app.data.repository.StorageRepository
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
class RegisterViewModelAdvancedTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
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
    fun `signUp should emit loading then success states in correct order`() = runTest {
        // Given
        val fullName = "John Doe"
        val phoneNumber = "1234567890"
        val email = "john@example.com"
        val password = "password123"
        val imageUri: Uri? = null
        
        coEvery { authRepository.register(fullName, phoneNumber, email, password, null) } coAnswers {
            delay(100) // Simulate network delay
            Result.success(true)
        }

        // When & Then
        viewModel.state.test {
            assertEquals(SignUpState.Nothing, awaitItem()) // Initial state

            viewModel.signUp(fullName, phoneNumber, email, password, imageUri)
            assertEquals(SignUpState.Loading, awaitItem()) // Loading state
            
            testScheduler.advanceUntilIdle() // Process all coroutines
            assertEquals(SignUpState.Success, awaitItem()) // Success state
        }
    }

    @Test
    fun `signUp should emit loading then error states when registration fails`() = runTest {
        // Given
        val fullName = "John Doe"
        val phoneNumber = "1234567890"
        val email = "invalid@example.com"
        val password = "password123"
        val imageUri: Uri? = null
        
        coEvery { authRepository.register(fullName, phoneNumber, email, password, null) } coAnswers {
            delay(100) // Simulate network delay
            Result.failure(Exception("Registration failed"))
        }

        // When & Then
        viewModel.state.test {
            assertEquals(SignUpState.Nothing, awaitItem()) // Initial state

            viewModel.signUp(fullName, phoneNumber, email, password, imageUri)
            assertEquals(SignUpState.Loading, awaitItem()) // Loading state
            
            testScheduler.advanceUntilIdle() // Process all coroutines
            assertEquals(SignUpState.Error, awaitItem()) // Error state
        }
    }

    @Test
    fun `signUp with image should handle image upload then registration sequence`() = runTest {
        // Given
        val fullName = "Jane Doe"
        val phoneNumber = "0987654321"
        val email = "jane@example.com"
        val password = "password456"
        val imageUri = mockk<Uri>()
        val imageUrl = "https://example.com/profile.jpg"
        
        coEvery { storageRepository.uploadProfileImage(imageUri, email) } coAnswers {
            delay(50) // Simulate upload delay
            imageUrl
        }
        coEvery { authRepository.register(fullName, phoneNumber, email, password, imageUrl) } coAnswers {
            delay(100) // Simulate registration delay
            Result.success(true)
        }

        // When & Then
        viewModel.state.test {
            assertEquals(SignUpState.Nothing, awaitItem()) // Initial state

            viewModel.signUp(fullName, phoneNumber, email, password, imageUri)
            assertEquals(SignUpState.Loading, awaitItem()) // Loading state
            
            testScheduler.advanceUntilIdle() // Process all coroutines
            assertEquals(SignUpState.Success, awaitItem()) // Success state
        }
    }

    @Test
    fun `signUp should handle storage repository exception and emit error state`() = runTest {
        // Given
        val fullName = "John Doe"
        val phoneNumber = "1234567890"
        val email = "john@example.com"
        val password = "password123"
        val imageUri = mockk<Uri>()
        
        coEvery { storageRepository.uploadProfileImage(imageUri, email) } coAnswers {
            delay(50)
            throw RuntimeException("Storage upload failed")
        }

        // When & Then
        viewModel.state.test {
            assertEquals(SignUpState.Nothing, awaitItem()) // Initial state

            viewModel.signUp(fullName, phoneNumber, email, password, imageUri)
            assertEquals(SignUpState.Loading, awaitItem()) // Loading state
            
            testScheduler.advanceUntilIdle() // Process all coroutines
            assertEquals(SignUpState.Error, awaitItem()) // Error state
        }
    }

    @Test
    fun `signUp with successful image upload but failed registration should emit error`() = runTest {
        // Given
        val fullName = "John Doe"
        val phoneNumber = "1234567890"
        val email = "john@example.com"
        val password = "password123"
        val imageUri = mockk<Uri>()
        val imageUrl = "https://example.com/profile.jpg"
        
        coEvery { storageRepository.uploadProfileImage(imageUri, email) } returns imageUrl
        coEvery { authRepository.register(fullName, phoneNumber, email, password, imageUrl) } coAnswers {
            delay(100)
            Result.failure(Exception("Registration failed after image upload"))
        }

        // When & Then
        viewModel.state.test {
            assertEquals(SignUpState.Nothing, awaitItem()) // Initial state

            viewModel.signUp(fullName, phoneNumber, email, password, imageUri)
            assertEquals(SignUpState.Loading, awaitItem()) // Loading state
            
            testScheduler.advanceUntilIdle() // Process all coroutines
            assertEquals(SignUpState.Error, awaitItem()) // Error state
        }
    }

    @Test
    fun `multiple signUp calls should eventually reach final state`() = runTest {
        // Given
        val fullName = "John Doe"
        val phoneNumber = "1234567890"
        val email = "john@example.com"
        val password = "password123"
        val imageUri: Uri? = null
        
        coEvery { authRepository.register(fullName, phoneNumber, email, password, null) } returns Result.success(true)

        // When
        viewModel.signUp(fullName, phoneNumber, email, password, imageUri)
        viewModel.signUp(fullName, phoneNumber, email, password, imageUri) // Second call
        
        testScheduler.advanceUntilIdle() // Process all coroutines

        // Then - Final state should be Success
        assertEquals(SignUpState.Success, viewModel.state.value)
    }

    @Test
    fun `signUp with failed image upload should proceed with null profile picture`() = runTest {
        // Given
        val fullName = "John Doe"
        val phoneNumber = "1234567890"
        val email = "john@example.com"
        val password = "password123"
        val imageUri = mockk<Uri>()
        
        coEvery { storageRepository.uploadProfileImage(imageUri, email) } coAnswers {
            delay(50)
            null // Upload fails
        }
        coEvery { authRepository.register(fullName, phoneNumber, email, password, null) } coAnswers {
            delay(100)
            Result.success(true)
        }

        // When & Then
        viewModel.state.test {
            assertEquals(SignUpState.Nothing, awaitItem()) // Initial state

            viewModel.signUp(fullName, phoneNumber, email, password, imageUri)
            assertEquals(SignUpState.Loading, awaitItem()) // Loading state
            
            testScheduler.advanceUntilIdle() // Process all coroutines
            assertEquals(SignUpState.Success, awaitItem()) // Success state (registration succeeded despite image upload failure)
        }
    }
}