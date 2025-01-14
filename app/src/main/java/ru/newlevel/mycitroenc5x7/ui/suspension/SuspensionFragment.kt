package ru.newlevel.mycitroenc5x7.ui.suspension

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
import ru.newlevel.mycitroenc5x7.databinding.FragmentSuspensionBinding

class SuspensionFragment() : Fragment(R.layout.fragment_suspension) {


    private val binding: FragmentSuspensionBinding by viewBinding()
    private val suspensionViewModel by viewModel<SuspensionViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectUiState()
    }

    private fun collectUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            suspensionViewModel.suspensionState.collect {
                Log.e(TAG, " homeViewModel.uiState.collect ")
//                when (it.suspensionState.mode) {
//
//                    }
            }
        }
    }
}