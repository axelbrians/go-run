package com.machina.gorun.view.home

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.machina.gorun.R
import com.machina.gorun.core.MyHelper
import com.machina.gorun.data.sources.shared_prefs.LocationSharedPrefs
import com.machina.gorun.databinding.FragmentHomeBinding
import com.machina.gorun.view.MainActivity
import com.machina.gorun.view.home.adapter.PastActivitiesAdapter
import com.machina.gorun.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<HomeViewModel>()


    private lateinit var pastActivitiesAdapter: PastActivitiesAdapter
    private lateinit var prefsCallback: SharedPreferences.OnSharedPreferenceChangeListener

    @Inject lateinit var myHelper: MyHelper
    
    @Inject lateinit var locationPrefs: LocationSharedPrefs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupView()
        subscribeToObservables()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.fragmentHomeSearch.doAfterTextChanged {
            val query = (it ?: "").toString().lowercase()

            viewModel.searchJoggingResults(query)

            binding.fragmentHomeSearchIc.apply {
                if (query.isNotEmpty()) {
                    setImageResource(R.drawable.ic_baseline_clear_24)
                    setOnClickListener {
                        binding.fragmentHomeSearch.setText("")
                    }
                } else {
                    setImageResource(R.drawable.ic_baseline_search_24)
                    setOnClickListener {  }
                }
            }

        }

        binding.fragmentHomeTodayActivities.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToOverallFragment()
            findNavController().navigate(action)
        }

        viewModel.getTodayJoggingResult()
    }

    private fun setupView() {
        pastActivitiesAdapter = PastActivitiesAdapter()
        pastActivitiesAdapter.setOnItemClick { id ->
            val action = HomeFragmentDirections.actionHomeFragmentToJoggingPathFragment(id)
            findNavController().navigate(action)
        }

        binding.fragmentHomeRecycler.apply {
            adapter = pastActivitiesAdapter
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
        }

        prefsCallback =
            SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                if (key == LocationSharedPrefs.IS_JOGGING) {
                    val isJogging = sharedPreferences.getBoolean(key, false)
                    resolveBottomButtonState(isJogging)
                }
            }

        viewModel.getJoggingResults()
        resolveBottomButtonState(locationPrefs.isJogging())
    }

    private fun subscribeToObservables() {
        viewModel.joggingResults.observe(viewLifecycleOwner) {
            pastActivitiesAdapter.dataSet = it
            viewModel.getTodayJoggingResult()
        }

        viewModel.todayJoggingResult.observe(viewLifecycleOwner) {
            with(binding) {
                distance.text = it.distance
                burnedCaloriesVal.text = it.calories
                timeVal.text = it.timeElapsed
            }
        }
    }

    private fun resolveBottomButtonState(isJogging: Boolean) {
        if (isJogging) {
            binding.fragmentHomeStartButton.apply {
                setBackgroundResource(R.drawable.ripple_12_pink_pastel)
                setOnClickListener {
                    val action = HomeFragmentDirections.actionHomeFragmentToTrackingFragment()
                    findNavController().navigate(action)
                }
            }

            binding.fragmentHomeStartButtonText.text = "Resume"
        } else {
            binding.fragmentHomeStartButton.apply {
                setBackgroundResource(R.drawable.ripple_12_orange)
                setOnClickListener {
                    with(requireActivity() as MainActivity) {
                        if (isForegroundPermissionApproved()) {
                            startTracking()
                            locationPrefs.setIsJogging(true)

                            val action = HomeFragmentDirections.actionHomeFragmentToTrackingFragment()
                            findNavController().navigate(action)
                        } else {
                            requestForegroundPermissions()
                        }
                    }


                }
            }

            binding.fragmentHomeStartButtonText.text = "Start"
        }
    }

    override fun onStart() {
        super.onStart()
        locationPrefs.prefs.registerOnSharedPreferenceChangeListener(prefsCallback)
    }

    override fun onStop() {
        super.onStop()
        locationPrefs.prefs.unregisterOnSharedPreferenceChangeListener(prefsCallback)
    }
}