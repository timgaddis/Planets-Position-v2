package planets.position.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import planets.position.PreferenceViewModel
import planets.position.databinding.FragmentLocationBinding

class LocationFragment : Fragment() {

    private val sharedViewModel: PreferenceViewModel by activityViewModels()
    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: LocationViewModel

    private lateinit var navController: NavController
    private var lastUpdate: Long = 0
    private var latitude = 0.0
    private var longitude: Double = 0.0
    private var altitude: Double = 0.0
    private var offset: Double = 0.0
    private var cityID: Int = -1
    private var edit = false
    private var manualEdit: Boolean = false
    private var zoneName: String = ""
    private lateinit var latitudeText: TextView
    private lateinit var longitudeText: TextView
    private lateinit var elevationText: TextView
    private lateinit var timezoneText: TextView
    private lateinit var gmtOffsetText: TextView
    private lateinit var latitudeEdit: EditText
    private lateinit var longitudeEdit: EditText
    private lateinit var elevationEdit: EditText
    private lateinit var timezoneEdit: EditText
    private lateinit var spinnerLat: Spinner
    private lateinit var spinnerLong: Spinner
    private lateinit var groupText: Group
    private lateinit var groupEdit: Group
    private lateinit var groupButtons: Group

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        latitudeText = binding.newLatText
        longitudeText = binding.newLongText
        elevationText = binding.newElevationText
        gmtOffsetText = binding.newGMTOffsetText
        timezoneText = binding.newTimezoneText
        latitudeEdit = binding.newLatEdit
        longitudeEdit = binding.newLongEdit
        elevationEdit = binding.newElevationEdit
        timezoneEdit = binding.newTimezoneEdit
        spinnerLat = binding.spinnerLat
        spinnerLong = binding.spinnerLong
//        val buttonCity = binding.buttonCity
//        val buttonEdit = binding.buttonEdit
        groupText = binding.manualEditGroup1
        groupEdit = binding.manualEditGroup2
        groupButtons = binding.manualEditGroup3

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
