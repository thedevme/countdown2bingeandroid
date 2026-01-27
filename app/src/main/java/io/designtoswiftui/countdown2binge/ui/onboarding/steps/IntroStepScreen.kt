package io.designtoswiftui.countdown2binge.ui.onboarding.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.onboarding.IntroStepContent
import io.designtoswiftui.countdown2binge.ui.onboarding.OnboardingUiState
import io.designtoswiftui.countdown2binge.ui.onboarding.components.StepIndicator
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundSubtle
import kotlinx.coroutines.delay

/**
 * Intro step screen (Steps 1-3).
 * Displays pain-agitate-solution messaging with optional sign-in.
 */
@Composable
fun IntroStepScreen(
    content: IntroStepContent,
    isSigningIn: Boolean,
    isSyncing: Boolean,
    signInError: String?,
    onContinue: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTitle by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }

    // Staggered animation on entry
    LaunchedEffect(content.step) {
        showTitle = false
        showSubtitle = false
        showButtons = false
        delay(100)
        showTitle = true
        delay(100)
        showSubtitle = true
        delay(100)
        showButtons = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Step indicator
        StepIndicator(
            currentStep = content.step,
            totalSteps = OnboardingUiState.TOTAL_STEPS,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Title
        AnimatedVisibility(
            visible = showTitle,
            enter = fadeIn() + slideInVertically { 20 }
        ) {
            Text(
                text = content.title,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Subtitle
        AnimatedVisibility(
            visible = showSubtitle,
            enter = fadeIn() + slideInVertically { 20 }
        ) {
            Text(
                text = content.subtitle,
                fontSize = 17.sp,
                color = OnBackgroundMuted,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Sign In section (Step 1 only)
        AnimatedVisibility(
            visible = showButtons && content.showSignIn,
            enter = fadeIn() + slideInVertically { 20 }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                // Sign In button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = !isSigningIn && !isSyncing) { onSignIn() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSigningIn || isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = DetailAccent,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Already have an account?\nSign in to restore your shows",
                    fontSize = 12.sp,
                    color = OnBackgroundSubtle,
                    textAlign = TextAlign.Center
                )

                // Error message
                if (signInError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = signInError,
                        fontSize = 12.sp,
                        color = Color(0xFFE57373),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Continue button
        AnimatedVisibility(
            visible = showButtons,
            enter = fadeIn() + slideInVertically { 20 }
        ) {
            Button(
                onClick = onContinue,
                enabled = !isSigningIn && !isSyncing,
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
                    text = content.buttonText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
