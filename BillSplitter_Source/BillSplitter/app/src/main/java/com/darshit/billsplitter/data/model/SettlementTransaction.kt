package com.darshit.billsplitter.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "settlement_transactions")
data class SettlementTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val billId: Long,
    val fromParticipant: String,
    val toParticipant: String,
    val amount: Double,
    val currency: String = "USD",
    val isSettled: Boolean = false,
    val settledAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

data class BillWithParticipants(
    val bill: Bill,
    val participants: List<Participant>
)

data class SettlementSuggestion(
    val from: String,
    val to: String,
    val amount: Double
)
