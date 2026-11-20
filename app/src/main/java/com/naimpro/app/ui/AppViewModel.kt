package com.naimpro.app.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.naimpro.app.data.di.AppContainer
import com.naimpro.app.data.prefs.AppSettings
import com.naimpro.app.data.repo.SalahStatus
import com.naimpro.app.domain.hijri.HijriCalendar
import com.naimpro.app.domain.hijri.IslamicEvent
import com.naimpro.app.domain.hijri.IslamicEvents
import com.naimpro.app.domain.model.Location
import com.naimpro.app.domain.model.Prayer
import com.naimpro.app.domain.model.PrayerTimes
import com.naimpro.app.domain.prayer.CalculationMethod
import com.naimpro.app.ui.util.LocationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Everything the home surface needs, resolved once per settings/location change. */
data class HomeUiState(
    val settings: AppSettings = AppSettings(),
    val location: Location? = null,
    val times: PrayerTimes? = null,
    val tomorrow: PrayerTimes? = null,
    val hijri: HijriCalendar.HijriDate? = null,
    /** Civil day currently shown; null while loading. */
    val date: LocalDate? = null,
    /** Offset from today, in days (`0` is today). */
    val dayOffset: Int = 0,
    val loading: Boolean = true,
) {
    val isToday: Boolean get() = dayOffset == 0
}

class AppViewModel(private val container: AppContainer) : ViewModel() {
    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    private val dayOffsetState = MutableStateFlow(0)

    val settings: StateFlow<AppSettings> =
        container.settings.settings
            .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    /** Settings as loaded from disk; `null` until the first emission. */
    val loadedSettings: StateFlow<AppSettings?> =
        container.settings.settings
            .map { it as AppSettings? }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val home: StateFlow<HomeUiState> =
        combine(
            container.settings.settings,
            _location,
            dayOffsetState,
        ) { settings, location, offset -> Triple(settings, location, offset) }
            .mapLatest { (settings, location, offset) ->
                if (location == null) {
                    HomeUiState(settings = settings)
                } else {
                    runCatching {
                        val today = LocalDate.now(location.zoneId)
                        val day = today.plusDays(offset.toLong())
                        val times = container.prayers.timesForCached(day, location, settings)
                        val tomorrow = container.prayers.timesForCached(day.plusDays(1), location, settings)
                        HomeUiState(
                            settings = settings,
                            location = location,
                            times = times,
                            tomorrow = tomorrow,
                            hijri = HijriCalendar.fromGregorian(day, settings.hicriOffsetDays),
                            date = day,
                            dayOffset = offset,
                            loading = false,
                        )
                    }.getOrElse { HomeUiState(settings = settings, location = location, dayOffset = offset) }
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    /** Moves the home screen one day back or forward; bounded to Â±30 days. */
    fun shiftDay(days: Int) {
        dayOffsetState.value = (dayOffsetState.value + days).coerceIn(-30, 30)
    }

    fun showToday() {
        dayOffsetState.value = 0
    }

    val zikirTotal: StateFlow<Int> =
        container.zikir.observeTotal()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val salahStatuses: StateFlow<Map<String, SalahStatus>> =
        container.salah.observeRange(LocalDate.now().minusDays(60), LocalDate.now().plusDays(1))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    init {
        // Startup must survive a broken settings file or an unreadable asset, and
        // parsing the bundled catalogue must not run on the main thread.
        viewModelScope.launch(Dispatchers.IO) {
            val resolved = runCatching { container.locations.current() }.getOrNull()
            if (resolved != null) _location.value = resolved
        }
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            container.settings.update(transform)
            container.alarms.rescheduleAsync()
        }
    }

    fun selectLocation(location: Location) {
        viewModelScope.launch {
            container.locations.save(location)
            _location.value = location
            container.alarms.rescheduleAsync()
            container.widgets.refreshAsync()
        }
    }

    companion object {
        private val popularCityNames = listOf("İstanbul", "Ankara", "İzmir", "Bursa", "Konya", "Antalya")
    }

    fun useCurrentLocation(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val resolved = resolveCoarseLocation()
            if (resolved != null) {
                selectLocation(resolved)
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    /** One-shot coarse fix, rounded to about a kilometre and labelled with the nearest city. */
    suspend fun resolveCoarseLocation(): Location? =
        runCatching { LocationProvider.current(container.context, container.cities) }.getOrNull()

    fun searchCities(query: String): List<Location> = container.cities.search(query)

    fun citySuggestions(): List<Location> = container.cities.suggestions()

    /** Six well known cities shown before the user types anything. */
    fun popularCities(): List<Location> = popularCityNames.mapNotNull { name -> container.cities.search(name).firstOrNull() }

    /**
     * Applies the onboarding choices.
     *
     * [location] may be null when the user skipped the step; in that case the
     * bundled default is stored so the alarm chain has a zone, but it is chosen
     * by the user's skip rather than silently.
     */
    fun completeOnboarding(
        location: Location?,
        method: CalculationMethod,
    ) {
        viewModelScope.launch {
            container.settings.update { it.copy(onboardingCompleted = true, method = method) }
            val target = location ?: _location.value ?: container.cities.defaultLocation()
            container.locations.save(target)
            _location.value = target
            container.alarms.rescheduleAsync()
            container.widgets.refreshAsync()
        }
    }

    suspend fun monthTimes(
        year: Int,
        month: Int,
    ): List<PrayerTimes> {
        val settings = container.settings.current()
        val location = _location.value ?: container.cities.defaultLocation()
        return container.prayers.monthTimes(year, month, location, settings)
    }

    suspend fun upcomingEvents(count: Int = 8): List<IslamicEvent> {
        val settings = container.settings.current()
        return IslamicEvents.upcoming(LocalDate.now(), count, settings.hicriOffsetDays)
    }

    suspend fun currentLocationOrDefault(): Location = _location.value ?: container.cities.defaultLocation()

    fun setSalahStatus(
        date: LocalDate,
        prayer: Prayer,
        status: SalahStatus,
    ) {
        viewModelScope.launch { container.salah.setStatus(date, prayer, status) }
    }

    fun recordZikir(
        phrase: String,
        count: Int,
        target: Int,
        completed: Boolean,
    ) {
        viewModelScope.launch { container.zikir.record(phrase, count, target, completed) }
    }

    suspend fun exportBackup(uri: Uri): Result<Int> = container.backup.export(uri)

    suspend fun importBackup(uri: Uri): Result<Int> = container.backup.import(uri)

    fun rescheduleAlarms() = container.alarms.rescheduleAsync()

    fun refreshWidgets() = container.widgets.refreshAsync()

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(container) as T
    }
}
