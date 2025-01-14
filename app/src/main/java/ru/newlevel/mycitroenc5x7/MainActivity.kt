package ru.newlevel.mycitroenc5x7

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import ru.newlevel.mycitroenc5x7.app.TAG
import ru.newlevel.mycitroenc5x7.databinding.ActivityMainBinding
import ru.newlevel.mycitroenc5x7.models.Mode
import ru.newlevel.mycitroenc5x7.service.UsbService
import ru.newlevel.mycitroenc5x7.ui.MainViewModel

class MainActivity : AppCompatActivity(), KoinComponent {

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel by viewModel<MainViewModel>()

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)
        checkOverlayPermission()
    }

    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            overlayPermissionLauncher.launch(intent)
        } else {
            val intent = Intent(this, UsbService::class.java)
            startForegroundService(intent)
            //     collectLog()
            //     collectData()
            collectSuspensionState()
        }
    }

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(this)) {
            val intent = Intent(this, UsbService::class.java)
            startForegroundService(intent)
            collectSuspensionState()
            //  collectLog()
            //  collectData()
        } else {
            Toast.makeText(this, "Разрешение не предоставлено", Toast.LENGTH_SHORT).show()
        }
    }

//    @OptIn(ExperimentalUnsignedTypes::class)
//    private fun collectData() {
//        lifecycleScope.launch {
//            canRepo.canDataFlow.collect {
//                Log.e(TAG, "dataFlowCollect")
//
//                binding.text.append(
//                    "\nID: 0x${it.canId.toString(16).uppercase()}, DLC: ${it.dlc} Data: ${
//                        it.data.joinToString(", ") {
//                            "0x${it.toString(16).uppercase()}"
//                        }
//                    }")
//                //  binding.text.append("\nid: ${it.canId} dlc: ${it.dlc} data: ${it.data}")
//            }
//        }
//    }
//
//    private fun collectLog() {
//        lifecycleScope.launch {
//            canRepo.logger.collect {
//                Log.e(TAG, "dataFlowCollect")
//                binding.text.append("\n${it}")
//            }
//        }
//    }

    private fun collectSuspensionState() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.suspensionState.collect {
                    Log.e(TAG, " homeViewModel.uiState.collect ")
                    when (it.suspensionState.mode) {
                        Mode.NOT_GRANTED -> {
                            Toast.makeText(this@MainActivity, "Изменение высоты запрещено", Toast.LENGTH_LONG).show()
                        }

                        Mode.HIGH -> {
                            setImageAndIndicators(4)
                            binding.ivLevelShower.visibility = View.GONE
                            binding.speedLimitSignView.visibility = View.VISIBLE
                            binding.speedLimitSignView.setSpeedLimit(10)
                            //TODO send to can1 frame 0x268 speed limit on CMB
                        }

                        Mode.MID -> {
                            setImageAndIndicators(3)
                            binding.ivLevelShower.visibility = View.GONE
                            binding.speedLimitSignView.visibility = View.VISIBLE
                            binding.speedLimitSignView.setSpeedLimit(40)
                            //TODO send to can1 frame 0x268 speed limit on CMB
                        }

                        Mode.NORMAL -> {
                            setImageAndIndicators(2)
                            binding.ivLevelShower.visibility = View.GONE
                            binding.speedLimitSignView.visibility = View.GONE
                            //TODO send to can1 frame 0x268 speed limit on CMB cancel
                        }

                        Mode.LOW -> {
                            setImageAndIndicators(2)
                            binding.ivLevelShower.visibility = View.GONE
                            binding.speedLimitSignView.setSpeedLimit(10)
                            binding.speedLimitSignView.visibility = View.VISIBLE
                            //TODO send to can1 frame 0x268 speed limit on CMB
                        }

                        Mode.LOW_TO_NORMAL -> {
                            binding.ivLevelShower.visibility = View.VISIBLE
                            binding.ivLevelShower.setImageResource(R.drawable.low_to_norm)
                            binding.speedLimitSignView.visibility = View.GONE
                            //TODO send to can1 frame 0x268 speed limit on CMB cancel
                        }

                        Mode.LOW_TO_MED -> {
                            binding.ivLevelShower.visibility = View.VISIBLE
                            binding.ivLevelShower.setImageResource(R.drawable.low_to_mid)
                            binding.speedLimitSignView.setSpeedLimit(40)
                            binding.speedLimitSignView.visibility = View.VISIBLE
                        }

                        Mode.LOW_TO_HIGH -> {
                            binding.ivLevelShower.visibility = View.VISIBLE
                            binding.ivLevelShower.setImageResource(R.drawable.low_to_high)
                            binding.speedLimitSignView.setSpeedLimit(10)
                            binding.speedLimitSignView.visibility = View.VISIBLE
                        }

                        Mode.NORMAL_TO_LOW -> {
                            binding.ivLevelShower.visibility = View.VISIBLE
                            binding.ivLevelShower.setImageResource(R.drawable.norm_to_low)
                            binding.speedLimitSignView.setSpeedLimit(10)
                            binding.speedLimitSignView.visibility = View.VISIBLE
                        }

                        Mode.NORMAL_TO_MED -> {
                            binding.ivLevelShower.visibility = View.VISIBLE
                            binding.ivLevelShower.setImageResource(R.drawable.norm_to_mid)
                            binding.speedLimitSignView.setSpeedLimit(40)
                            binding.speedLimitSignView.visibility = View.VISIBLE
                        }

                        Mode.NORMAL_TO_HIGH -> {
                            binding.ivLevelShower.visibility = View.VISIBLE
                            binding.ivLevelShower.setImageResource(R.drawable.norm_to_high)
                            binding.speedLimitSignView.setSpeedLimit(10)
                            binding.speedLimitSignView.visibility = View.VISIBLE
                        }

                        Mode.MED_TO_LOW -> {
                            binding.ivLevelShower.visibility = View.VISIBLE
                            binding.ivLevelShower.setImageResource(R.drawable.med_to_low)
                            binding.speedLimitSignView.setSpeedLimit(10)
                            binding.speedLimitSignView.visibility = View.VISIBLE
                        }

                        Mode.MED_TO_NORMAL -> {
                            binding.ivLevelShower.visibility = View.VISIBLE
                            binding.ivLevelShower.setImageResource(R.drawable.med_to_norm)
                            binding.speedLimitSignView.visibility = View.GONE
                            //TODO send to can1 frame 0x268 speed limit on CMB cancel
                        }

                        Mode.MED_TO_HIGH -> {
                            binding.ivLevelShower.visibility = View.VISIBLE
                            binding.ivLevelShower.setImageResource(R.drawable.med_to_high)
                            binding.speedLimitSignView.setSpeedLimit(10)
                            binding.speedLimitSignView.visibility = View.VISIBLE
                        }

                        Mode.HIGH_TO_LOW -> {
                            binding.ivLevelShower.visibility = View.VISIBLE
                            binding.ivLevelShower.setImageResource(R.drawable.high_to_low)
                            binding.speedLimitSignView.setSpeedLimit(10)
                            binding.speedLimitSignView.visibility = View.VISIBLE
                        }

                        Mode.HIGH_TO_NORMAL -> {
                            binding.ivLevelShower.visibility = View.VISIBLE
                            binding.ivLevelShower.setImageResource(R.drawable.high_to_norm)
                            binding.speedLimitSignView.visibility = View.GONE
                            //TODO send to can1 frame 0x268 speed limit on CMB cancel
                        }

                        Mode.HIGH_TO_MED -> {
                            binding.ivLevelShower.visibility = View.VISIBLE
                            binding.ivLevelShower.setImageResource(R.drawable.high_to_med)
                            binding.speedLimitSignView.setSpeedLimit(40)
                            binding.speedLimitSignView.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mainViewModel.setToBackground(true)
    }

    override fun onStart() {
        super.onStart()
        mainViewModel.setToBackground(false)
    }

    private fun setImageAndIndicators(id: Int) {
        binding.ivHigh.setBackgroundColor(ContextCompat.getColor(this, if (id == 4) R.color.main_yellow else R.color.semi_yellow))
        binding.ivMid.setBackgroundColor(ContextCompat.getColor(this, if (id == 3) R.color.main_yellow else R.color.semi_yellow))
        binding.ivNormal.setBackgroundColor(ContextCompat.getColor(this, if (id == 2) R.color.main_yellow else R.color.semi_yellow))
        binding.ivLow.setBackgroundColor(ContextCompat.getColor(this, if (id == 1) R.color.main_yellow else R.color.semi_yellow))
        when (id) {
            1 -> binding.imageView.setImageResource(R.drawable.c5max_low_pos)
            2 -> binding.imageView.setImageResource(R.drawable.c5max)
            3 -> binding.imageView.setImageResource(R.drawable.c5max_mid_pos)
            4 -> binding.imageView.setImageResource(R.drawable.c5max_max_pos)
        }
    }

}