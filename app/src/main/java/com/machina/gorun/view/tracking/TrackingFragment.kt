package com.machina.gorun.view.tracking

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.machina.gorun.R
import com.machina.gorun.core.MyHelper
import com.machina.gorun.data.models.toFourDecimal
import com.machina.gorun.data.sources.shared_prefs.LocationSharedPrefs
import com.machina.gorun.databinding.FragmentTrackingBinding
import com.machina.gorun.viewmodels.TrackingViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<TrackingViewModel>()

    @Inject lateinit var myHelper: MyHelper

    @Inject lateinit var locationPrefs: LocationSharedPrefs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)


        setupView()
        subscribeToObservables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.fragmentTrackFinishButton.setOnClickListener {
            locationPrefs.setIsJogging(false)
            findNavController().navigateUp()
        }
    }

    private fun setupView() {

        viewModel.getLivePoint()
    }

    private fun subscribeToObservables() {
        viewModel.currentPoint.observe(viewLifecycleOwner) { point ->
            Timber.d("$point")

            if (point.distanceInMeter > 1000) {
                binding.fragmentTrackStepMeasure.text = "Km"
                binding.fragmentTrackDistance.text = String.format("%.1f", point.distanceInMeter / 1000)
            } else {
                binding.fragmentTrackStepMeasure.text = "Meters"
                binding.fragmentTrackDistance.text = String.format("%.1f", point.distanceInMeter)
            }
        }
    }

}