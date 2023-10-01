package planets.position.solar

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewmodel.CreationExtras
import planets.position.PlanetApplication
import planets.position.database.solar.Solar
import planets.position.database.solar.SolarRepository

class SolarEclipseDataViewModel(
    private val solarRepository: SolarRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var solar: LiveData<Solar>? = null

    init {
        solar = savedStateHandle.getLiveData("solarID", 0).switchMap { i ->
            solarRepository.getSolarEclipseLive(i).asLiveData()
        }
    }

    fun saveSolarEclipse(solar: Int) {
        savedStateHandle["solarID"] = solar
    }

    fun getSolarEclipse(): LiveData<Solar>? {
        return solar
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

                return SolarEclipseDataViewModel(
                    (application as PlanetApplication).solarRepository,
                    savedStateHandle
                ) as T
            }
        }
    }
}
