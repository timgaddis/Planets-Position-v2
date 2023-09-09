package planets.position.location

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import planets.position.PlanetApplication
import planets.position.database.timezone.TimeZone
import planets.position.database.timezone.TimeZoneRepository
import planets.position.database.timezone.WorldCity

class LocationViewModel(
    private val timeZoneRepository: TimeZoneRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var city: LiveData<WorldCity>? = null
    private var zone: LiveData<TimeZone>? = null
    private var zoneName: String = ""

    init {
        city = savedStateHandle.getLiveData("cityID", 0).switchMap { x ->
            timeZoneRepository.getCityData(x)
        }
        zone = savedStateHandle.getLiveData("locTime", 0L).switchMap { x ->
            timeZoneRepository.getZoneOffset(zoneName, x)
        }
    }

    fun saveCityID(id: Int) {
        savedStateHandle["cityID"] = id
    }

    fun getCity(): LiveData<WorldCity>? {
        return city
    }

    fun saveZone(name: String, time: Long) {
        zoneName = name
        savedStateHandle["locTime"] = time
    }

    fun getZone(): LiveData<TimeZone>? {
        return zone
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val savedStateHandle = extras.createSavedStateHandle()

                return LocationViewModel(
                    (application as PlanetApplication).timeZoneRepository,
                    savedStateHandle
                ) as T
            }
        }
    }
}
