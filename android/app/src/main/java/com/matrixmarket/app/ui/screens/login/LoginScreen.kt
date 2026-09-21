package com.matrixmarket.app.ui.screens.login

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matrixmarket.app.auth.GoogleAuthManager
import com.matrixmarket.app.ui.theme.MatrixBlack
import com.matrixmarket.app.ui.theme.RoyalPurple
import com.matrixmarket.app.util.AppContainer
import com.matrixmarket.app.util.Resource
import com.matrixmarket.app.util.ViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel(
        factory = ViewModelFactory { LoginViewModel(AppContainer.authRepository) }
    )
    val authState by viewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val googleAuthManager = remember { GoogleAuthManager(context) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        scope.launch {
            // Handle result even if resultCode != RESULT_OK to extract detailed ApiException details
            googleAuthManager.handleSignInResult(result.data).onSuccess { sso ->
                viewModel.completeSsoLogin(sso.email, sso.fullName)
            }.onFailure { error ->
                viewModel.reportSsoFailure("Google SSO Failed: ${error.message}")
            }
        }
    }

    LaunchedEffect(authState) {
        if (authState is Resource.Success) onLoginSuccess()
    }

    Surface(color = MatrixBlack, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome to Matrix Market", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Sign in", color = Color.White.copy(alpha = 0.7f))

            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Student Email") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            if (authState is Resource.Error) {
                Spacer(Modifier.height(10.dp))
                Text((authState as Resource.Error).message, color = Color(0xFFFF6B6B), fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { viewModel.login(email, password) },
                enabled = authState !is Resource.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (authState is Resource.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Login", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    scope.launch {
                        googleSignInLauncher.launch(googleAuthManager.getSignInIntent())
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Continue with Google SSO", color = Color.White)
            }

            Spacer(Modifier.height(14.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text("Create Account", color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}
