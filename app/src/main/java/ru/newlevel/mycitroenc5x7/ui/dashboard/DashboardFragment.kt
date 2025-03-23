package ru.newlevel.mycitroenc5x7.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.mycitroenc5x7.R
import ru.newlevel.mycitroenc5x7.app.TAG
import ru.newlevel.mycitroenc5x7.databinding.FragmentDashboardBinding
import ru.newlevel.mycitroenc5x7.models.CmbColor
import ru.newlevel.mycitroenc5x7.models.CmbGlobalTheme
import ru.newlevel.mycitroenc5x7.models.PersonSettingsStatus

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val binding: FragmentDashboardBinding by viewBinding()
    private val dashboardViewModel by viewModel<DashboardViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupLightingListeners()
        setupComfortListeners()
        setupWindowListeners()
        setUi(dashboardViewModel.state.value)
        collectUiState()
    }

    private fun setupWindowListeners() {
        binding.tvTahometrLeft.setOnClickListener{
            setLeftWindow(1)
            sendLeftWindow(1)
        }
        binding.tvMusicLeft.setOnClickListener{
            setLeftWindow(2)
            sendLeftWindow(2)
        }
//        binding.tvNaviLeft.setOnClickListener{
//            setLeftWindow(3)
//            sendLeftWindow(3)
//        }
        binding.tvTemperatureLeft.setOnClickListener{
            setLeftWindow(4)
            sendLeftWindow(4)
        }
        binding.tvTripLeft.setOnClickListener{
            setLeftWindow(5)
            sendLeftWindow(5)
        }
        binding.tvNothingLeft.setOnClickListener{
            setLeftWindow(6)
            sendLeftWindow(6)
        }
        binding.tvTahometrRight.setOnClickListener{
            setRightWindow(1)
            sendRightWindow(1)
        }
        binding.tvMusicRight.setOnClickListener{
            setRightWindow(2)
            sendRightWindow(2)
        }
//        binding.tvNaviRight.setOnClickListener{
//            setRightWindow(3)
//            sendRightWindow(3)
//        }
        binding.tvTemperatureRight.setOnClickListener{
            setRightWindow(4)
            sendRightWindow(4)
        }
        binding.tvTripRight.setOnClickListener{
            setRightWindow(5)
            sendRightWindow(5)
        }
        binding.tvNothingRight.setOnClickListener{
            setRightWindow(6)
            sendRightWindow(6)
        }
    }

    private fun setUi(status: PersonSettingsStatus) {
        binding.ivDayNight.setImageResource(if (status.isDay) R.drawable.vector_sun else R.drawable.vector_moon)
        binding.switchDirHeadlamp.isChecked = status.adaptiveLighting
        if (status.guideMeHome) {
            binding.guideDurationSetup.visibility = View.VISIBLE
            //   binding.switchGuideToHome.isChecked = true
            if (!binding.switchGuideToHome.isChecked)
                binding.switchGuideToHome.toggle()
        } else {
            binding.guideDurationSetup.visibility = View.GONE
            if (binding.switchGuideToHome.isChecked)
                binding.switchGuideToHome.toggle()
            //  binding.switchGuideToHome.isChecked = false
        }
        binding.tvGuideDuration.text = status.durationGuide.toString()
        binding.switchHandbrake.isChecked = status.automaticHandbrake
        binding.switchParktronic.isChecked = status.parktronics
        binding.switchDriverPosition.isChecked = status.driverWelcome
        binding.switchEsp.isChecked = status.espStatus
        setColorNormal(status.colorNormal)
        setColorSport(status.colorSport)
        if (status.isDay) {
            binding.seekBar.progress = 15
            binding.seekBar.isActivated = false
            binding.seekBar.isClickable = false
            binding.seekBar.isEnabled = false
        } else {
            binding.seekBar.progress = status.cmbBrightness
            binding.seekBar.isActivated = true
            binding.seekBar.isClickable = true
            binding.seekBar.isEnabled = true
        }
        if (status.cmbGlobalTheme == CmbGlobalTheme.THEME_NORMAL){
            binding.tvThemePerfomance.setTextColor(ContextCompat.getColor(requireContext(), R.color.semi_yellow))
            binding.tvThemePerfomance.alpha = 0.5f
            binding.tvThemeNormal.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_yellow))
            binding.tvThemeNormal.alpha = 1f
        } else {
            binding.tvThemeNormal.setTextColor(ContextCompat.getColor(requireContext(), R.color.semi_yellow))
            binding.tvThemeNormal.alpha = 0.5f
            binding.tvThemePerfomance.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_yellow))
            binding.tvThemePerfomance.alpha = 1f
        }
        setLeftWindow(status.cmbThemeLeft)
        setRightWindow(status.cmbThemeRight)
    }

    private fun setColorSport(color: CmbColor) {
        when(color){
            CmbColor.COLOR_YELLOW -> {
                binding.ibSportThemeYellow.alpha = 1f
                binding.ibSportThemeBlue.alpha = 0.3f
                binding.ibSportThemeRed.alpha = 0.3f
            }
            CmbColor.COLOR_BLUE ->  {
                binding.ibSportThemeYellow.alpha = 0.3f
                binding.ibSportThemeBlue.alpha = 1f
                binding.ibSportThemeRed.alpha = 0.3f
            }
            CmbColor.COLOR_RED ->  {
                binding.ibSportThemeYellow.alpha = 0.3f
                binding.ibSportThemeBlue.alpha = 0.3f
                binding.ibSportThemeRed.alpha = 1f
            }
        }
    }

    private fun setColorNormal(color: CmbColor) {
        when(color){
            CmbColor.COLOR_YELLOW -> {
                binding.ibThemeYellow.alpha = 1f
                binding.ibThemeBlue.alpha = 0.3f
                binding.ibThemeRed.alpha = 0.3f
            }
            CmbColor.COLOR_BLUE ->  {
                binding.ibThemeYellow.alpha = 0.3f
                binding.ibThemeBlue.alpha = 1f
                binding.ibThemeRed.alpha = 0.3f
            }
            CmbColor.COLOR_RED ->  {
                binding.ibThemeYellow.alpha = 0.3f
                binding.ibThemeBlue.alpha = 0.3f
                binding.ibThemeRed.alpha = 1f
            }
        }
    }

    private fun setLeftWindow(id: Int) {
        binding.tvTahometrLeft.setTextColor(ContextCompat.getColor(requireContext(), if (id == 1) R.color.main_yellow else R.color.semi_yellow))
        binding.tvMusicLeft.setTextColor(ContextCompat.getColor(requireContext(), if (id == 2) R.color.main_yellow else R.color.semi_yellow))
    //    binding.tvNaviLeft.setTextColor(ContextCompat.getColor(requireContext(), if (id == 3) R.color.main_yellow else R.color.semi_yellow))
        binding.tvTemperatureLeft.setTextColor(ContextCompat.getColor(requireContext(), if (id == 4) R.color.main_yellow else R.color.semi_yellow))
        binding.tvTripLeft.setTextColor(ContextCompat.getColor(requireContext(), if (id == 5) R.color.main_yellow else R.color.semi_yellow))
        binding.tvNothingLeft.setTextColor(ContextCompat.getColor(requireContext(), if (id == 6) R.color.main_yellow else R.color.semi_yellow))
    }

    private fun setRightWindow(id: Int) {
        binding.tvTahometrRight.setTextColor(ContextCompat.getColor(requireContext(), if (id == 1) R.color.main_yellow else R.color.semi_yellow))
        binding.tvMusicRight.setTextColor(ContextCompat.getColor(requireContext(), if (id == 2) R.color.main_yellow else R.color.semi_yellow))
    //    binding.tvNaviRight.setTextColor(ContextCompat.getColor(requireContext(), if (id == 3) R.color.main_yellow else R.color.semi_yellow))
        binding.tvTemperatureRight.setTextColor(ContextCompat.getColor(requireContext(), if (id == 4) R.color.main_yellow else R.color.semi_yellow))
        binding.tvTripRight.setTextColor(ContextCompat.getColor(requireContext(), if (id == 5) R.color.main_yellow else R.color.semi_yellow))
        binding.tvNothingRight.setTextColor(ContextCompat.getColor(requireContext(), if (id == 6) R.color.main_yellow else R.color.semi_yellow))
    }


    private fun setupComfortListeners() {
        binding.tvThemeNormal.setOnClickListener {
            sendThemePerformanceOn(false)
        }
        binding.tvThemePerfomance.setOnClickListener {
            sendThemePerformanceOn(true)
        }
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
        binding.switchEsp.setOnClickListener {
            val isCheck = binding.switchEsp.isChecked
            sendESP(isCheck)
        }
        binding.ibThemeRed.setOnClickListener {
            sendTheme("red")
        }
        binding.ibThemeBlue.setOnClickListener {
            sendTheme("blue")
        }
        binding.ibThemeYellow.setOnClickListener {
            sendTheme("yellow")
        }
        binding.ibSportThemeRed.setOnClickListener {
            sendSportTheme("red")
        }
        binding.ibSportThemeBlue.setOnClickListener {
            sendSportTheme("blue")
        }
        binding.ibSportThemeYellow.setOnClickListener {
            sendSportTheme("yellow")
        }
    }
    private fun sendLeftWindow(id: Int) {
        val intent = Intent("ru.newlevel.mycitroenc5x7.service.LOCAL_BROADCAST")
        intent.putExtra("message", "LeftWindow")
        intent.putExtra("id", id)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }
    private fun sendRightWindow(id: Int) {
        val intent = Intent("ru.newlevel.mycitroenc5x7.service.LOCAL_BROADCAST")
        intent.putExtra("message", "RightWindow")
        intent.putExtra("id", id)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }
    private fun sendSportTheme(color: String) {
        val intent = Intent("ru.newlevel.mycitroenc5x7.service.LOCAL_BROADCAST")
        intent.putExtra("message", "SportTheme")
        intent.putExtra("theme", color)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    private fun sendTheme(color: String) {
        val intent = Intent("ru.newlevel.mycitroenc5x7.service.LOCAL_BROADCAST")
        intent.putExtra("message", "Theme")
        intent.putExtra("theme", color)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
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
            var text = 15
            try {
                text = binding.tvGuideDuration.text.toString().toInt()
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
            }
            text = if (text == 15) 45 else (text - 15)
            binding.tvGuideDuration.text = text.toString()
            sendGuideMeToHomeDuration(text)
        }

        binding.ibRight.setOnClickListener {
            var text = 15
            try {
                text = binding.tvGuideDuration.text.toString().toInt()
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
            }
            text = if (text == 45) 15 else (text + 15)
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
    fun sendThemePerformanceOn(isOn: Boolean) {
        val intent = Intent("ru.newlevel.mycitroenc5x7.service.LOCAL_BROADCAST")
        intent.putExtra("message", "ThemePerformance")
        intent.putExtra("isOn", isOn)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    fun sendESP(isOn: Boolean) {
        val intent = Intent("ru.newlevel.mycitroenc5x7.service.LOCAL_BROADCAST")
        intent.putExtra("message", "Esp")
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
                    binding.linearWindows.visibility = View.GONE
                    setUi(dashboardViewModel.state.value)
                }

                R.id.button_2 -> if (isChecked) {
                    binding.linearLayout1.visibility = View.GONE
                    binding.linearBrighness.visibility = View.GONE
                    binding.linearLayout2.visibility = View.VISIBLE
                    binding.linearWindows.visibility = View.GONE
                    setUi(dashboardViewModel.state.value)
                }

                R.id.button_3 -> if (isChecked) {
                    binding.linearLayout2.visibility = View.GONE
                    binding.linearLayout1.visibility = View.GONE
                    binding.linearBrighness.visibility = View.VISIBLE
                    binding.linearWindows.visibility = View.GONE
                    setUi(dashboardViewModel.state.value)
                }

                R.id.button_4 -> if (isChecked) {
                    binding.linearLayout2.visibility = View.GONE
                    binding.linearLayout1.visibility = View.GONE
                    binding.linearBrighness.visibility = View.GONE
                    binding.linearWindows.visibility = View.VISIBLE
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



