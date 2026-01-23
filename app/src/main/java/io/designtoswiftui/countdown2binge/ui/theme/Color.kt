package io.designtoswiftui.countdown2binge.ui.theme

import androidx.compose.ui.graphics.Color

// Primary brand colors - warm, inviting purple-rose tones
val Primary = Color(0xFFE8B4BC)
val PrimaryVariant = Color(0xFFD4949E)
val OnPrimary = Color(0xFF1A1A1A)

// Secondary - muted sage for balance
val Secondary = Color(0xFFA8B5A0)
val SecondaryVariant = Color(0xFF8A9A82)
val OnSecondary = Color(0xFF1A1A1A)

// Background layers - deep charcoal with subtle warmth
val Background = Color(0xFF0F0F12)
val Surface = Color(0xFF1A1A1F)
val SurfaceVariant = Color(0xFF252529)
val SurfaceElevated = Color(0xFF2A2A30)

// Text hierarchy
val OnBackground = Color(0xFFF5F5F5)
val OnBackgroundMuted = Color(0xFFB0B0B5)
val OnBackgroundSubtle = Color(0xFF6B6B70)

// State colors - muted, elegant tones
val StateAnticipated = Color(0xFF7A8599)  // Cool slate blue
val StatePremieringFrom = Color(0xFFE8B4BC)  // Warm rose
val StatePremieringTo = Color(0xFFC9A0D4)  // Soft lavender
val StateAiring = Color(0xFF7DD3C0)  // Soft teal
val StateBingeReady = Color(0xFFB8D48A)  // Fresh green
val StateWatched = Color(0xFF6B6B70)  // Muted gray

// Accent colors
val AccentGold = Color(0xFFD4AF37)
val AccentError = Color(0xFFE57373)
val Destructive = Color(0xFFCF6679)  // Muted red for destructive actions

// Detail screen accent - Teal (from iOS design spec #4AC7B8)
val DetailAccent = Color(0xFF4AC7B8)

// Timeline-specific colors
val TimelineAccent = Color(0xFF4ECDC4)  // Bright teal for timeline elements
val TimelineAccentMuted = Color(0xFF4ECDC4).copy(alpha = 0.15f)
val TimelineLine = Color(0xFF4ECDC4).copy(alpha = 0.6f)
val CardBackground = Color(0xFF1A1A1F)
val CardBackgroundElevated = Color(0xFF222228)
val ButtonOutline = Color(0xFF4ECDC4)

// Binge Ready-specific colors (matching iOS)
val BingeReadyAccent = Color(0xFF2BAFA9)  // Teal accent for binge ready elements
val BingeReadyBackground = Color(0xFF000000)  // Pure black background

// Timeline section-specific colors (matching iOS)
val EndingSoonAccent = Color(0xFF73E6B3)      // Teal-green for ending soon (same as premiering)
val PremieringSoonAccent = Color(0xFF73E6B3)  // Teal-green for premiering soon
val AnticipatedAccent = Color(0xFF666666)      // Muted gray for anticipated/TBD

// Gradients
val GradientOverlayStart = Color(0x00000000)
val GradientOverlayEnd = Color(0xE6000000)

// Footer button colors (matching iOS spec)
val FooterButtonBackground = Color(0xFF0D0D0D)
val FooterButtonBorder = Color(0xFF252525)

// Text colors for Timeline UI
val InfoTextColor = Color(0xFF8E8E93)  // Muted gray for info text
val SectionLabelColor = Color(0xFF8E8E93)  // Gray for section labels like "N total"

// Legacy support (keeping for any existing references)
val Purple80 = Primary
val PurpleGrey80 = Secondary
val Pink80 = PrimaryVariant
val Purple40 = PrimaryVariant
val PurpleGrey40 = SecondaryVariant
val Pink40 = Color(0xFF7D5260)
