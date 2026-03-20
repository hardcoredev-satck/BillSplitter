package com.darshit.billsplitter.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val totalAmount: Double,
    val currency: String = "USD",
    val category: String = "General",
    val date: Long = System.currentTimeMillis(),
    val splitType: String = "EQUAL", // EQUAL, CUSTOM, PERCENTAGE
    val paidById: Long = 0,
    val paidByName: String = "",
    val isSettled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

enum class SplitType {
    EQUAL, CUSTOM, PERCENTAGE
}

enum class Category(val displayName: String, val emoji: String) {
    FOOD("Food & Dining", "🍔"),
    TRANSPORT("Transport", "🚗"),
    ENTERTAINMENT("Entertainment", "🎬"),
    UTILITIES("Utilities", "💡"),
    SHOPPING("Shopping", "🛍️"),
    ACCOMMODATION("Accommodation", "🏠"),
    HEALTH("Health", "💊"),
    TRAVEL("Travel", "✈️"),
    GENERAL("General", "📦")
}
