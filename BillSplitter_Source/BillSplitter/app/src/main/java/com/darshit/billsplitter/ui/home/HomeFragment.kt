package com.darshit.billsplitter.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.darshit.billsplitter.BillSplitterApp
import com.darshit.billsplitter.R
import com.darshit.billsplitter.databinding.FragmentHomeBinding
import com.darshit.billsplitter.utils.CurrencyUtils
import com.darshit.billsplitter.utils.PreferenceManager
import com.darshit.billsplitter.utils.ViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory((requireActivity().application as BillSplitterApp).repository)
    }

    private lateinit var billAdapter: HomeBillAdapter
    private lateinit var prefManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefManager = PreferenceManager(requireContext())

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        billAdapter = HomeBillAdapter(
            currencySymbol = prefManager.getCurrencySymbol(),
            onBillClick = { bill ->
                val action = HomeFragmentDirections.actionHomeFragmentToBillDetailFragment(bill.id)
                findNavController().navigate(action)
            },
            onSettleClick = { bill ->
                viewModel.markBillSettled(bill.id)
            }
        )
        binding.rvRecentBills.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = billAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupObservers() {
        val symbol = prefManager.getCurrencySymbol()

        viewModel.activeBills.observe(viewLifecycleOwner) { bills ->
            if (bills.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvRecentBills.visibility = View.GONE
                binding.tvActiveBillsCount.text = "0 active bills"
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvRecentBills.visibility = View.VISIBLE
                billAdapter.submitList(bills.take(5))
                binding.tvActiveBillsCount.text = "${bills.size} active bill${if (bills.size != 1) "s" else ""}"
            }
        }

        viewModel.totalOutstanding.observe(viewLifecycleOwner) { total ->
            binding.tvTotalOutstanding.text = CurrencyUtils.formatAmount(total, symbol)
        }

        viewModel.totalBillCount.observe(viewLifecycleOwner) { count ->
            binding.tvTotalBills.text = count.toString()
        }
    }

    private fun setupClickListeners() {
        binding.fabAddBill.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addBillFragment)
        }

        binding.btnViewAll.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_historyFragment)
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
