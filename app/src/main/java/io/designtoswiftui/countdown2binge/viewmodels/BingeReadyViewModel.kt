package io.designtoswiftui.countdown2binge.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.designtoswiftui.countdown2binge.models.Season
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.services.repository.ShowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data class representing a binge-ready season with its associated show.
 */
data class BingeReadySeason(
    val show: Show,
    val season: Season
)

/**
 * ViewModel for the Binge Ready screen.
 * Displays seasons that are complete and ready to binge.
 */
@HiltViewModel
class BingeReadyViewModel @Inject constructor(
    private val repository: ShowRepository
) : ViewModel() {

    // Binge-ready seasons with their shows
    private val _bingeReadySeasons = MutableStateFlow<List<BingeReadySeason>>(emptyList())
    val bingeReadySeasons: StateFlow<List<BingeReadySeason>> = _bingeReadySeasons.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Whether the list is empty
    private val _isEmpty = MutableStateFlow(false)
    val isEmpty: StateFlow<Boolean> = _isEmpty.asStateFlow()

    init {
        loadBingeReadySeasons()
    }

    /**
     * Load binge-ready seasons from the repository.
     */
    private fun loadBingeReadySeasons() {
        viewModelScope.launch {
            _isLoading.value = true

            repository.getBingeReadySeasons().collect { seasons ->
                if (seasons.isEmpty()) {
                    _isEmpty.value = true
                    _bingeReadySeasons.value = emptyList()
                    _isLoading.value = false
                    return@collect
                }

                _isEmpty.value = false

                // Fetch show data for each season
                val bingeReadyList = mutableListOf<BingeReadySeason>()
                for (season in seasons) {
                    val show = repository.getShowById(season.showId)
                    if (show != null) {
                        bingeReadyList.add(BingeReadySeason(show = show, season = season))
                    }
                }

                // Sort by show title
                _bingeReadySeasons.value = bingeReadyList.sortedBy { it.show.title }
                _isLoading.value = false
            }
        }
    }

    /**
     * Refresh the binge-ready list.
     */
    fun refresh() {
        loadBingeReadySeasons()
    }
}
