package io.designtoswiftui.countdown2binge.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.designtoswiftui.countdown2binge.models.AlertFilter
import io.designtoswiftui.countdown2binge.models.NotificationSettings
import io.designtoswiftui.countdown2binge.models.NotificationStatus
import io.designtoswiftui.countdown2binge.models.NotificationType
import io.designtoswiftui.countdown2binge.models.ScheduledNotification
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.services.notifications.NotificationService
import io.designtoswiftui.countdown2binge.services.notifications.NotificationSettingsRepository
import io.designtoswiftui.countdown2binge.services.premium.PremiumManager
import io.designtoswiftui.countdown2binge.services.repository.ShowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Notifications Hub screen.
 */
data class NotificationsUiState(
    val hasPermission: Boolean = false,
    val isPremium: Boolean = false,
    val pendingCount: Int = 0,
    val typeBreakdown: Map<NotificationType, Int> = emptyMap(),
    val nextScheduled: ScheduledNotification? = null,
    val globalSettings: NotificationSettings = NotificationSettings(),
    val followedShows: List<ShowWithNextNotification> = emptyList(),
    val scheduledAlerts: List<ScheduledNotification> = emptyList(),
    val selectedFilter: AlertFilter = AlertFilter.PENDING
)

/**
 * Show with its next upcoming notification info.
 */
data class ShowWithNextNotification(
    val show: Show,
    val nextNotification: ScheduledNotification?,
    val pendingCount: Int
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationService: NotificationService,
    private val settingsRepository: NotificationSettingsRepository,
    private val showRepository: ShowRepository,
    private val premiumManager: PremiumManager
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(AlertFilter.PENDING)
    val selectedFilter: StateFlow<AlertFilter> = _selectedFilter.asStateFlow()

    val hasPermission: StateFlow<Boolean> = MutableStateFlow(notificationService.hasNotificationPermission())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isPremium: StateFlow<Boolean> = premiumManager.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val pendingCount: StateFlow<Int> = notificationService.getPendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val globalSettings: StateFlow<NotificationSettings> = settingsRepository.globalSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationSettings())

    val nextScheduled: StateFlow<ScheduledNotification?> = notificationService.getNextScheduledNotification()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _typeBreakdown = MutableStateFlow<Map<NotificationType, Int>>(emptyMap())
    val typeBreakdown: StateFlow<Map<NotificationType, Int>> = _typeBreakdown.asStateFlow()

    val followedShows: StateFlow<List<ShowWithNextNotification>> = showRepository.getInProductionShows()
        .map { shows ->
            shows.map { show ->
                ShowWithNextNotification(
                    show = show,
                    nextNotification = null, // Will be populated in loadData
                    pendingCount = 0
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduledAlerts: StateFlow<List<ScheduledNotification>> = combine(
        notificationService.getAllNotifications(),
        _selectedFilter
    ) { notifications, filter ->
        notifications.filter { notification ->
            when (filter) {
                AlertFilter.PENDING -> notification.status in listOf(
                    NotificationStatus.PENDING,
                    NotificationStatus.SCHEDULED,
                    NotificationStatus.QUEUED
                )
                AlertFilter.DELIVERED -> notification.status == NotificationStatus.DELIVERED
                AlertFilter.CANCELLED -> notification.status == NotificationStatus.CANCELLED
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadTypeBreakdown()
    }

    private fun loadTypeBreakdown() {
        viewModelScope.launch {
            val counts = notificationService.getPendingCountByType()
            _typeBreakdown.value = counts.associate { it.type to it.count }
        }
    }

    fun refreshPermissionStatus() {
        viewModelScope.launch {
            (hasPermission as MutableStateFlow).value = notificationService.hasNotificationPermission()
        }
    }

    fun setSelectedFilter(filter: AlertFilter) {
        _selectedFilter.value = filter
    }

    // region Global Settings

    fun setSeasonPremiere(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSeasonPremiere(enabled)
        }
    }

    fun setNewEpisodes(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNewEpisodes(enabled)
        }
    }

    fun setFinaleReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setFinaleReminder(enabled)
        }
    }

    fun setBingeReady(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBingeReady(enabled)
        }
    }

    // endregion

    // region Alert Actions

    fun cancelNotification(notificationId: Long) {
        viewModelScope.launch {
            notificationService.cancelNotification(notificationId)
            loadTypeBreakdown()
        }
    }

    // endregion
}
