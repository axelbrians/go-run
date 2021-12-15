package com.machina.gorun.view.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.machina.gorun.R
import com.machina.gorun.core.MyHelper
import com.machina.gorun.databinding.FragmentOverallBinding
import com.machina.gorun.viewmodels.OverallViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OverallFragment : Fragment() {

    private var _binding: FragmentOverallBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<OverallViewModel>()

    @Inject lateinit var myHelper: MyHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOverallBinding.inflate(inflater, container, false)

        setupView()
        subscribeToObservables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.fragmentOverallWeightLinearLayout.setOnClickListener {

        }

        binding.fragmentOverallHeightLinearLayout.setOnClickListener {

        }
    }

    private fun setupView() {

    }

    private fun subscribeToObservables() {
        viewModel.monthActivities.observe(viewLifecycleOwner) {
            with(binding) {
                fragmentOverallDistance.text = it.distance
                fragmentOverallCalories.text = it.calories
                fragmentOverallTimeElapsed.text = it.timeElapsed
            }
        }

        viewModel.getPastMonthJoggingResult()
    }

}