package ru.newlevel.mycitroenc5x7.ui.home

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.mycitroenc5x7.R
import ru.newlevel.mycitroenc5x7.app.TAG
import ru.newlevel.mycitroenc5x7.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {


    private val binding: FragmentHomeBinding by viewBinding()
    private val homeViewModel by viewModel<HomeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.tripGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            when (checkedId) {
                R.id.button_1 -> if (isChecked) {
                    homeViewModel.setUi(1)
                }

                R.id.button_2 -> if (isChecked) {
                    homeViewModel.setUi(2)
                }

                R.id.button_3 -> if (isChecked) {
                    homeViewModel.setUi(3)
                }
            }
        }
        collectUiState()
        collectTripState()
    }

    private fun collectTripState() {
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.state.collect {
                binding.textDistanceLeftMoment.text = it.totalDistanceFinish
                binding.textDistanceTraveledMoment.text = it.totalDistance
                binding.textL100Moment.text = it.litersPer100km
                if (homeViewModel.uiState.value == 2) {
                    binding.textL100.text = it.litersPer100kmTrip1
                    binding.textSpeed.text = it.avgSpeedTrip1
                    binding.textDistance.text = it.totalDistanceTrip1
                } else if (homeViewModel.uiState.value == 3){
                    binding.textL100.text = it.litersPer100kmTrip2
                    binding.textSpeed.text = it.avgSpeedTrip2
                    binding.textDistance.text = it.totalDistanceTrip2
                }
            }
        }
    }

    private fun collectUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.uiState.collect {
                Log.e(TAG, " homeViewModel.uiState.collect ")
                when (it) {
                    1 -> {
                        binding.linearTrip.visibility = View.GONE
                        binding.linearMomentTrip.visibility = View.VISIBLE
                    }

                    2 -> {
                        binding.linearMomentTrip.visibility = View.GONE
                        binding.linearTrip.visibility = View.VISIBLE
                    }

                    3 -> {
                        binding.linearMomentTrip.visibility = View.GONE
                        binding.linearTrip.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

}