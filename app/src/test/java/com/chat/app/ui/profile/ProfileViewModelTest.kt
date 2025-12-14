package com.chat.app.ui.profile

import android.net.Uri
import com.chat.app.data.local.entities.UserEntity
import com.chat.app.data.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var mockUserRepository: UserRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockUserRepository = mockk()
        viewModel = ProfileViewModel(mockUserRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadUserProfile should update UI state with user data`() = runTest {
        // Given
        val testUid = "test_uid"
        val testUser = UserEntity(
            uid = testUid,
            fullName = "John Doe",
            phoneNumber = "+1234567890",
            profilePictureUrl = "https://example.com/profile.jpg"
        )
        coEvery { mockUserRepository.getUser(testUid) } returns testUser

        // When
        viewModel.loadUserProfile(testUid)

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals("John Doe", uiState.username)
        assertEquals("+1234567890", uiState.phone)
        assertEquals("https://example.com/profile.jpg", uiState.profileImageUrl)
        assertEquals(testUid, uiState.uid)
        assertFalse(uiState.isLoading)
    }

    @Test
    fun `loadUserProfile should handle user not found`() = runTest {
        // Given
        val testUid = "nonexistent_uid"
        coEvery { mockUserRepository.getUser(testUid) } returns null

        // When
        viewModel.loadUserProfile(testUid)

        // Then
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoading)
        // Should load from SharedPreferences fallback
    }

    @Test
    fun `updateUsername should update UI state`() = runTest {
        // Given
        val newUsername = "Jane Doe"

        // When
        viewModel.updateUsername(newUsername)

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals(newUsername, uiState.username)
    }

    @Test
    fun `updatePhone should update UI state`() = runTest {
        // Given
        val newPhone = "+9876543210"

        // When
        viewModel.updatePhone(newPhone)

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals(newPhone, uiState.phone)
    }

    @Test
    fun `updateEmail should update UI state`() = runTest {
        // Given
        val newEmail = "jane@example.com"

        // When
        viewModel.updateEmail(newEmail)

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals(newEmail, uiState.email)
    }

    @Test
    fun `updateProfileImage should update UI state`() = runTest {
        // Given
        val mockUri = mockk<Uri>()

        // When
        viewModel.updateProfileImage(mockUri)

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals(mockUri, uiState.profileImageUri)
    }

    @Test
    fun `toggleEditMode should change editing state`() = runTest {
        // Given
        val initialEditingState = viewModel.isEditing.first()

        // When
        viewModel.toggleEditMode()

        // Then
        val newEditingState = viewModel.isEditing.first()
        assertEquals(!initialEditingState, newEditingState)
    }

    @Test
    fun `saveProfile should call repository and update UI state`() = runTest {
        // Given
        viewModel.updateUsername("Test User")
        viewModel.updatePhone("+1234567890")
        coEvery { mockUserRepository.saveUserLocally(any()) } returns Unit

        // When
        viewModel.saveProfile()

        // Then
        coVerify { mockUserRepository.saveUserLocally(any()) }
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isSaving)
    }

    @Test
    fun `saveProfile should handle repository error`() = runTest {
        // Given
        val errorMessage = "Database error"
        coEvery { mockUserRepository.saveUserLocally(any()) } throws Exception(errorMessage)

        // When
        viewModel.saveProfile()

        // Then
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isSaving)
        assertTrue(uiState.errorMessage?.contains("Failed to save profile") == true)
    }

    @Test
    fun `loadFromSharedPreferences should update UI state`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val userData = UserData(
            username = "Shared User",
            phone = "+1111111111",
            email = "shared@example.com",
            profileUri = mockUri
        )

        // When
        viewModel.loadFromSharedPreferences(userData)

        // Then
        val uiState = viewModel.uiState.first()
        assertEquals("Shared User", uiState.username)
        assertEquals("+1111111111", uiState.phone)
        assertEquals("shared@example.com", uiState.email)
        assertEquals(mockUri, uiState.profileImageUri)
        assertFalse(uiState.isLoading)
    }

    @Test
    fun `saveToSharedPreferences should return current UI state as UserData`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        viewModel.updateUsername("Test User")
        viewModel.updatePhone("+1234567890")
        viewModel.updateEmail("test@example.com")
        viewModel.updateProfileImage(mockUri)

        // When
        val userData = viewModel.saveToSharedPreferences()

        // Then
        assertEquals("Test User", userData.username)
        assertEquals("+1234567890", userData.phone)
        assertEquals("test@example.com", userData.email)
        assertEquals(mockUri, userData.profileUri)
    }
}