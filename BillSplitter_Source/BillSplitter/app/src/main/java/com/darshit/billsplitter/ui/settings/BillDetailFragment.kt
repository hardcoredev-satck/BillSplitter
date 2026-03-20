package com.darshit.billsplitter.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.darshit.billsplitter.BillSplitterApp
import com.darshit.billsplitter.data.model.Bill
import com.darshit.billsplitter.data.model.Participant
import com.darshit.billsplitter.data.model.SettlementTransaction
import com.darshit.billsplitter.databinding.FragmentBillDetailBinding
import com.darshit.billsplitter.utils.CurrencyUtils
import com.darshit.billsplitter.utils.DateUtils
import com.darshit.billsplitter.utils.ExportUtils
import com.darshit.billsplitter.utils.PreferenceManager
import com.darshit.billsplitter.utils.ViewModelFactory
import com.darshit.billsplitter.ui.history.ParticipantAdapter
import com.darshit.billsplitter.ui.history.TransactionAdapter

class BillDetailFragment : Fragment() {

    private var _binding: FragmentBillDetailBinding? = null
    private val binding get() = _binding!!

    private val args: BillDetailFragmentArgs by navArgs()

    private val viewModel: BillDetailViewModel by viewModels {
        ViewModelFactory((requireActivity().application as BillSplitterApp).repository)
    }

    private lateinit var prefManager: PreferenceManager
    private var currentBill: Bill? = null
    private var currentParticipants: List<Participant> = emptyList()
    private var currentTransactions: List<SettlementTransaction> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBillDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefManager = PreferenceManager(requireContext())

        setupRecyclerViews()
        setupObservers()
        setupButtons()

        viewModel.loadBill(args.billId)
    }

    private fun setupRecyclerViews() {
        binding.rvParticipants.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ParticipantAdapter(prefManager.getCurrencySymbol()) { participantId ->
                viewModel.markParticipantPaid(participantId, args.billId)
            }
            isNestedScrollingEnabled = false
        }

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = TransactionAdapter(prefManager.getCurrencySymbol()) { transactionId ->
                viewModel.markTransactionSettled(transactionId, args.billId)
            }
            isNestedScrollingEnabled = false
        }
    }

    private fun setupObservers() {
        val symbol = prefManager.getCurrencySymbol()

        viewModel.bill.observe(viewLifecycleOwner) { bill ->
            bill ?: return@observe
            currentBill = bill
            binding.tvDetailTitle.text = bill.title
            binding.tvDetailAmount.text = CurrencyUtils.formatAmount(bill.totalAmount, symbol)
            binding.tvDetailDate.text = DateUtils.formatDate(bill.date)
            binding.tvDetailCategory.text = bill.category
            binding.tvDetailPaidBy.text = bill.paidByName
            binding.tvDetailSplitType.text = bill.splitType.lowercase().replaceFirstChar { it.uppercase() } + " split"
            binding.tvDetailDescription.text = if (bill.description.isBlank()) "No description" else bill.description

            if (bill.isSettled) {
                binding.chipDetailStatus.text = "Settled ✓"
                binding.chipDetailStatus.setChipBackgroundColorResource(com.darshit.billsplitter.R.color.settled_color)
            } else {
                binding.chipDetailStatus.text = "Active"
                binding.chipDetailStatus.setChipBackgroundColorResource(com.darshit.billsplitter.R.color.active_color)
            }
        }

        viewModel.participants.observe(viewLifecycleOwner) { participants ->
            currentParticipants = participants
            (binding.rvParticipants.adapter as? ParticipantAdapter)?.submitList(participants)
        }

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            currentTransactions = transactions
            (binding.rvTransactions.adapter as? TransactionAdapter)?.submitList(transactions)

            if (transactions.isEmpty()) {
                binding.tvNoSettlements.visibility = View.VISIBLE
                binding.rvTransactions.visibility = View.GONE
            } else {
                binding.tvNoSettlements.visibility = View.GONE
                binding.rvTransactions.visibility = View.VISIBLE
            }
        }
    }

    private fun setupButtons() {
        binding.btnShareBill.setOnClickListener {
            val bill = currentBill ?: return@setOnClickListener
            val summaryText = ExportUtils.buildBillSummaryText(
                bill, currentParticipants, currentTransactions, prefManager.getCurrencySymbol()
            )
            ExportUtils.shareBillSummary(requireContext(), summaryText, bill.title)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
