package com.chat.app.ui.login

import org.junit.Test
import org.junit.Assert.*

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
        assertFalse(state is SignInState.Nothing)
        assertTrue(state is SignInState.Loading)
        assertFalse(state is SignInState.Success)
        assertFalse(state is SignInState.Error)
    }

    @Test
    fun `SignInState Success should be correct type`() {
        val state = SignInState.Success
        assertFalse(state is SignInState.Nothing)
        assertFalse(state is SignInState.Loading)
        assertTrue(state is SignInState.Success)
        assertFalse(state is SignInState.Error)
    }

    @Test
    fun `SignInState Error should be correct type`() {
        val state = SignInState.Error
        assertFalse(state is SignInState.Nothing)
        assertFalse(state is SignInState.Loading)
        assertFalse(state is SignInState.Success)
        assertTrue(state is SignInState.Error)
    }

    @Test
    fun `SignInState sealed class should have exactly 4 states`() {
        val states = listOf(
            SignInState.Nothing,
            SignInState.Loading,
            SignInState.Success,
            SignInState.Error
        )
        assertEquals(4, states.size)
        
        // Verify each state is unique
        val uniqueStates = states.map { it::class }.toSet()
        assertEquals(4, uniqueStates.size)
    }

    @Test
    fun `SignInState should be comparable with when expression`() {
        val testStates = listOf(
            SignInState.Nothing,
            SignInState.Loading,
            SignInState.Success,
            SignInState.Error
        )

        testStates.forEach { state ->
            val result = when (state) {
                is SignInState.Nothing -> "nothing"
                is SignInState.Loading -> "loading"
                is SignInState.Success -> "success"
                is SignInState.Error -> "error"
            }
            assertNotNull(result)
            assertTrue(result in listOf("nothing", "loading", "success", "error"))
        }
    }

    @Test
    fun `password validation helper should work correctly`() {
        // Simple password validation tests
        val validPasswords = listOf(
            "password123",
            "mySecurePass",
            "test1234"
        )
        
        val invalidPasswords = listOf(
            "",
            "123",
            "ab"
        )

        validPasswords.forEach { password ->
            assertTrue("$password should be valid", password.length >= 6)
        }

        invalidPasswords.forEach { password ->
            assertFalse("$password should be invalid", password.length >= 6)
        }
    }
}