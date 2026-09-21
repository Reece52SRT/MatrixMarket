package com.matrixmarket.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrixmarket.app.data.local.SessionManager
import com.matrixmarket.app.data.remote.dto.UserProfileResponse
import com.matrixmarket.app.data.repository.ProfileRepository
import com.matrixmarket.app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// One entry in the Trust Badges row - "earned" tells the UI whether to render it
// in full color or as a greyed-out "locked" preview with its unlock condition.
data class BadgeDisplay(
    val type: String,
    val description: String,
    val earned: Boolean
)

// Drives User Defined Feature 3: the Gamified Student Score profile - karma points,
// trust tier, trades completed, membership date, and both earned AND locked
// trust badges, all sourced live from GET /users/{id}/profile.
class ProfileViewModel(
    private val repository: ProfileRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _profileState = MutableStateFlow<Resource<UserProfileResponse>>(Resource.Loading)
    val profileState: StateFlow<Resource<UserProfileResponse>> = _profileState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        _profileState.value = Resource.Loading
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            if (userId == null) {
                _profileState.value = Resource.Error("You need to be logged in to view your profile.")
                return@launch
            }
            _profileState.value = repository.getProfile(userId)
        }
    }

    // Returns 0f..1f progress toward the next trust tier, purely for the progress bar UI.
    private val tierThresholds = listOf(0, 40, 100, 200)
    private val tierNames = listOf("New Trader", "Rising Trader", "Trusted Trader", "Campus Legend")

    fun tierProgress(karmaPoints: Int): Float {
        val next = tierThresholds.firstOrNull { it > karmaPoints } ?: return 1f
        val prevIndex = tierThresholds.indexOf(next) - 1
        val prev = if (prevIndex >= 0) tierThresholds[prevIndex] else 0
        val range = (next - prev).coerceAtLeast(1)
        return ((karmaPoints - prev).toFloat() / range).coerceIn(0f, 1f)
    }

    // "23 pts to Trusted Trader" - null once the top tier is reached.
    fun pointsToNextTier(karmaPoints: Int): Pair<Int, String>? {
        val nextIndex = tierThresholds.indexOfFirst { it > karmaPoints }
        if (nextIndex == -1) return null
        return (tierThresholds[nextIndex] - karmaPoints) to tierNames[nextIndex]
    }

    // Every badge that exists in the system (mirrors KarmaService.EvaluateBadgesAsync
    // server-side), paired with whether THIS user has earned it yet, so the UI can
    // show locked previews for badges still to come - not just the ones you have.
    fun allBadges(earnedBadges: List<String>, karmaPoints: Int, tradesCompleted: Int): List<BadgeDisplay> {
        val definitions = listOf(
            Triple("First Trade", "Complete your first verified trade", tradesCompleted >= 1),
            Triple("Top Seller", "Complete 5 verified trades", tradesCompleted >= 5),
            Triple("Trusted Trader", "Reach 100 Student Score points", karmaPoints >= 100)
        )
        return definitions.map { (type, desc, _) ->
            BadgeDisplay(type = type, description = desc, earned = earnedBadges.contains(type))
        }
    }

    // Formats the ISO-8601 "memberSince" timestamp from the backend into "September 2026".
    fun formatMemberSince(isoDate: String): String {
        return try {
            val parsed = java.time.OffsetDateTime.parse(isoDate)
            parsed.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy"))
        } catch (e: Exception) {
            try {
                val parsed = java.time.LocalDateTime.parse(isoDate)
                parsed.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy"))
            } catch (e2: Exception) {
                isoDate.take(7) // fallback: "2026-09"
            }
        }
    }
}
