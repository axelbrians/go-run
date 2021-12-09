package com.machina.gorun.view.home

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

    private fun setupView() {
        pastActivitiesAdapter = PastActivitiesAdapter()

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

        resolveBottomButtonState(locationPrefs.isJogging())
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