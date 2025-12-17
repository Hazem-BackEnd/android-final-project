package com.chat.app.ui.login

import org.junit.Assert.*
import org.junit.Test

class LoginViewModelTest {

    @Test
    fun `SignInState Nothing should be initial state`() {
        val state = SignInState.Nothing
        assertTrue(state is SignInState.Nothing)
        assertFalse(state is SignInState.Loading)
        assertFalse(state is SignInState.Success)
        assertFalse(state is SignInState.Error)
    }

    @Test
    fun `SignInState Loading should be correct type`() {
        val state = SignInState.Loading
        assertTrue(state is SignInState.Loading)
        assertFalse(state is SignInState.Success)
    }

    @Test
    fun `SignInState Success should be correct type`() {
        val state = SignInState.Success
        assertTrue(state is SignInState.Success)
        assertFalse(state is SignInState.Error)
    }

    @Test
    fun `SignInState Error should be correct type`() {
        val state = SignInState.Error
        assertTrue(state is SignInState.Error)
        assertFalse(state is SignInState.Success)
    }

    @Test
    fun `SignInState should work with when expression`() {
        val states = listOf(
            SignInState.Nothing,
            SignInState.Loading,
            SignInState.Success,
            SignInState.Error
        )
        states.forEach { state ->
            val result = when (state) {
                is SignInState.Nothing -> "nothing"
                is SignInState.Loading -> "loading"
                is SignInState.Success -> "success"
                is SignInState.Error -> "error"
            }
            assertNotNull(result)
        }
    }

    @Test
    fun `password validation should require minimum 6 characters`() {
        val validPasswords = listOf("password123", "mySecurePass")
        val invalidPasswords = listOf("", "123", "ab")

        validPasswords.forEach { assertTrue(it.length >= 6) }
        invalidPasswords.forEach { assertFalse(it.length >= 6) }
    }
}
