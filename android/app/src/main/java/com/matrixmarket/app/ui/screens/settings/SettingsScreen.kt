package com.matrixmarket.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matrixmarket.app.R
import com.matrixmarket.app.ui.theme.LavenderLight
import com.matrixmarket.app.ui.theme.RoyalPurple
import com.matrixmarket.app.util.AppContainer
import com.matrixmarket.app.util.ViewModelFactory

// Matches the "Find Setting" mockup: grouped settings cards with clear labels.
@Composable
fun SettingsScreen(onLoggedOut: () -> Unit) {
    val viewModel: SettingsViewModel = viewModel(
        factory = ViewModelFactory {
            SettingsViewModel(AppContainer.sessionManager, AppContainer.profileRepository, AppContainer.authRepository)
        }
    )
    val state by viewModel.uiState.collectAsState()
    // Used to force-refresh the screen right after a language change. AppCompatDelegate
    // applies the new locale, but MainActivity is a plain ComponentActivity (not
    // AppCompatActivity), which is the class that normally auto-recreates itself on a
    // locale change. Some devices/API levels pick the new language up on their own;
    // others (MIUI in particular) need this explicit nudge to actually redraw with it.
    val activity = LocalContext.current as? android.app.Activity

    Surface(color = LavenderLight, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(stringResource(R.string.settings), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(18.dp))

            SettingsCard(icon = Icons.Filled.Person, title = stringResource(R.string.profile)) {
                OutlinedTextField(
                    value = state.fullName,
                    onValueChange = viewModel::onFullNameChanged,
                    label = { Text(stringResource(R.string.full_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.saveProfile() },
                    enabled = !state.isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple)
                ) {
                    Text(if (state.isSaving) stringResource(R.string.saving) else stringResource(R.string.save), color = Color.White)
                }
                state.saveMessage?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(14.dp))

            SettingsCard(icon = Icons.Filled.Language, title = stringResource(R.string.language)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("en" to "English", "af" to "Afrikaans", "zu" to "isiZulu").forEach { (code, label) ->
                        FilterChip(
                            selected = state.language == code,
                            onClick = {
                                viewModel.onLanguageSelected(code)
                                activity?.recreate()
                            },
                            label = { Text(label) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            SettingsCard(icon = Icons.Filled.Notifications, title = stringResource(R.string.notifications)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.notifications_desc))
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = viewModel::onNotificationsToggled,
                        colors = SwitchDefaults.colors(checkedThumbColor = RoyalPurple)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = { viewModel.logout(onLoggedOut) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.log_out))
            }
        }
    }
}

@Composable
private fun SettingsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(color = Color.White, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = RoyalPurple)
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}
