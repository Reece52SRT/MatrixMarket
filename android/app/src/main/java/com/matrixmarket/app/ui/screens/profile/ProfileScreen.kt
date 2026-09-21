package com.matrixmarket.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matrixmarket.app.R
import com.matrixmarket.app.ui.theme.LavenderCard
import com.matrixmarket.app.ui.theme.LavenderLight
import com.matrixmarket.app.ui.theme.RisingTraderTeal
import com.matrixmarket.app.ui.theme.RoyalPurple
import com.matrixmarket.app.ui.theme.badgeColor
import com.matrixmarket.app.ui.theme.tierColor
import com.matrixmarket.app.util.AppContainer
import com.matrixmarket.app.util.Resource
import com.matrixmarket.app.util.ViewModelFactory

// User Defined Feature 3: Gamified Student Score. Shows karma points, trust tier
// progress (color-coded per tier), campus + member-since, trades completed, and
// earned AND locked trust badges (awarded server-side on confirmed trades).
@Composable
fun ProfileScreen() {
    val viewModel: ProfileViewModel = viewModel(
        factory = ViewModelFactory { ProfileViewModel(AppContainer.profileRepository, AppContainer.sessionManager) }
    )
    val state by viewModel.profileState.collectAsState()

    Surface(color = LavenderLight, modifier = Modifier.fillMaxSize()) {
        when (val s = state) {
            is Resource.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RoyalPurple)
            }
            is Resource.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.message)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadProfile() }) { Text("Retry") }
                }
            }
            is Resource.Success -> {
                val profile = s.data
                val currentTierColor = tierColor(profile.trustTier)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(64.dp).background(currentTierColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = currentTierColor, modifier = Modifier.size(32.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(profile.fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(profile.studentEmail, color = Color.Gray, fontSize = 13.sp)
                            // Campus + Member since - both already returned by the backend,
                            // just not shown here before.
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!profile.campus.isNullOrBlank()) {
                                    Icon(Icons.Filled.School, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(3.dp))
                                    Text(profile.campus, color = Color.Gray, fontSize = 12.sp)
                                    Spacer(Modifier.width(10.dp))
                                }
                                Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    stringResource(R.string.member_since, viewModel.formatMemberSince(profile.memberSince)),
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Student Score card - now color-coded per tier, so the card itself
                    // visibly "levels up" (grey -> teal -> gold -> deep purple) as the
                    // student's trust tier rises, instead of always looking the same.
                    Surface(color = Color.White, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.student_score), fontWeight = FontWeight.SemiBold)
                                Text("${profile.karmaPoints} pts", color = currentTierColor, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { viewModel.tierProgress(profile.karmaPoints) },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = currentTierColor,
                                trackColor = currentTierColor.copy(alpha = 0.15f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(color = currentTierColor.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp)) {
                                    Text(
                                        profile.trustTier,
                                        color = currentTierColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                val nextTier = viewModel.pointsToNextTier(profile.karmaPoints)
                                Text(
                                    if (nextTier != null) stringResource(R.string.points_to_go, nextTier.first, nextTier.second)
                                    else stringResource(R.string.top_tier_reached),
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Trades Completed - a fresh stat card using data the backend already
                    // computes for badge eligibility but never surfaced to the app before.
                    Surface(color = Color.White, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = RisingTraderTeal)
                            Spacer(Modifier.width(10.dp))
                            Text(stringResource(R.string.trades_completed), modifier = Modifier.weight(1f))
                            Text("${profile.tradesCompleted}", fontWeight = FontWeight.Bold, color = RisingTraderTeal, fontSize = 18.sp)
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text(stringResource(R.string.trust_badges), fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))

                    // Every possible badge is shown now, not just earned ones: earned
                    // badges render in their full color, locked ones render greyed-out
                    // with a padlock and their unlock condition, so students know what
                    // to aim for next.
                    val allBadges = viewModel.allBadges(profile.badges, profile.karmaPoints, profile.tradesCompleted)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(allBadges) { badge ->
                            val color = if (badge.earned) badgeColor(badge.type) else Color.Gray
                            Surface(
                                color = if (badge.earned) badgeColor(badge.type).copy(alpha = 0.15f) else LavenderCard,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp).widthIn(max = 160.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (badge.earned) Icons.Filled.EmojiEvents else Icons.Filled.Lock,
                                            contentDescription = null,
                                            tint = color,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(badge.type, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (badge.earned) Color.Black else Color.Gray)
                                    }
                                    if (!badge.earned) {
                                        Spacer(Modifier.height(3.dp))
                                        Text(badge.description, fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Surface(color = Color.White, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.active_listings))
                            Text("${profile.listingsCount}", fontWeight = FontWeight.Bold, color = RoyalPurple)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
            Resource.Idle -> Unit
        }
    }
}
