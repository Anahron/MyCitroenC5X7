package ru.newlevel.mycitroenc5x7.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.mycitroenc5x7.R
import ru.newlevel.mycitroenc5x7.app.TAG
import ru.newlevel.mycitroenc5x7.databinding.FragmentDashboardBinding
import ru.newlevel.mycitroenc5x7.models.PersonSettingsStatus

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val binding: FragmentDashboardBinding by viewBinding()
    private val dashboardViewModel by viewModel<DashboardViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        collectUiState()
        setupLightingListeners()
        setupComfortListeners()
        setUi(dashboardViewModel.state.value)
    }

    private fun setUi(status: PersonSettingsStatus) {
        binding.ivDayNight.setImageResource(if (status.isDay) R.drawable.vector_sun else R.drawable.vector_moon)
        binding.switchDirHeadlamp.isChecked = status.adaptiveLighting
        if (status.guideMeHome) {
            binding.guideDurationSetup.visibility = View.VISIBLE
            binding.switchGuideToHome.isChecked = true
        } else {
            binding.guideDurationSetup.visibility = View.GONE
            binding.switchGuideToHome.isChecked = false
        }
        binding.tvGuideDuration.text = status.durationGuide.toString()
        binding.switchHandbrake.isChecked = status.automaticHandbrake
        binding.switchParktronic.isChecked = status.parktronics
        binding.switchDriverPosition.isChecked = status.driverWelcome
        binding.seekBar.progress = status.cmbBrightness
    }

    private fun setupComfortListeners() {
        binding.switchHandbrake.setOnClickListener {
            val isCheck = binding.switchHandbrake.isChecked
            sendHandBrake(isCheck)
        }
        binding.switchDriverPosition.setOnClickListener {
            val isCheck = binding.switchDriverPosition.isChecked
            sendDriverPosition(isCheck)
        }
        binding.switchParktronic.setOnClickListener {
            val isCheck = binding.switchParktronic.isChecked
            sendParktronics(isCheck)
        }
    }

    private fun setupLightingListeners() {
        binding.switchDirHeadlamp.setOnClickListener {
            val isCheck = binding.switchDirHeadlamp.isChecked
            sendAdaptiveStatus(isCheck)
        }
        binding.switchGuideToHome.setOnClickListener {
            val isCheck = binding.switchGuideToHome.isChecked
            if (isCheck) binding.guideDurationSetup.visibility = View.VISIBLE
            else binding.guideDurationSetup.visibility = View.GONE
            sendGuideMeToHome(isCheck)
        }

        binding.ibLeft.setOnClickListener {
            var text = 0
            try {
                text = binding.tvGuideDuration.text.toString().toInt()
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
            }
            text = if (text == 0) 45 else (text -15)
            binding.tvGuideDuration.text = text.toString()
            sendGuideMeToHomeDuration(text)
        }

        binding.ibRight.setOnClickListener {
            var text = 0
            try {
                text = binding.tvGuideDuration.text.toString().toInt()
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
            }
            text = if (text == 45) 0 else (text + 15)
            binding.tvGuideDuration.text = text.toString()
            sendGuideMeToHomeDuration(text)
        }
    }

    fun sendBrightness(level: Int, isDay: Boolean) {
        val intent = Intent("ru.newlevel.mycitroenc5x7.service.LOCAL_BROADCAST")
        intent.putExtra("message", "Brightness")
        intent.putExtra("level", level)
        intent.putExtra("isDay", isDay)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    fun sendAdaptiveStatus(isOn: Boolean) {
        val intent = Intent("ru.newlevel.mycitroenc5x7.service.LOCAL_BROADCAST")
        intent.putExtra("message", "Adaptive")
        intent.putExtra("isOn", isOn)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    fun sendGuideMeToHome(isOn: Boolean) {
        val intent = Intent("ru.newlevel.mycitroenc5x7.service.LOCAL_BROADCAST")
        intent.putExtra("message", "GuideMeToHome")
        intent.putExtra("isOn", isOn)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    fun sendGuideMeToHomeDuration(duration: Int) {
        val intent = Intent("ru.newlevel.mycitroenc5x7.service.LOCAL_BROADCAST")
        intent.putExtra("message", "GuideMeToHomeDuration")
        intent.putExtra("Duration", duration)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    fun sendHandBrake(isOn: Boolean) {
        val intent = Intent("ru.newlevel.mycitroenc5x7.service.LOCAL_BROADCAST")
        intent.putExtra("message", "HandBrake")
        intent.putExtra("isOn", isOn)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    fun sendDriverPosition(isOn: Boolean) {
        val intent = Intent("ru.newlevel.mycitroenc5x7.service.LOCAL_BROADCAST")
        intent.putExtra("message", "DriverPosition")
        intent.putExtra("isOn", isOn)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    fun sendParktronics(isOn: Boolean) {
        val intent = Intent("ru.newlevel.mycitroenc5x7.service.LOCAL_BROADCAST")
        intent.putExtra("message", "Parktronics")
        intent.putExtra("isOn", isOn)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    private fun setupListeners() {
        binding.materialButtonToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            when (checkedId) {
                R.id.button_1 -> if (isChecked) {
                    binding.linearLayout2.visibility = View.GONE
                    binding.linearBrighness.visibility = View.GONE
                    binding.linearLayout1.visibility = View.VISIBLE
                    setUi(dashboardViewModel.state.value)
                }

                R.id.button_2 -> if (isChecked) {
                    binding.linearLayout1.visibility = View.GONE
                    binding.linearBrighness.visibility = View.GONE
                    binding.linearLayout2.visibility = View.VISIBLE
                    setUi(dashboardViewModel.state.value)
                }

                R.id.button_3 -> if (isChecked) {
                    binding.linearLayout2.visibility = View.GONE
                    binding.linearLayout1.visibility = View.GONE
                    binding.linearBrighness.visibility = View.VISIBLE
                    setUi(dashboardViewModel.state.value)
                }
            }
        }
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val resultBrightnessIn16 = when (seekBar?.progress) { // 1110
                    0 -> 0x00  // 0000
                    1 -> 0x01  // 0001
                    2 -> 0x02  // 0010
                    3 -> 0x03  // 0011
                    4 -> 0x04  // 0100
                    5 -> 0x05  // 0101
                    6 -> 0x06  // 0110
                    7 -> 0x07  // 0111
                    8 -> 0x08  // 1000
                    9 -> 0x09  // 1001
                    10 -> 0x0A  // 1010
                    11 -> 0x0B  // 1011
                    12 -> 0x0C  // 1100
                    13 -> 0x0D  // 1101
                    14 -> 0x0E  // 1110
                    15 -> 0x0F  // 1111
                    else -> 0x08
                }
                sendBrightness(
                    resultBrightnessIn16, isDay = dashboardViewModel.state.value.isDay
                )
            }
        })
    }

    private fun collectUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            dashboardViewModel.state.collect { it ->
                setUi(it)
            }
        }
    }
}