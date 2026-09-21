package com.matrixmarket.app.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matrixmarket.app.ui.theme.MatrixBlack
import com.matrixmarket.app.ui.theme.RoyalPurple
import com.matrixmarket.app.util.AppContainer
import com.matrixmarket.app.util.Resource
import com.matrixmarket.app.util.ViewModelFactory

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val viewModel: LoginViewModel = viewModel(
        factory = ViewModelFactory { LoginViewModel(AppContainer.authRepository) }
    )
    val authState by viewModel.authState.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var campus by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        if (authState is Resource.Success) onRegisterSuccess()
    }

    Surface(color = MatrixBlack, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Create your account", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Use your campus email to get verified.", color = Color.White.copy(alpha = 0.7f))

            Spacer(Modifier.height(24.dp))

            listOf(
                Triple("Full Name", fullName) { v: String -> fullName = v },
                Triple("Student Email", email) { v: String -> email = v },
                Triple("Campus (optional)", campus) { v: String -> campus = v }
            ).forEach { (label, value, onChange) ->
                OutlinedTextField(
                    value = value,
                    onValueChange = onChange,
                    label = { Text(label) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password (6+ characters)") },
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

            Spacer(Modifier.height(22.dp))

            Button(
                onClick = { viewModel.register(email, password, fullName, campus.ifBlank { null }) },
                enabled = authState !is Resource.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (authState is Resource.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Create Account", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onBackToLogin) {
                Text("Already have an account? Log in", color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}
