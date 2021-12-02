package com.machina.gorun.view.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.machina.gorun.R
import com.machina.gorun.databinding.FragmentHomeBinding
import com.machina.gorun.view.home.adapter.PastActivitiesAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var pastActivitiesAdapter: PastActivitiesAdapter

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
    }
}