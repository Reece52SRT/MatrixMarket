package com.matrixmarket.app.ui.theme

import androidx.compose.ui.graphics.Color

// Palette pulled directly from the Part 1 UI mockups: dark neon-purple onboarding/login
// screens, and a light lavender palette for the main marketplace/browsing screens.
val MatrixBlack = Color(0xFF0D0D0F)
val MatrixGold = Color(0xFFD4AF37)
val RoyalPurple = Color(0xFF6B4EFF)
val NeonPurple = Color(0xFF9D7BFF)
val LavenderLight = Color(0xFFEDE7FA)
val LavenderCard = Color(0xFFD8CCF5)
val TextDark = Color(0xFF1A1A1D)
val SuccessGreen = Color(0xFF2ECC71)
val WarningAmber = Color(0xFFF2A93B)
val White = Color(0xFFFFFFFF)

// Extra accents so different actions/listing types read distinctly instead of
// everything being the same purple: Sell/Trade/Lend get their own color, and
// scheduling a meeting gets its own teal accent instead of reusing RoyalPurple.
val SellPurple = RoyalPurple
val TradeTeal = Color(0xFF1FA7A0)
val LendAmber = Color(0xFFE08A3C)
val MeetingTeal = Color(0xFF2E8B8B)

fun listingTypeColor(listingType: String): Color = when (listingType.lowercase()) {
    "trade" -> TradeTeal
    "lend" -> LendAmber
    else -> SellPurple
}

// Profile / Student Score accents: each trust tier gets its own color so the
// progress card visibly "levels up" in color, not just in number.
val NewTraderGray = Color(0xFF9C92C2)
val RisingTraderTeal = TradeTeal
val TrustedTraderGold = MatrixGold
val CampusLegendPurple = Color(0xFF4B2E9E)

fun tierColor(tier: String): Color = when (tier) {
    "Campus Legend" -> CampusLegendPurple
    "Trusted Trader" -> TrustedTraderGold
    "Rising Trader" -> RisingTraderTeal
    else -> NewTraderGray
}

// Each earnable Trust Badge gets a distinct color so the badge row itself is
// colorful (not just green/grey), and unearned badges reuse the same color at
// low alpha for their "locked" state so it's clear which badge each lock belongs to.
fun badgeColor(badgeType: String): Color = when (badgeType) {
    "First Trade" -> RisingTraderTeal
    "Top Seller" -> TrustedTraderGold
    "Trusted Trader" -> CampusLegendPurple
    else -> RoyalPurple
}