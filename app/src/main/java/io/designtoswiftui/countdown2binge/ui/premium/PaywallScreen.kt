package io.designtoswiftui.countdown2binge.ui.premium

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.SurfaceVariant
import io.designtoswiftui.countdown2binge.viewmodels.PaywallViewModel
import kotlinx.coroutines.delay

/**
 * Standalone paywall screen for upgrading to premium.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    onBackClick: () -> Unit,
    onPurchaseComplete: () -> Unit = {},
    viewModel: PaywallViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val isPurchasing by viewModel.isPurchasing.collectAsState()
    val purchaseError by viewModel.purchaseError.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()

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

    // Handle successful purchase
    LaunchedEffect(isPremium) {
        if (isPremium) {
            onPurchaseComplete()
            onBackClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = OnBackground
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Background
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            AnimatedVisibility(
                visible = showHeader,
                enter = fadeIn() + slideInVertically { -40 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Unlock Premium",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Get the full Countdown2Binge experience",
                        fontSize = 16.sp,
                        color = OnBackgroundMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Features
            AnimatedVisibility(
                visible = showFeatures,
                enter = fadeIn() + slideInVertically { 40 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceVariant)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PremiumFeatureRow(
                        icon = Icons.Default.Star,
                        title = "Unlimited Shows",
                        description = "Track as many shows as you want"
                    )
                    PremiumFeatureRow(
                        icon = Icons.Default.Cloud,
                        title = "Cloud Sync",
                        description = "Sync your shows across all devices"
                    )
                    PremiumFeatureRow(
                        icon = Icons.Default.Notifications,
                        title = "Smart Notifications",
                        description = "Get reminders for premieres and finales"
                    )
                    PremiumFeatureRow(
                        icon = Icons.Default.Timer,
                        title = "Countdown Widgets",
                        description = "See countdowns on your home screen"
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Pricing
            AnimatedVisibility(
                visible = showPricing,
                enter = fadeIn() + slideInVertically { 40 }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PricingOption(
                        title = "Annual",
                        price = "$19.99/year",
                        savings = "Save 58%",
                        isSelected = true,
                        onClick = { }
                    )
                    PricingOption(
                        title = "Monthly",
                        price = "$3.99/month",
                        savings = null,
                        isSelected = false,
                        onClick = { }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Purchase Button
            AnimatedVisibility(
                visible = showButton,
                enter = fadeIn() + slideInVertically { 40 }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            activity?.let { viewModel.purchase(it) }
                        },
                        enabled = !isPurchasing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DetailAccent
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        if (isPurchasing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Start 7-Day Free Trial",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Then $19.99/year. Cancel anytime.",
                        fontSize = 13.sp,
                        color = OnBackgroundMuted
                    )

                    if (purchaseError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = purchaseError ?: "",
                            fontSize = 13.sp,
                            color = Color(0xFFE57373)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Restore Purchases",
                        fontSize = 14.sp,
                        color = DetailAccent,
                        modifier = Modifier.clickable {
                            viewModel.restorePurchases()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PremiumFeatureRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DetailAccent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DetailAccent,
                modifier = Modifier.size(24.dp)
            )
        }
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnBackground
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = OnBackgroundMuted
            )
        }
    }
}

@Composable
private fun PricingOption(
    title: String,
    price: String,
    savings: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) DetailAccent.copy(alpha = 0.1f) else SurfaceVariant)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) DetailAccent else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnBackground
            )
            Text(
                text = price,
                fontSize = 14.sp,
                color = OnBackgroundMuted
            )
        }
        if (savings != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DetailAccent)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = savings,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = DetailAccent,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
