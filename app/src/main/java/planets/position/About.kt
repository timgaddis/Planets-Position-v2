package planets.position

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import planets.position.databinding.FragmentAboutBinding
import java.util.Locale

class About : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val about1: TextView = binding.textAbout1
        val about2: TextView = binding.textAbout2

        about1.text = String.format(
            Locale.getDefault(),
            "Planet's Position v%s",
            BuildConfig.VERSION_NAME
        )
        about2.setText(R.string.about_main)
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
