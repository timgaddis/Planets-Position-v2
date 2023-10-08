package planets.position.lunar.eclipse

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewmodel.CreationExtras
import planets.position.PlanetApplication
import planets.position.database.lunar.Lunar
import planets.position.database.lunar.LunarRepository

class LunarEclipseDataViewModel(
    private val lunarRepository: LunarRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var lunar: LiveData<Lunar>? = null

    init {
        lunar = savedStateHandle.getLiveData("lunarID", 0).switchMap { i ->
            lunarRepository.getLunarEclipse(i).asLiveData()
        }
    }

    fun saveLunarEclipse(id: Int) {
        savedStateHandle["lunarID"] = id
    }

    fun getLunarEclipse(): LiveData<Lunar>? {
        return lunar
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

                return LunarEclipseDataViewModel(
                    (application as PlanetApplication).lunarRepository,
                    savedStateHandle
                ) as T
            }
        }
    }
}
