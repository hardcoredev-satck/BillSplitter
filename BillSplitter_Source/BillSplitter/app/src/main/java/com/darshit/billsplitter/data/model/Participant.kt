package com.darshit.billsplitter.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "participants",
    foreignKeys = [ForeignKey(
        entity = Bill::class,
        parentColumns = ["id"],
        childColumns = ["billId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("billId")]
)
data class Participant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val billId: Long,
    val name: String,
    val shareAmount: Double = 0.0,
    val sharePercentage: Double = 0.0,
    val isPaid: Boolean = false,
    val avatarColor: Int = 0, // color index 0-9
    val phone: String = ""
) : Parcelable
