package planets.position.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
    private var raIndex = MutableLiveData<Int>()
    private var decIndex = MutableLiveData<Int>()
    private var azIndex = MutableLiveData<Int>()
    private var altIndex = MutableLiveData<Int>()

    fun setRAIndex(i: Int) {
        raIndex.value = i
    }

    fun setDecIndex(i: Int) {
        decIndex.value = i
    }

    fun setAzIndex(i: Int) {
        azIndex.value = i
    }

    fun setAltIndex(i: Int) {
        altIndex.value = i
    }

    fun getRAIndex(): LiveData<Int> {
        return raIndex
    }

    fun getDecIndex(): LiveData<Int> {
        return decIndex
    }

    fun getAzIndex(): LiveData<Int> {
        return azIndex
    }

    fun getAltIndex(): LiveData<Int> {
        return altIndex
    }
}
