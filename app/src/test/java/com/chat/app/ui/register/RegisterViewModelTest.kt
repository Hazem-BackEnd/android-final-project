package com.chat.app.ui.register

import org.junit.Assert.*
import org.junit.Test

class RegisterViewModelTest {

    @Test
    fun `SignUpState Nothing should be initial state`() {
        val state = SignUpState.Nothing
        assertTrue(state is SignUpState.Nothing)
        assertFalse(state is SignUpState.Loading)
        assertFalse(state is SignUpState.Success)
    }

    @Test
    fun `SignUpState Error should contain message`() {
        val errorMessage = "Registration failed"
        val state = SignUpState.Error(errorMessage)
        assertTrue(state is SignUpState.Error)
        assertEquals(errorMessage, state.message)
    }

    @Test
    fun `SignUpState ValidationError should contain message`() {
        val validationMessage = "Please fix the errors above"
        val state = SignUpState.ValidationError(validationMessage)
        assertTrue(state is SignUpState.ValidationError)
        assertEquals(validationMessage, state.message)
    }

    @Test
    fun `SignUpState should work with when expression`() {
        val states = listOf(
            SignUpState.Nothing,
            SignUpState.Loading,
            SignUpState.Success,
            SignUpState.Error("error"),
            SignUpState.ValidationError("validation")
        )
        states.forEach { state ->
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
    fun `validation errors map should work correctly`() {
        val errors = mapOf(
            "username" to "Username is required",
            "email" to "Invalid email format",
            "password" to "Password too short"
        )
        assertEquals(3, errors.size)
        assertTrue(errors.containsKey("username"))
        assertEquals("Invalid email format", errors["email"])
    }

    @Test
    fun `phone validation should require 11 digits starting with 01`() {
        val validPhones = listOf("01234567890", "01012345678")
        val invalidPhones = listOf("", "123", "abcdefghijk")

        validPhones.forEach { phone ->
            assertTrue(phone.length == 11 && phone.startsWith("01") && phone.all { it.isDigit() })
        }
        invalidPhones.forEach { phone ->
            assertFalse(phone.length == 11 && phone.startsWith("01") && phone.all { it.isDigit() })
        }
    }
}
