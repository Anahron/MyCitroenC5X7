package ru.newlevel.mycitroenc5x7.ui.suspension

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.newlevel.mycitroenc5x7.databinding.FragmentSuspensionBinding

class SuspensionFragment : Fragment() {

    private var _binding: FragmentSuspensionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val suspensionViewModel =
            ViewModelProvider(this).get(SuspensionViewModel::class.java)

        _binding = FragmentSuspensionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textNotifications
        suspensionViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}