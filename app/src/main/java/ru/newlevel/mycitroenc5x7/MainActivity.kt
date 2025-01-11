package ru.newlevel.mycitroenc5x7


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.newlevel.mycitroenc5x7.app.TAG
import ru.newlevel.mycitroenc5x7.databinding.ActivityMainBinding
import ru.newlevel.mycitroenc5x7.repository.CanRepo
import ru.newlevel.mycitroenc5x7.service.UsbService

class MainActivity : AppCompatActivity(), KoinComponent {

    private lateinit var binding: ActivityMainBinding
    private val canRepo: CanRepo by inject()

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        val intent = Intent(this, UsbService::class.java)
        startForegroundService(intent)
        collectLog()
        collectData()
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun collectData() {
        lifecycleScope.launch {
            canRepo.canDataFlow.collect {
                Log.e(TAG, "dataFlowCollect")

                binding.text.append("\nID: 0x${it.canId.toString(16).uppercase()}, DLC: ${it.dlc} Data: ${
                    it.data.joinToString(", ") {
                        "0x${it.toString(16).uppercase()}"
                    }
                }")
              //  binding.text.append("\nid: ${it.canId} dlc: ${it.dlc} data: ${it.data}")
            }
        }
    }

    private fun collectLog() {
        lifecycleScope.launch {
            canRepo.logger.collect {
                Log.e(TAG, "dataFlowCollect")
                binding.text.append("\n${it}")
            }
        }
    }


}