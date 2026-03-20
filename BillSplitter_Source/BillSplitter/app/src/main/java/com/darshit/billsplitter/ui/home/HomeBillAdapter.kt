package com.darshit.billsplitter.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.darshit.billsplitter.R
import com.darshit.billsplitter.data.model.Bill
import com.darshit.billsplitter.data.model.Category
import com.darshit.billsplitter.databinding.ItemBillHomeBinding
import com.darshit.billsplitter.utils.CurrencyUtils
import com.darshit.billsplitter.utils.DateUtils

class HomeBillAdapter(
    private val currencySymbol: String,
    private val onBillClick: (Bill) -> Unit,
    private val onSettleClick: (Bill) -> Unit
) : ListAdapter<Bill, HomeBillAdapter.BillViewHolder>(BillDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val binding = ItemBillHomeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BillViewHolder(private val binding: ItemBillHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bill: Bill) {
            binding.tvBillTitle.text = bill.title
            binding.tvBillAmount.text = CurrencyUtils.formatAmount(bill.totalAmount, currencySymbol)
            binding.tvBillDate.text = DateUtils.formatRelativeDate(bill.date)
            binding.tvPaidBy.text = "Paid by ${bill.paidByName}"
            binding.tvCategory.text = getCategoryEmoji(bill.category) + " " + bill.category

            if (bill.isSettled) {
                binding.chipStatus.text = "Settled"
                binding.chipStatus.setChipBackgroundColorResource(R.color.settled_color)
            } else {
                binding.chipStatus.text = "Active"
                binding.chipStatus.setChipBackgroundColorResource(R.color.active_color)
            }

            binding.root.setOnClickListener { onBillClick(bill) }
            binding.btnSettle.setOnClickListener { onSettleClick(bill) }

            if (bill.isSettled) {
                binding.btnSettle.visibility = android.view.View.GONE
            } else {
                binding.btnSettle.visibility = android.view.View.VISIBLE
            }
        }

        private fun getCategoryEmoji(category: String): String {
            return Category.values().find { it.displayName == category || it.name == category }?.emoji ?: "📦"
        }
    }

    class BillDiffCallback : DiffUtil.ItemCallback<Bill>() {
        override fun areItemsTheSame(oldItem: Bill, newItem: Bill) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Bill, newItem: Bill) = oldItem == newItem
    }
}
