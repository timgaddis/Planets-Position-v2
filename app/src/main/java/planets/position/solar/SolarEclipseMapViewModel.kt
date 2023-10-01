package planets.position.solar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SolarEclipseMapViewModel : ViewModel() {

    var path = MutableLiveData<List<LatLng>>()

    companion object {
        // load c library
        init {
            System.loadLibrary("planets_swiss")
        }
    }

    // c function prototypes
    private external fun solarMapPos(d2: Double): DoubleArray?

    fun getSEPath(): MutableLiveData<List<LatLng>> {
        return path
    }

    fun computePath(start: Double, end: Double) {
        var data: DoubleArray
        val num = 80
        val interval = (end - start) / num
        var date = start
        val sePath = mutableListOf<LatLng>()
        viewModelScope.launch(Dispatchers.Default) {
            for (x in 0..num) {
                data = solarMapPos(date)!!
                sePath.add(LatLng(data[1], data[0]))
                date += interval
            }
            withContext(Dispatchers.Main) {
                path.value = sePath
            }
        }
    }
}
