package com.chat.app.ui.register

import org.junit.Test
import org.junit.Assert.*

class RegisterViewModelTest {

    @Test
    fun `SignUpState Nothing should be initial state`() {
        val state = SignUpState.Nothing
        assertTrue(state is SignUpState.Nothing)
        assertFalse(state is SignUpState.Loading)
        assertFalse(state is SignUpState.Success)
        assertFalse(state is SignUpState.Error)
        assertFalse(state is SignUpState.ValidationError)
    }

    @Test
    fun `SignUpState Loading should be correct type`() {
        val state = SignUpState.Loading
        assertFalse(state is SignUpState.Nothing)
        assertTrue(state is SignUpState.Loading)
        assertFalse(state is SignUpState.Success)
        assertFalse(state is SignUpState.Error)
        assertFalse(state is SignUpState.ValidationError)
    }

    @Test
    fun `SignUpState Success should be correct type`() {
        val state = SignUpState.Success
        assertFalse(state is SignUpState.Nothing)
        assertFalse(state is SignUpState.Loading)
        assertTrue(state is SignUpState.Success)
        assertFalse(state is SignUpState.Error)
        assertFalse(state is SignUpState.ValidationError)
    }

    @Test
    fun `SignUpState Error should contain message`() {
        val errorMessage = "Registration failed"
        val state = SignUpState.Error(errorMessage)
        
        assertFalse(state is SignUpState.Nothing)
        assertFalse(state is SignUpState.Loading)
        assertFalse(state is SignUpState.Success)
        assertTrue(state is SignUpState.Error)
        assertFalse(state is SignUpState.ValidationError)
        assertEquals(errorMessage, state.message)
    }

    @Test
    fun `SignUpState ValidationError should contain message`() {
        val validationMessage = "Please fix the errors above"
        val state = SignUpState.ValidationError(validationMessage)
        
        assertFalse(state is SignUpState.Nothing)
        assertFalse(state is SignUpState.Loading)
        assertFalse(state is SignUpState.Success)
        assertFalse(state is SignUpState.Error)
        assertTrue(state is SignUpState.ValidationError)
        assertEquals(validationMessage, state.message)
    }

    @Test
    fun `SignUpState sealed class should work with when expression`() {
        val testStates = listOf(
            SignUpState.Nothing,
            SignUpState.Loading,
            SignUpState.Success,
            SignUpState.Error("test error"),
            SignUpState.ValidationError("test validation")
        )

        testStates.forEach { state ->
            val result = when (state) {
                is SignUpState.Nothing -> "nothing"
                is SignUpState.Loading -> "loading"
                is SignUpState.Success -> "success"
                is SignUpState.Error -> "error: ${state.message}"
                is SignUpState.ValidationError -> "validation: ${state.message}"
            }
            assertNotNull(result)
        }
    }

    @Test
    fun `username validation helper should work correctly`() {
        val validUsernames = listOf(
            "John Doe",
            "Ahmed Ali",
            "Maria Garcia",
            "Test User"
        )
        
        val invalidUsernames = listOf(
            "",
            "A",
            "AB"
        )

        validUsernames.forEach { username ->
            assertTrue("$username should be valid", username.length >= 3 && username.isNotBlank())
        }

        invalidUsernames.forEach { username ->
            assertFalse("$username should be invalid", username.length >= 3 && username.isNotBlank())
        }
    }

    @Test
    fun `phone validation helper should work correctly`() {
        val validPhones = listOf(
            "01234567890",
            "01012345678",
            "01123456789"
        )
        
        val invalidPhones = listOf(
            "",
            "123",
            "abcdefghijk",
            "012345678901" // too long
        )

        validPhones.forEach { phone ->
            assertTrue("$phone should be valid", 
                phone.length == 11 && phone.startsWith("01") && phone.all { it.isDigit() })
        }

        invalidPhones.forEach { phone ->
            assertFalse("$phone should be invalid", 
                phone.length == 11 && phone.startsWith("01") && phone.all { it.isDigit() })
        }
    }

    @Test
    fun `password validation helper should work correctly`() {
        val validPasswords = listOf(
            "password123",
            "mySecurePass",
            "test1234567"
        )
        
        val invalidPasswords = listOf(
            "",
            "123",
            "short"
        )

        validPasswords.forEach { password ->
            assertTrue("$password should be valid", password.length >= 6)
        }

        invalidPasswords.forEach { password ->
            assertFalse("$password should be invalid", password.length >= 6)
        }
    }

    @Test
    fun `validation errors map should work correctly`() {
        val errors = mapOf(
            "username" to "Username is required",
            "email" to "Invalid email format",
            "password" to "Password too short"
        )

        assertEquals(3, errors.size)
        assertTrue(errors.containsKey("username"))
        assertTrue(errors.containsKey("email"))
        assertTrue(errors.containsKey("password"))
        assertEquals("Username is required", errors["username"])
        assertEquals("Invalid email format", errors["email"])
        assertEquals("Password too short", errors["password"])
    }

    @Test
    fun `empty validation errors map should work correctly`() {
        val errors = emptyMap<String, String>()
        
        assertEquals(0, errors.size)
        assertTrue(errors.isEmpty())
        assertFalse(errors.containsKey("username"))
        assertNull(errors["email"])
    }
}