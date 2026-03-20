package com.darshit.billsplitter.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.darshit.billsplitter.databinding.FragmentSettingsBinding
import com.darshit.billsplitter.utils.CurrencyUtils
import com.darshit.billsplitter.utils.PreferenceManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefManager: PreferenceManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefManager = PreferenceManager(requireContext())

        setupDarkModeToggle()
        setupCurrencySelector()
        setupUserName()
        setupAboutSection()
    }

    private fun setupDarkModeToggle() {
        binding.switchDarkMode.isChecked = prefManager.isDarkMode()
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefManager.setDarkMode(isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun setupCurrencySelector() {
        val currencies = CurrencyUtils.currencies
        val displayList = currencies.map { "${it.symbol}  ${it.code} — ${it.name}" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, displayList)
        binding.actvCurrency.setAdapter(adapter)

        val currentCode = prefManager.getCurrency()
        val currentIdx = currencies.indexOfFirst { it.code == currentCode }
        if (currentIdx >= 0) binding.actvCurrency.setText(displayList[currentIdx], false)

        binding.actvCurrency.setOnItemClickListener { _, _, position, _ ->
            val selected = currencies[position]
            prefManager.setCurrency(selected.code, selected.symbol)
            Toast.makeText(requireContext(), "Currency set to ${selected.code}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUserName() {
        binding.etUserName.setText(prefManager.getUserName())
        binding.btnSaveName.setOnClickListener {
            val name = binding.etUserName.text.toString().trim()
            prefManager.setUserName(name)
            Toast.makeText(requireContext(), "Name saved!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAboutSection() {
        binding.tvAppVersion.text = "Version 1.0.0"
        binding.tvDeveloper.text = "Developed by Darshit"
        binding.tvDescription.text =
            "Bill Splitter helps you easily split expenses among friends, family, and colleagues. " +
            "Track who owes what, settle up quickly, and keep your finances organised."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
