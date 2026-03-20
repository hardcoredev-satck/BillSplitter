package com.darshit.billsplitter.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.darshit.billsplitter.data.model.SettlementTransaction

@Dao
interface TransactionDao {
    @Query("SELECT * FROM settlement_transactions WHERE billId = :billId ORDER BY createdAt DESC")
    fun getTransactionsForBill(billId: Long): LiveData<List<SettlementTransaction>>

    @Query("SELECT * FROM settlement_transactions WHERE billId = :billId")
    suspend fun getTransactionsForBillSync(billId: Long): List<SettlementTransaction>

    @Query("SELECT * FROM settlement_transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): LiveData<List<SettlementTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: SettlementTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<SettlementTransaction>)

    @Update
    suspend fun updateTransaction(transaction: SettlementTransaction)

    @Query("UPDATE settlement_transactions SET isSettled = 1, settledAt = :settledAt WHERE id = :transactionId")
    suspend fun markTransactionSettled(transactionId: Long, settledAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM settlement_transactions WHERE billId = :billId")
    suspend fun deleteTransactionsForBill(billId: Long)
}
