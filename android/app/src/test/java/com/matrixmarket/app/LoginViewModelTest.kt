package com.matrixmarket.app

import com.matrixmarket.app.data.remote.dto.AuthResponse
import com.matrixmarket.app.data.repository.AuthRepository
import com.matrixmarket.app.ui.screens.login.LoginViewModel
import com.matrixmarket.app.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock()
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login with blank fields returns a validation error without calling the repository`() = runTest {
        viewModel.login("", "")
        advanceUntilIdle()
        assertTrue(viewModel.authState.value is Resource.Error)
    }

    @Test
    fun `login with valid credentials transitions through Loading to Success`() = runTest {
        val fakeAuth = AuthResponse("fake.jwt.token", 1, "Caleb Naidoo", "st10084625@iie.ac.za", 0)
        whenever(authRepository.login(any(), any())).thenReturn(Resource.Success(fakeAuth))

        viewModel.login("st10084625@iie.ac.za", "password123")
        advanceUntilIdle()

        assertTrue(viewModel.authState.value is Resource.Success)
    }

    @Test
    fun `login surfaces the repository's error message on failure`() = runTest {
        whenever(authRepository.login(any(), any())).thenReturn(Resource.Error("Invalid email or password."))

        viewModel.login("student@iie.ac.za", "wrongpassword")
        advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is Resource.Error)
        assertEquals("Invalid email or password.", (state as Resource.Error).message)
    }

    @Test
    fun `register rejects a password shorter than 6 characters`() = runTest {
        viewModel.register("student@iie.ac.za", "123", "Student Name", null)
        advanceUntilIdle()
        assertTrue(viewModel.authState.value is Resource.Error)
    }

    @Test
    fun `resetState returns the state to Idle`() {
        viewModel.resetState()
        assertEquals(Resource.Idle, viewModel.authState.value)
    }
}
