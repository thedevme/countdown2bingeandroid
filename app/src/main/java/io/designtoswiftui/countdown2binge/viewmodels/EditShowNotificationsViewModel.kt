package io.designtoswiftui.countdown2binge.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.designtoswiftui.countdown2binge.models.FinaleReminderTiming
import io.designtoswiftui.countdown2binge.models.NotificationSettings
import io.designtoswiftui.countdown2binge.models.NotificationStatus
import io.designtoswiftui.countdown2binge.models.ScheduledNotification
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.models.ShowNotificationSettings
import io.designtoswiftui.countdown2binge.services.notifications.NotificationService
import io.designtoswiftui.countdown2binge.services.notifications.NotificationSettingsRepository
import io.designtoswiftui.countdown2binge.services.repository.ShowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Edit Show Notifications screen.
 */
data class EditShowNotificationsUiState(
    val show: Show? = null,
    val nextNotification: ScheduledNotification? = null,
    val pendingCount: Int = 0,
    val effectiveSettings: NotificationSettings = NotificationSettings(),
    val showSettings: ShowNotificationSettings? = null,
    val globalSettings: NotificationSettings = NotificationSettings(),
    val scheduledAlerts: List<ScheduledNotification> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class EditShowNotificationsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val notificationService: NotificationService,
    private val settingsRepository: NotificationSettingsRepository,
    private val showRepository: ShowRepository
) : ViewModel() {

    private val showId: Long = savedStateHandle.get<Long>("showId") ?: 0L

    private val _uiState = MutableStateFlow(EditShowNotificationsUiState())
    val uiState: StateFlow<EditShowNotificationsUiState> = _uiState.asStateFlow()

    val show: StateFlow<Show?> = showRepository.getShowByIdFlow(showId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val globalSettings: StateFlow<NotificationSettings> = settingsRepository.globalSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationSettings())

    val showSettings: StateFlow<ShowNotificationSettings> = settingsRepository.getShowSettings(showId)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ShowNotificationSettings(showId)
        )

    val effectiveSettings: StateFlow<NotificationSettings> = combine(
        globalSettings,
        showSettings
    ) { global, show ->
        show.resolveWith(global)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationSettings())

    val pendingCount: StateFlow<Int> = notificationService.getPendingCountForShow(showId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val scheduledAlerts: StateFlow<List<ScheduledNotification>> = notificationService.getNotificationsForShow(showId)
        .map { notifications ->
            notifications.filter { it.status in listOf(
                NotificationStatus.PENDING,
                NotificationStatus.SCHEDULED,
                NotificationStatus.QUEUED
            )}
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nextNotification: StateFlow<ScheduledNotification?> = scheduledAlerts
        .map { alerts -> alerts.minByOrNull { it.scheduledDate } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    // region Settings Updates

    fun setSeasonPremiere(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowSeasonPremiere(showId, enabled)
        }
    }

    fun setNewEpisodes(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowNewEpisodes(showId, enabled)
        }
    }

    fun setFinaleReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowFinaleReminder(showId, enabled)
        }
    }

    fun setFinaleReminderTiming(timing: FinaleReminderTiming) {
        viewModelScope.launch {
            settingsRepository.setShowFinaleReminderTiming(showId, timing)
        }
    }

    fun setBingeReady(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowBingeReady(showId, enabled)
        }
    }

    fun setQuietHoursEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowQuietHoursEnabled(showId, enabled)
        }
    }

    fun setQuietHoursStart(minutesFromMidnight: Int) {
        viewModelScope.launch {
            settingsRepository.setShowQuietHoursStart(showId, minutesFromMidnight)
        }
    }

    fun setQuietHoursEnd(minutesFromMidnight: Int) {
        viewModelScope.launch {
            settingsRepository.setShowQuietHoursEnd(showId, minutesFromMidnight)
        }
    }

    // endregion

    // region Actions

    fun resetToGlobalDefaults() {
        viewModelScope.launch {
            settingsRepository.resetShowToGlobalDefaults(showId)
        }
    }

    fun cancelAllNotifications() {
        viewModelScope.launch {
            notificationService.cancelNotificationsForShow(showId)
        }
    }

    fun cancelNotification(notificationId: Long) {
        viewModelScope.launch {
            notificationService.cancelNotification(notificationId)
        }
    }

    // endregion
}
