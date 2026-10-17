package com.lk.lankaboardinghouse.android.ui.login

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        viewModel = LoginViewModel()
    }

    @Test
    fun `empty email returns error`() {
        viewModel.email = ""
        viewModel.password = "password123"

        viewModel.login()

        val state = viewModel.uiState
        assertTrue(state is LoginUiState.Error)
        assertEquals("Email and password are required", (state as LoginUiState.Error).message)
    }

    @Test
    fun `empty password returns error`() {
        viewModel.email = "test@test.com"
        viewModel.password = ""

        viewModel.login()

        val state = viewModel.uiState
        assertTrue(state is LoginUiState.Error)
        assertEquals("Email and password are required", (state as LoginUiState.Error).message)
    }
}