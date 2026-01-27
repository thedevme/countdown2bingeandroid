package io.designtoswiftui.countdown2binge.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.designtoswiftui.countdown2binge.services.premium.PremiumManager
import io.designtoswiftui.countdown2binge.services.premium.PremiumOfferings
import io.designtoswiftui.countdown2binge.services.premium.PremiumPackage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Paywall screen.
 */
@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val premiumManager: PremiumManager
) : ViewModel() {

    val isPremium: StateFlow<Boolean> = premiumManager.isPremium

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

    private val _purchaseError = MutableStateFlow<String?>(null)
    val purchaseError: StateFlow<String?> = _purchaseError.asStateFlow()

    private val _offerings = MutableStateFlow<PremiumOfferings?>(null)
    val offerings: StateFlow<PremiumOfferings?> = _offerings.asStateFlow()

    private val _selectedPackage = MutableStateFlow<PremiumPackage?>(null)
    val selectedPackage: StateFlow<PremiumPackage?> = _selectedPackage.asStateFlow()

    init {
        loadOfferings()
    }

    /**
     * Load available offerings from RevenueCat.
     */
    private fun loadOfferings() {
        viewModelScope.launch {
            premiumManager.getOfferings()
                .onSuccess { offerings ->
                    _offerings.value = offerings
                    // Select the first package (usually annual) by default
                    _selectedPackage.value = offerings.packages.firstOrNull()
                }
                .onFailure { error ->
                    _purchaseError.value = error.message ?: "Failed to load offerings"
                }
        }
    }

    /**
     * Select a package for purchase.
     */
    fun selectPackage(pkg: PremiumPackage) {
        _selectedPackage.value = pkg
    }

    /**
     * Purchase the selected package.
     */
    fun purchase(activity: Activity) {
        val pkg = _selectedPackage.value?.rcPackage ?: return

        viewModelScope.launch {
            _isPurchasing.value = true
            _purchaseError.value = null

            premiumManager.purchase(activity, pkg)
                .onSuccess {
                    _isPurchasing.value = false
                    // isPremium will be updated automatically by PremiumManager
                }
                .onFailure { error ->
                    _isPurchasing.value = false
                    _purchaseError.value = error.message ?: "Purchase failed"
                }
        }
    }

    /**
     * Restore previous purchases.
     */
    fun restorePurchases() {
        viewModelScope.launch {
            _isPurchasing.value = true
            _purchaseError.value = null

            premiumManager.restorePurchases()
                .onSuccess {
                    _isPurchasing.value = false
                }
                .onFailure { error ->
                    _isPurchasing.value = false
                    _purchaseError.value = error.message ?: "Restore failed"
                }
        }
    }

    /**
     * Clear any purchase error.
     */
    fun clearError() {
        _purchaseError.value = null
    }
}
