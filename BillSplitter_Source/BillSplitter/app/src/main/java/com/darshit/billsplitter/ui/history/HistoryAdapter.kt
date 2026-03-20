package com.darshit.billsplitter.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.darshit.billsplitter.R
import com.darshit.billsplitter.data.model.Bill
import com.darshit.billsplitter.data.model.Category
import com.darshit.billsplitter.databinding.ItemBillHistoryBinding
import com.darshit.billsplitter.utils.CurrencyUtils
import com.darshit.billsplitter.utils.DateUtils

class HistoryAdapter(
    private val currencySymbol: String,
    private val onBillClick: (Bill) -> Unit,
    private val onDeleteClick: (Bill) -> Unit,
    private val onSettleClick: (Bill) -> Unit
) : ListAdapter<Bill, HistoryAdapter.HistoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemBillHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(private val binding: ItemBillHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bill: Bill) {
            binding.tvTitle.text = bill.title
            binding.tvAmount.text = CurrencyUtils.formatAmount(bill.totalAmount, currencySymbol)
            binding.tvDate.text = DateUtils.formatRelativeDate(bill.date)
            binding.tvCategory.text = getCategoryEmoji(bill.category) + " " + bill.category
            binding.tvPaidBy.text = "Paid by ${bill.paidByName}"
            binding.tvSplitType.text = bill.splitType.lowercase().replaceFirstChar { it.uppercase() } + " split"

            if (bill.isSettled) {
                binding.chipStatus.text = "Settled ✓"
                binding.chipStatus.setChipBackgroundColorResource(R.color.settled_color)
                binding.btnSettle.visibility = android.view.View.GONE
            } else {
                binding.chipStatus.text = "Active"
                binding.chipStatus.setChipBackgroundColorResource(R.color.active_color)
                binding.btnSettle.visibility = android.view.View.VISIBLE
            }

            binding.root.setOnClickListener { onBillClick(bill) }
            binding.btnDelete.setOnClickListener { onDeleteClick(bill) }
            binding.btnSettle.setOnClickListener { onSettleClick(bill) }
        }

        private fun getCategoryEmoji(category: String): String {
            return Category.values().find { it.displayName == category || it.name == category }?.emoji ?: "📦"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Bill>() {
        override fun areItemsTheSame(oldItem: Bill, newItem: Bill) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Bill, newItem: Bill) = oldItem == newItem
    }
}
