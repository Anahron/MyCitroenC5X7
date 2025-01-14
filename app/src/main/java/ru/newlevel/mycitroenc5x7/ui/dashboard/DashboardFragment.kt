package ru.newlevel.mycitroenc5x7.ui.dashboard

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.mycitroenc5x7.R
import ru.newlevel.mycitroenc5x7.databinding.FragmentDashboardBinding
import ru.newlevel.mycitroenc5x7.ui.home.HomeViewModel

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val binding: FragmentDashboardBinding by viewBinding()
    private val dashboardViewModel by viewModel<HomeViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.materialButtonToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            when (checkedId) {
                R.id.button_1 -> if (isChecked) {
                    binding.linearLayout1.visibility = View.VISIBLE
                    binding.linearLayout2.visibility = View.GONE
                }

                R.id.button_2 -> if (isChecked) {
                    binding.linearLayout2.visibility = View.VISIBLE
                    binding.linearLayout1.visibility = View.GONE
                }

                R.id.button_3 -> if (isChecked) { /* Действие для кнопки 3 */
                }
            }
        }
    }
}