package planets.position

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.preference.PreferenceManager
import planets.position.database.timezone.TimeZone
import planets.position.database.timezone.TimeZoneRepository

class PreferenceViewModel(
    application: Application,
    private val timeZoneRepository: TimeZoneRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var zone: LiveData<TimeZone>? = null
    private var zoneName: String = ""
    private var settings: SharedPreferences

    init {
        settings = PreferenceManager.getDefaultSharedPreferences(application)
        zone = savedStateHandle.getLiveData("prefTime", 0L).switchMap { x ->
            timeZoneRepository.getZoneOffset(zoneName, x)
        }
    }

    fun saveTime(time: Long, zone: String) {
        savedStateHandle["prefTime"] = time
        zoneName = zone
    }

    fun getZone(): LiveData<TimeZone>? {
        return zone
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>, extras: CreationExtras
            ): T {
                val application =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val savedStateHandle = extras.createSavedStateHandle()

                return PreferenceViewModel(
                    application,
                    (application as PlanetApplication).timeZoneRepository,
                    savedStateHandle
                ) as T
            }
        }
    }
}
