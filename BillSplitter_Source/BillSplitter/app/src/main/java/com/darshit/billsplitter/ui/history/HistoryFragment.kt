package com.darshit.billsplitter.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.darshit.billsplitter.BillSplitterApp
import com.darshit.billsplitter.databinding.FragmentHistoryBinding
import com.darshit.billsplitter.utils.PreferenceManager
import com.darshit.billsplitter.utils.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels {
        ViewModelFactory((requireActivity().application as BillSplitterApp).repository)
    }

    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var prefManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefManager = PreferenceManager(requireContext())

        setupRecyclerView()
        setupFilters()
        setupSearch()
        setupObservers()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(
            currencySymbol = prefManager.getCurrencySymbol(),
            onBillClick = { bill ->
                val action = HistoryFragmentDirections.actionHistoryFragmentToBillDetailFragment(bill.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { bill ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Bill")
                    .setMessage("Are you sure you want to delete '${bill.title}'?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteBill(bill.id)
                    }
                    .show()
            },
            onSettleClick = { bill ->
                viewModel.markBillSettled(bill.id)
            }
        )
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }
    }

    private fun setupFilters() {
        binding.chipAll.isChecked = true
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when {
                checkedIds.contains(binding.chipActive.id) -> "ACTIVE"
                checkedIds.contains(binding.chipSettled.id) -> "SETTLED"
                else -> "ALL"
            }
            viewModel.setFilter(filter)
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            viewModel.search(text.toString())
        }
    }

    private fun setupObservers() {
        viewModel.allBills.observe(viewLifecycleOwner) {
            viewModel.setFilter("ALL")
        }

        viewModel.filteredBills.observe(viewLifecycleOwner) { bills ->
            if (bills.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvHistory.visibility = View.GONE
                binding.tvBillCount.text = "No bills found"
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvHistory.visibility = View.VISIBLE
                historyAdapter.submitList(bills)
                binding.tvBillCount.text = "${bills.size} bill${if (bills.size != 1) "s" else ""}"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
