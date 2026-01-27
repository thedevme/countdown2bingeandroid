package io.designtoswiftui.countdown2binge.services.premium

import android.app.Activity
import android.content.Context
import android.util.Log
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.awaitRestore
import com.revenuecat.purchases.interfaces.LogInCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages premium subscription status using RevenueCat.
 */
@Singleton
class PremiumManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "PremiumManager"
        const val API_KEY = "goog_zCvQihaKcKgJlcpORhdFdXUfObh"
        const val ENTITLEMENT_ID = "premium"

        // Free tier limits
        const val FREE_SHOW_LIMIT = 3
    }

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _customerInfo = MutableStateFlow<CustomerInfo?>(null)
    val customerInfo: StateFlow<CustomerInfo?> = _customerInfo.asStateFlow()

    /**
     * The show limit based on premium status.
     */
    val showLimit: Int
        get() = if (_isPremium.value) Int.MAX_VALUE else FREE_SHOW_LIMIT

    /**
     * Whether the user can use cloud sync (premium feature).
     */
    val canUseCloudSync: Boolean
        get() = _isPremium.value

    /**
     * Initialize RevenueCat SDK. Call this in Application.onCreate().
     */
    fun configure() {
        try {
            Purchases.logLevel = LogLevel.DEBUG
            Purchases.configure(
                PurchasesConfiguration.Builder(context, API_KEY)
                    .build()
            )
            Log.d(TAG, "RevenueCat configured successfully")

            // Fetch initial customer info
            refreshPremiumStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to configure RevenueCat", e)
        }
    }

    /**
     * Refresh premium status from RevenueCat.
     */
    fun refreshPremiumStatus() {
        _isLoading.value = true

        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                updatePremiumStatus(customerInfo)
                _isLoading.value = false
            }

            override fun onError(error: PurchasesError) {
                Log.e(TAG, "Error fetching customer info: ${error.message}")
                _isLoading.value = false
            }
        })
    }

    /**
     * Update premium status based on customer info.
     */
    private fun updatePremiumStatus(customerInfo: CustomerInfo) {
        _customerInfo.value = customerInfo
        val hasPremium = customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
        _isPremium.value = hasPremium
        Log.d(TAG, "Premium status updated: $hasPremium")
    }

    /**
     * Identify user for RevenueCat (call after sign-in).
     */
    fun identifyUser(userId: String) {
        Purchases.sharedInstance.logIn(userId, object : LogInCallback {
            override fun onReceived(customerInfo: CustomerInfo, created: Boolean) {
                updatePremiumStatus(customerInfo)
                Log.d(TAG, "User identified: $userId, created=$created")
            }

            override fun onError(error: PurchasesError) {
                Log.e(TAG, "Error identifying user: ${error.message}")
            }
        })
    }

    /**
     * Log out user from RevenueCat (call after sign-out).
     */
    fun logOutUser() {
        Purchases.sharedInstance.logOut(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                updatePremiumStatus(customerInfo)
                Log.d(TAG, "User logged out from RevenueCat")
            }

            override fun onError(error: PurchasesError) {
                Log.e(TAG, "Error logging out: ${error.message}")
            }
        })
    }

    /**
     * Get available offerings (products) using suspend function.
     */
    suspend fun getOfferings(): Result<PremiumOfferings> {
        return try {
            val offerings = Purchases.sharedInstance.awaitOfferings()
            val currentOffering = offerings.current
            val packages = currentOffering?.availablePackages ?: emptyList()

            val premiumPackages = packages.map { pkg ->
                PremiumPackage(
                    identifier = pkg.identifier,
                    rcPackage = pkg,
                    localizedPrice = pkg.product.price.formatted,
                    packageType = pkg.packageType.name
                )
            }

            Result.success(PremiumOfferings(premiumPackages, null))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching offerings: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Purchase a package using suspend function.
     */
    suspend fun purchase(activity: Activity, pkg: Package): Result<CustomerInfo> {
        _isLoading.value = true

        return try {
            val purchaseParams = PurchaseParams.Builder(activity, pkg).build()
            val result = Purchases.sharedInstance.awaitPurchase(purchaseParams)
            updatePremiumStatus(result.customerInfo)
            _isLoading.value = false
            Result.success(result.customerInfo)
        } catch (e: Exception) {
            _isLoading.value = false
            Log.e(TAG, "Purchase error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Restore purchases using suspend function.
     */
    suspend fun restorePurchases(): Result<CustomerInfo> {
        _isLoading.value = true

        return try {
            val customerInfo = Purchases.sharedInstance.awaitRestore()
            updatePremiumStatus(customerInfo)
            _isLoading.value = false
            Result.success(customerInfo)
        } catch (e: Exception) {
            _isLoading.value = false
            Log.e(TAG, "Restore error: ${e.message}")
            Result.failure(e)
        }
    }
}

/**
 * Available premium packages.
 */
data class PremiumOfferings(
    val packages: List<PremiumPackage>,
    val error: String?
)

/**
 * A purchasable premium package.
 */
data class PremiumPackage(
    val identifier: String,
    val rcPackage: Package,
    val localizedPrice: String,
    val packageType: String
)
