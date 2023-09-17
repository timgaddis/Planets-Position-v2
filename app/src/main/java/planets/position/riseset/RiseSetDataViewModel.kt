package planets.position.riseset

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewmodel.CreationExtras
import planets.position.PlanetApplication
import planets.position.database.planet.Planet
import planets.position.database.planet.PlanetRepository

class RiseSetDataViewModel(
    private val planetRepository: PlanetRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var planet: LiveData<Planet>? = null

    init {
        planet = savedStateHandle.getLiveData("planetID", 0).switchMap { x ->
            planetRepository.getPlanet(x).asLiveData()
        }
    }

    fun savePlanetID(id: Int) {
        savedStateHandle["planetID"] = id
    }

    fun getPlanet(): LiveData<Planet>? {
        return planet
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

                return RiseSetDataViewModel(
                    (application as PlanetApplication).planetRepository,
                    savedStateHandle
                ) as T
            }
        }
    }
}
