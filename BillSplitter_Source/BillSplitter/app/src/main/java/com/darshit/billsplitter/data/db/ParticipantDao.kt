package com.darshit.billsplitter.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.darshit.billsplitter.data.model.Participant

@Dao
interface ParticipantDao {
    @Query("SELECT * FROM participants WHERE billId = :billId")
    fun getParticipantsForBill(billId: Long): LiveData<List<Participant>>

    @Query("SELECT * FROM participants WHERE billId = :billId")
    suspend fun getParticipantsForBillSync(billId: Long): List<Participant>

    @Query("SELECT DISTINCT name FROM participants ORDER BY name ASC")
    suspend fun getAllParticipantNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: Participant): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipants(participants: List<Participant>)

    @Update
    suspend fun updateParticipant(participant: Participant)

    @Query("UPDATE participants SET isPaid = 1 WHERE id = :participantId")
    suspend fun markParticipantPaid(participantId: Long)

    @Delete
    suspend fun deleteParticipant(participant: Participant)

    @Query("DELETE FROM participants WHERE billId = :billId")
    suspend fun deleteParticipantsForBill(billId: Long)
}
