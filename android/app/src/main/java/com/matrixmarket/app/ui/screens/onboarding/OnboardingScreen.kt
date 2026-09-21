package com.matrixmarket.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matrixmarket.app.ui.theme.*

// Matches the dark, neon-purple "Campus Verified" onboarding mockup from the Part 1 design doc.
@Composable
fun OnboardingScreen(
    onContinueWithEmail: () -> Unit,
    onContinueWithGoogle: () -> Unit
) {
    Surface(color = MatrixBlack, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(NeonPurple.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(36.dp))
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "Campus Verified.",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "The safest way to trade textbooks, gear, and essentials at your institution.",
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(Modifier.weight(1f))

            Text(
                "Build your Campus Trust Score by completing secure trades.",
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = onContinueWithEmail,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Continue with Student Email", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onContinueWithGoogle) {
                Text("Or log in with Google SSO", color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}
