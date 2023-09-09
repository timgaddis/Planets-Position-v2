package planets.position.location

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import planets.position.PlanetApplication
import planets.position.database.timezone.Country
import planets.position.database.timezone.State
import planets.position.database.timezone.TimeZoneRepository
import planets.position.database.timezone.WorldCity

class ManualLocationDialogViewModel(
    private val timeZoneRepository: TimeZoneRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var country: LiveData<Country>? = null
    private var stateList: LiveData<List<State>>? = null
    private var cityList: LiveData<List<WorldCity>>? = null
    private var cityID: Int = 0

    init {
        country = savedStateHandle.getLiveData("country", "").switchMap { i ->
            timeZoneRepository.getCounrty(i)
        }
        stateList = savedStateHandle.getLiveData("cc", "").switchMap { i ->
            timeZoneRepository.getStateList(i)
        }
        cityList = savedStateHandle.getLiveData("state", "").switchMap { i ->
            timeZoneRepository.getCityList(i)
        }
    }

    fun saveState(state: String) {
        savedStateHandle["state"] = state
    }

    fun getCityList(): LiveData<List<WorldCity>>? {
        return cityList
    }

    fun saveCountryCode(cc: String) {
        savedStateHandle["cc"] = cc
    }

    fun getStateList(): LiveData<List<State>>? {
        return stateList
    }

    fun saveCountry(country: String) {
        savedStateHandle["country"] = country
    }

    fun getCountry(): LiveData<Country>? {
        return country
    }

    fun saveCityID(c: Int) {
        cityID = c
    }

    fun getCityID(): Int {
        return cityID
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

                return ManualLocationDialogViewModel(
                    (application as PlanetApplication).timeZoneRepository,
                    savedStateHandle
                ) as T
            }
        }
    }
}
