package com.lk.lankaboardinghouse.android.ui.account

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChangePasswordViewModelTest {

    private lateinit var viewModel: ChangePasswordViewModel

    @Before
    fun setup() {
        viewModel = ChangePasswordViewModel()
    }

    @Test
    fun `empty fields returns error`() {
        viewModel.submit()
        val state = viewModel.state
        assertTrue(state is ChangePasswordState.Error)
        assertEquals("Please fill in all fields", (state as ChangePasswordState.Error).message)
    }

    @Test
    fun `new password too short returns error`() {
        viewModel.currentPassword = "oldPass"
        viewModel.newPassword = "123"
        viewModel.confirmPassword = "123"

        viewModel.submit()
        val state = viewModel.state
        assertTrue(state is ChangePasswordState.Error)
        assertEquals("New password must be at least 6 characters", (state as ChangePasswordState.Error).message)
    }

    @Test
    fun `passwords do not match returns error`() {
        viewModel.currentPassword = "oldPass"
        viewModel.newPassword = "newPass123"
        viewModel.confirmPassword = "different123"

        viewModel.submit()
        val state = viewModel.state
        assertTrue(state is ChangePasswordState.Error)
        assertEquals("New passwords do not match", (state as ChangePasswordState.Error).message)
    }

    @Test
    fun `new password same as current returns error`() {
        viewModel.currentPassword = "samePass"
        viewModel.newPassword = "samePass"
        viewModel.confirmPassword = "samePass"

        viewModel.submit()
        val state = viewModel.state
        assertTrue(state is ChangePasswordState.Error)
        assertEquals("New password must be different from the current one", (state as ChangePasswordState.Error).message)
    }
}