package com.darshit.billsplitter.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.darshit.billsplitter.data.model.SettlementTransaction
import com.darshit.billsplitter.databinding.ItemTransactionBinding
import com.darshit.billsplitter.utils.CurrencyUtils

class TransactionAdapter(
    private val currencySymbol: String,
    private val onMarkSettled: (Long) -> Unit
) : ListAdapter<SettlementTransaction, TransactionAdapter.VH>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val b: ItemTransactionBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(t: SettlementTransaction) {
            b.tvFrom.text = t.fromParticipant
            b.tvTo.text = t.toParticipant
            b.tvTransactionAmount.text = CurrencyUtils.formatAmount(t.amount, currencySymbol)
            if (t.isSettled) {
                b.tvTransactionStatus.text = "Settled ✓"
                b.tvTransactionStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                b.btnMarkSettled.visibility = android.view.View.GONE
            } else {
                b.tvTransactionStatus.text = "Pending"
                b.tvTransactionStatus.setTextColor(android.graphics.Color.parseColor("#FF9800"))
                b.btnMarkSettled.visibility = android.view.View.VISIBLE
                b.btnMarkSettled.setOnClickListener { onMarkSettled(t.id) }
            }
        }
    }

    class Diff : DiffUtil.ItemCallback<SettlementTransaction>() {
        override fun areItemsTheSame(a: SettlementTransaction, b: SettlementTransaction) = a.id == b.id
        override fun areContentsTheSame(a: SettlementTransaction, b: SettlementTransaction) = a == b
    }
}
