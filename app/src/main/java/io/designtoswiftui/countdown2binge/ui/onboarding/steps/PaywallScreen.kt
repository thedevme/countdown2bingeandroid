package io.designtoswiftui.countdown2binge.ui.onboarding.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.onboarding.PremiumFeatures
import io.designtoswiftui.countdown2binge.ui.onboarding.PricingOption
import io.designtoswiftui.countdown2binge.ui.onboarding.ShowSummary
import io.designtoswiftui.countdown2binge.ui.onboarding.components.FeatureRow
import io.designtoswiftui.countdown2binge.ui.onboarding.components.PricingCard
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundSubtle
import io.designtoswiftui.countdown2binge.ui.theme.SurfaceVariant
import kotlinx.coroutines.delay

/**
 * Paywall screen (Step 6).
 * Shows premium features and pricing options.
 */
@Composable
fun PaywallScreen(
    selectedShows: Map<Int, ShowSummary>,
    selectedPricingOption: PricingOption,
    isPurchasing: Boolean,
    purchaseError: String?,
    onPricingOptionSelect: (PricingOption) -> Unit,
    onStartTrial: () -> Unit,
    onContinueFree: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHeader by remember { mutableStateOf(false) }
    var showFeatures by remember { mutableStateOf(false) }
    var showPricing by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    // Staggered animations
    LaunchedEffect(Unit) {
        delay(100)
        showHeader = true
        delay(150)
        showFeatures = true
        delay(150)
        showPricing = true
        delay(150)
        showButton = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Header
        AnimatedVisibility(
            visible = showHeader,
            enter = fadeIn() + slideInVertically { 20 }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (selectedShows.isNotEmpty()) {
                    Text(
                        text = "You're tracking ${selectedShows.size} shows",
                        fontSize = 14.sp,
                        color = OnBackgroundMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = "Unlock the full\nexperience",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground,
                    textAlign = TextAlign.Center,
                    lineHeight = 38.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Features list
        AnimatedVisibility(
            visible = showFeatures,
            enter = fadeIn() + slideInVertically { 30 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PremiumFeatures.features.forEach { feature ->
                    FeatureRow(feature = feature)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pricing options
        AnimatedVisibility(
            visible = showPricing,
            enter = fadeIn() + slideInVertically { 40 }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PricingCard(
                    option = PricingOption.MONTHLY,
                    price = "$2.99",
                    period = "/month",
                    isSelected = selectedPricingOption == PricingOption.MONTHLY,
                    onClick = { onPricingOptionSelect(PricingOption.MONTHLY) }
                )

                PricingCard(
                    option = PricingOption.YEARLY,
                    price = "$19.99",
                    period = "/year",
                    isSelected = selectedPricingOption == PricingOption.YEARLY,
                    onClick = { onPricingOptionSelect(PricingOption.YEARLY) },
                    badge = "SAVE 44%"
                )

                PricingCard(
                    option = PricingOption.LIFETIME,
                    price = "$29.99",
                    period = "once",
                    isSelected = selectedPricingOption == PricingOption.LIFETIME,
                    onClick = { onPricingOptionSelect(PricingOption.LIFETIME) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Error message
        if (purchaseError != null) {
            Text(
                text = purchaseError,
                fontSize = 14.sp,
                color = Color(0xFFE57373),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Start trial button
        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn() + slideInVertically { 20 }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onStartTrial,
                    enabled = !isPurchasing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DetailAccent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (isPurchasing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Start 7-Day Free Trial",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Continue with limited features
                Text(
                    text = "Continue with limited features",
                    fontSize = 14.sp,
                    color = OnBackgroundSubtle,
                    modifier = Modifier
                        .clickable(enabled = !isPurchasing) { onContinueFree() }
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
