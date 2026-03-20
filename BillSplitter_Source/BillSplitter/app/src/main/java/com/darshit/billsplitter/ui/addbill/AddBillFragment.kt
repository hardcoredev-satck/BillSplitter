package com.darshit.billsplitter.ui.addbill

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.darshit.billsplitter.BillSplitterApp
import com.darshit.billsplitter.data.model.Category
import com.darshit.billsplitter.databinding.FragmentAddBillBinding
import com.darshit.billsplitter.utils.DateUtils
import com.darshit.billsplitter.utils.PreferenceManager
import com.darshit.billsplitter.utils.ViewModelFactory
import com.google.android.material.chip.Chip
import java.util.*

class AddBillFragment : Fragment() {

    private var _binding: FragmentAddBillBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddBillViewModel by viewModels {
        ViewModelFactory((requireActivity().application as BillSplitterApp).repository)
    }

    private var selectedDate: Long = System.currentTimeMillis()
    private val participants = mutableListOf<String>()
    private lateinit var prefManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBillBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefManager = PreferenceManager(requireContext())

        setupCategoryDropdown()
        setupSplitTypeButtons()
        setupDatePicker()
        setupParticipants()
        setupSaveButton()
        setupObservers()

        // Set current currency
        binding.tvCurrency.text = prefManager.getCurrency()
        binding.tvDateValue.text = DateUtils.formatDate(selectedDate)
    }

    private fun setupCategoryDropdown() {
        val categories = Category.values().map { "${it.emoji} ${it.displayName}" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)
        binding.actvCategory.setText("${Category.GENERAL.emoji} ${Category.GENERAL.displayName}", false)
    }

    private fun setupSplitTypeButtons() {
        binding.btnSplitEqual.isChecked = true
        binding.rgSplitType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.btnSplitEqual.id -> {
                    binding.layoutCustomAmounts.visibility = View.GONE
                }
                binding.btnSplitCustom.id -> {
                    binding.layoutCustomAmounts.visibility = View.VISIBLE
                    refreshCustomAmountFields()
                }
                binding.btnSplitPercentage.id -> {
                    binding.layoutCustomAmounts.visibility = View.VISIBLE
                    refreshCustomAmountFields()
                }
            }
        }
    }

    private fun setupDatePicker() {
        binding.btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val newCal = Calendar.getInstance().apply { set(year, month, day) }
                    selectedDate = newCal.timeInMillis
                    binding.tvDateValue.text = DateUtils.formatDate(selectedDate)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupParticipants() {
        binding.btnAddParticipant.setOnClickListener {
            val name = binding.etParticipantName.text.toString().trim()
            if (name.isBlank()) {
                binding.etParticipantName.error = "Enter a name"
                return@setOnClickListener
            }
            if (participants.contains(name)) {
                binding.etParticipantName.error = "Already added"
                return@setOnClickListener
            }
            addParticipantChip(name)
            participants.add(name)
            binding.etParticipantName.setText("")

            // Update paid by spinner
            updatePaidBySpinner()
            refreshCustomAmountFields()
        }

        // Quick add suggestions from recent participants
        viewModel.recentParticipants.observe(viewLifecycleOwner) { names ->
            if (names.isNotEmpty()) {
                binding.layoutSuggestions.visibility = View.VISIBLE
                binding.chipGroupSuggestions.removeAllViews()
                names.take(6).forEach { name ->
                    val chip = Chip(requireContext()).apply {
                        text = name
                        isClickable = true
                        setOnClickListener {
                            if (!participants.contains(name)) {
                                addParticipantChip(name)
                                participants.add(name)
                                updatePaidBySpinner()
                                refreshCustomAmountFields()
                            }
                        }
                    }
                    binding.chipGroupSuggestions.addView(chip)
                }
            }
        }
    }

    private fun addParticipantChip(name: String) {
        val chip = Chip(requireContext()).apply {
            text = name
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                binding.chipGroupParticipants.removeView(this)
                participants.remove(name)
                updatePaidBySpinner()
                refreshCustomAmountFields()
            }
        }
        binding.chipGroupParticipants.addView(chip)
    }

    private fun updatePaidBySpinner() {
        val adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_dropdown_item_1line, participants)
        binding.actvPaidBy.setAdapter(adapter)
        if (participants.size == 1) {
            binding.actvPaidBy.setText(participants[0], false)
        }
    }

    private fun refreshCustomAmountFields() {
        if (binding.layoutCustomAmounts.visibility == View.GONE) return
        binding.layoutCustomAmounts.removeAllViews()

        val isPercentage = binding.btnSplitPercentage.isChecked
        val label = if (isPercentage) "%" else prefManager.getCurrencySymbol()

        participants.forEach { name ->
            val itemView = layoutInflater.inflate(
                com.darshit.billsplitter.R.layout.item_custom_amount,
                binding.layoutCustomAmounts, false
            )
            val tvName = itemView.findViewById<android.widget.TextView>(com.darshit.billsplitter.R.id.tvParticipantNameCustom)
            val etAmount = itemView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.darshit.billsplitter.R.id.etCustomAmount)
            val tvLabel = itemView.findViewById<android.widget.TextView>(com.darshit.billsplitter.R.id.tvAmountLabel)

            tvName.text = name
            tvLabel.text = label
            etAmount.tag = name
            binding.layoutCustomAmounts.addView(itemView)
        }
    }

    private fun getCustomAmounts(): Map<String, Double> {
        val amounts = mutableMapOf<String, Double>()
        for (i in 0 until binding.layoutCustomAmounts.childCount) {
            val child = binding.layoutCustomAmounts.getChildAt(i)
            val name = child.tag as? String ?: child.findViewById<android.widget.TextView>(
                com.darshit.billsplitter.R.id.tvParticipantNameCustom)?.text?.toString() ?: continue
            val et = child.findViewById<com.google.android.material.textfield.TextInputEditText>(
                com.darshit.billsplitter.R.id.etCustomAmount)
            amounts[name] = et?.text?.toString()?.toDoubleOrNull() ?: 0.0
        }
        return amounts
    }

    private fun setupSaveButton() {
        binding.btnSaveBill.setOnClickListener {
            val title = binding.etBillTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val amountStr = binding.etTotalAmount.text.toString().trim()
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            val paidBy = binding.actvPaidBy.text.toString().trim()
            val categoryText = binding.actvCategory.text.toString()
            val category = Category.values().find {
                categoryText.contains(it.displayName) || categoryText.contains(it.name)
            }?.displayName ?: "General"

            val splitType = when {
                binding.btnSplitEqual.isChecked -> "EQUAL"
                binding.btnSplitCustom.isChecked -> "CUSTOM"
                binding.btnSplitPercentage.isChecked -> "PERCENTAGE"
                else -> "EQUAL"
            }

            viewModel.saveBill(
                title = title,
                description = description,
                totalAmount = amount,
                currency = prefManager.getCurrency(),
                category = category,
                date = selectedDate,
                splitType = splitType,
                paidByName = paidBy,
                participantNames = participants.toList(),
                customAmounts = if (splitType != "EQUAL") getCustomAmounts() else null
            )
        }
    }

    private fun setupObservers() {
        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Bill saved successfully! 🎉", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
