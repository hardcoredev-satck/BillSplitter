package com.darshit.billsplitter.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.darshit.billsplitter.data.model.Participant
import com.darshit.billsplitter.databinding.ItemParticipantBinding
import com.darshit.billsplitter.utils.CurrencyUtils

class ParticipantAdapter(
    private val currencySymbol: String,
    private val onMarkPaid: (Long) -> Unit
) : ListAdapter<Participant, ParticipantAdapter.VH>(Diff()) {

    private val avatarColors = listOf(
        "#F44336","#E91E63","#9C27B0","#3F51B5","#2196F3",
        "#009688","#4CAF50","#FF9800","#795548","#607D8B"
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemParticipantBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val b: ItemParticipantBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(p: Participant) {
            b.tvParticipantName.text = p.name
            b.tvParticipantShare.text = CurrencyUtils.formatAmount(p.shareAmount, currencySymbol)
            b.tvParticipantInitial.text = p.name.firstOrNull()?.uppercase() ?: "?"
            val colorHex = avatarColors[p.avatarColor % avatarColors.size]
            b.tvParticipantInitial.backgroundTintList =
                android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(colorHex))
            if (p.isPaid) {
                b.tvPaidStatus.text = "Paid ✓"
                b.tvPaidStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                b.btnMarkPaid.visibility = android.view.View.GONE
            } else {
                b.tvPaidStatus.text = "Pending"
                b.tvPaidStatus.setTextColor(android.graphics.Color.parseColor("#FF9800"))
                b.btnMarkPaid.visibility = android.view.View.VISIBLE
                b.btnMarkPaid.setOnClickListener { onMarkPaid(p.id) }
            }
        }
    }

    class Diff : DiffUtil.ItemCallback<Participant>() {
        override fun areItemsTheSame(a: Participant, b: Participant) = a.id == b.id
        override fun areContentsTheSame(a: Participant, b: Participant) = a == b
    }
}
