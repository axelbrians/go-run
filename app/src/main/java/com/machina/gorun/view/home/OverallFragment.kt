package com.machina.gorun.view.home

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.machina.gorun.R
import com.machina.gorun.core.MyHelper
import com.machina.gorun.data.sources.shared_prefs.UserSharedPrefs
import com.machina.gorun.databinding.FragmentOverallBinding
import com.machina.gorun.view.InsertDialog
import com.machina.gorun.viewmodels.OverallViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class OverallFragment : Fragment() {

    private var _binding: FragmentOverallBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<OverallViewModel>()

    @Inject lateinit var myHelper: MyHelper
    @Inject lateinit var prefs: UserSharedPrefs

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
            val dialog = InsertDialog("Enter your weight", "Weight (Kg)",
            object : InsertDialog.InsertDialogInterface {
                override fun onInsert(dialogFragment: DialogFragment, data: String) {
                    val weight = data.toDouble()
                    prefs.setUserWeight(weight)
                    dialogFragment.dismiss()
                    revalidateSetting()
                }

            })

            dialog.show(parentFragmentManager, "dialog")
        }

        binding.fragmentOverallHeightLinearLayout.setOnClickListener {
            val dialog = InsertDialog("Enter your height", "Height (Cm)",
                object : InsertDialog.InsertDialogInterface {
                    override fun onInsert(dialogFragment: DialogFragment, data: String) {
                        val height = data.toDouble()
                        prefs.setUserHeight(height)
                        dialogFragment.dismiss()
                        revalidateSetting()
                    }

                })

            dialog.show(parentFragmentManager, "dialog")
        }
    }

    private fun setupView() {
        revalidateSetting()
    }

    private fun revalidateSetting() {
        val weight = prefs.getUserWeight()
        val height = prefs.getUserHeight()

        binding.fragmentOverallWeight.text = String.format("%.1f Kg", weight)
        binding.fragmentOverallHeight.text = String.format("%.1f Cm", height)
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