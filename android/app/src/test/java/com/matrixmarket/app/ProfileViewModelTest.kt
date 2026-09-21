package com.matrixmarket.app

import com.matrixmarket.app.data.local.SessionManager
import com.matrixmarket.app.data.repository.ProfileRepository
import com.matrixmarket.app.ui.screens.profile.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

// Tests for User Defined Feature 3 (Gamified Student Score) trust-tier progress math.
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(kotlinx.coroutines.test.StandardTestDispatcher())
        viewModel = ProfileViewModel(mock<ProfileRepository>(), mock<SessionManager>())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `tierProgress is 0 at the very start of a tier`() {
        assertEquals(0f, viewModel.tierProgress(0), 0.01f)
    }

    @Test
    fun `tierProgress is halfway between 40 and 100 at 70 points`() {
        assertEquals(0.5f, viewModel.tierProgress(70), 0.01f)
    }

    @Test
    fun `tierProgress caps at 1 for the highest tier`() {
        assertEquals(1f, viewModel.tierProgress(500), 0.01f)
    }
}
