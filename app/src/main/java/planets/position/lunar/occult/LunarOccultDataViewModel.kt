package planets.position.lunar.occult

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewmodel.CreationExtras
import planets.position.PlanetApplication
import planets.position.database.lunar.LunarRepository
import planets.position.database.lunar.Occult

class LunarOccultDataViewModel(
    private val lunarRepository: LunarRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var occult: LiveData<Occult>? = null

    init {
        occult = savedStateHandle.getLiveData("occultID", 0).switchMap { i ->
            lunarRepository.getLunarOccult(i).asLiveData()
        }
    }

    fun saveLunarOccult(id: Int) {
        savedStateHandle["occultID"] = id
    }

    fun getLunarOccult(): LiveData<Occult>? {
        return occult
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

                return LunarOccultDataViewModel(
                    (application as PlanetApplication).lunarRepository,
                    savedStateHandle
                ) as T
            }
        }
    }
}
