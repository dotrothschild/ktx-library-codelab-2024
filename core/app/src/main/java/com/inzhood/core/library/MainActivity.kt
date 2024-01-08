package com.inzhood.core.library

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.inzhood.core.R
import com.inzhood.core.databinding.ActivityMainBinding
import com.inzhood.core.library.gps.hasPermission
import com.inzhood.core.library.gps.showLocation

class MainActivity : AppCompatActivity() {
    // Using a custom ViewModelFactory:
    private val viewModel: SharedViewModel by viewModels { GpsLocationViewModelFactory(this) }
    /* THIS IS HOW IN A FRAGMENT TO REFERENCE the existing view model with existing data
    private val sharedViewModel: GpsLocationViewModel by activityViewModels {
        GpsLocationViewModelFactory(requireActivity().application)
    }
     */
    private lateinit var _binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding.root)
        //  setContentView(R.layout.activity_main) can delete on compile, using binding instead
        viewModel.location.observe(this) { location ->
            if (location == null) {
                _binding.textViewGps.text = getString(R.string.no_location)
            } else {
                showLocation(_binding.textViewGps.id, location)
            }
        }
        viewModel.lastLocation()
        viewModel.trackLocation()
        val items = listOf("Walking", "Bicycle", "Scooter", "Automobile", "Helicopter")
        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, items)
        _binding.spinnerUpdateSpeed.adapter = adapter // Access spinner using binding
        _binding.spinnerUpdateSpeed.setSelection(0)
        _binding.button .setOnClickListener {
            viewModel.doUpdateRateFrequency( _binding.spinnerUpdateSpeed.selectedItemPosition)
            viewModel.trackLocation()
        }
    }

    override fun onStart() {
        super.onStart()
        if (!hasPermission(ACCESS_FINE_LOCATION)) {
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION), 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            recreate()
        }
    }
}
