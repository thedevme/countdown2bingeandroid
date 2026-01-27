package io.designtoswiftui.countdown2binge.ui.onboarding.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.ui.onboarding.OnboardingUiState
import io.designtoswiftui.countdown2binge.ui.onboarding.ShowSummary
import io.designtoswiftui.countdown2binge.ui.onboarding.components.StepIndicator
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.Surface
import io.designtoswiftui.countdown2binge.ui.theme.SurfaceVariant
import kotlinx.coroutines.delay

/**
 * Completion screen (Step 5).
 * Shows selected shows and smart notifications callout.
 */
@Composable
fun CompletionScreen(
    selectedShows: Map<Int, ShowSummary>,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCheckmark by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    val checkmarkScale by animateFloatAsState(
        targetValue = if (showCheckmark) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "checkmarkScale"
    )

    // Staggered animations
    LaunchedEffect(Unit) {
        delay(200)
        showCheckmark = true
        delay(300)
        showTitle = true
        delay(200)
        showContent = true
        delay(200)
        showButton = true
    }

    val showsList = selectedShows.values.toList()
    val displayedShows = showsList.take(3)
    val extraCount = (showsList.size - 3).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Step indicator
        StepIndicator(
            currentStep = 5,
            totalSteps = OnboardingUiState.TOTAL_STEPS,
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.Start)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Checkmark
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(checkmarkScale)
                .clip(CircleShape)
                .background(DetailAccent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Complete",
                tint = Color.Black,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        AnimatedVisibility(
            visible = showTitle,
            enter = fadeIn() + slideInVertically { 20 }
        ) {
            Text(
                text = "You're All Set",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Followed shows card
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically { 30 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariant)
                    .padding(16.dp)
            ) {
                Text(
                    text = "FOLLOWED",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackgroundMuted,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Poster row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    displayedShows.forEach { show ->
                        AsyncImage(
                            model = show.posterPath?.let { "https://image.tmdb.org/t/p/w185$it" },
                            contentDescription = show.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(80.dp)
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Surface)
                        )
                    }
                }

                if (extraCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "+$extraCount MORE IN YOUR LIST",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnBackgroundMuted,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Smart notifications card
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically { 40 }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariant)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = DetailAccent,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "SMART NOTIFICATIONS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DetailAccent,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Get alerts for new episodes and seasons",
                        fontSize = 14.sp,
                        color = OnBackgroundMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Continue button
        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn() + slideInVertically { 20 }
        ) {
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DetailAccent,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
