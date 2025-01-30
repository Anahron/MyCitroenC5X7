package ru.newlevel.mycitroenc5x7.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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

        binding.buttonReset.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.custom_dialog, null)
            val dialogBuilder = AlertDialog.Builder(requireContext())
                .setView(dialogView)


            val buttonYes = dialogView.findViewById<Button>(R.id.buttonYes)
            val buttonNo = dialogView.findViewById<Button>(R.id.buttonNo)

            val dialog = dialogBuilder.create()
            dialog.window?.setBackgroundDrawableResource(R.color.transparent)

            buttonYes.setOnClickListener {
                sendResetTrip(homeViewModel.uiState.value)
                dialog.dismiss()
            }

            buttonNo.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }
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


    fun sendResetTrip(position: Int) {
        val intent = Intent("ru.newlevel.mycitroenc5x7.service.LOCAL_BROADCAST")
        intent.putExtra("message", if (position == 2) "ResetTrip1" else if (position == 3) "ResetTrip2" else "ResetTripMoment")
        intent.putExtra("reset", true)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    private fun collectTripState() {
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.state.collect {
                binding.textDistanceTraveledMoment.text = it.totalDistance
                binding.textL100Moment.text = it.litersPer100km
                if (homeViewModel.uiState.value == 2) {
                    binding.textL100.text = it.litersPer100kmTrip1
                    binding.textSpeed.text = it.avgSpeedTrip1
                    binding.textDistance.text = it.totalDistanceTrip1
                    binding.textTime1.text = it.engineTime1
                } else {
                    binding.textL100.text = it.litersPer100kmTrip2
                    binding.textSpeed.text = it.avgSpeedTrip2
                    binding.textDistance.text = it.totalDistanceTrip2
                    binding.textTime1.text = it.engineTime2
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
                        binding.textDistanceTraveledMoment.text = homeViewModel.state.value.totalDistance
                        binding.textL100Moment.text = homeViewModel.state.value.litersPer100km
                        binding.buttonReset.visibility = View.GONE
                    }

                    2 -> {
                        binding.linearMomentTrip.visibility = View.GONE
                        binding.linearTrip.visibility = View.VISIBLE
                        binding.textTime1.text = homeViewModel.state.value.engineTime1
                        binding.textL100.text =  homeViewModel.state.value.litersPer100kmTrip1
                        binding.textSpeed.text = homeViewModel.state.value.avgSpeedTrip1
                        binding.textDistance.text = homeViewModel.state.value.totalDistanceTrip1
                        binding.buttonReset.visibility = View.VISIBLE
                    }

                    3 -> {
                        binding.linearMomentTrip.visibility = View.GONE
                        binding.linearTrip.visibility = View.VISIBLE
                        binding.textL100.text =  homeViewModel.state.value.litersPer100kmTrip2
                        binding.textSpeed.text = homeViewModel.state.value.avgSpeedTrip2
                        binding.textTime1.text = homeViewModel.state.value.engineTime2
                        binding.textDistance.text = homeViewModel.state.value.totalDistanceTrip2
                        binding.buttonReset.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

}