package com.darshit.billsplitter.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.darshit.billsplitter.data.model.Bill

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY createdAt DESC")
    fun getAllBills(): LiveData<List<Bill>>

    @Query("SELECT * FROM bills ORDER BY createdAt DESC")
    suspend fun getAllBillsSync(): List<Bill>

    @Query("SELECT * FROM bills WHERE id = :billId")
    suspend fun getBillById(billId: Long): Bill?

    @Query("SELECT * FROM bills WHERE isSettled = 0 ORDER BY createdAt DESC")
    fun getActiveBills(): LiveData<List<Bill>>

    @Query("SELECT * FROM bills WHERE isSettled = 1 ORDER BY createdAt DESC")
    fun getSettledBills(): LiveData<List<Bill>>

    @Query("SELECT COUNT(*) FROM bills")
    suspend fun getTotalBillCount(): Int

    @Query("SELECT SUM(totalAmount) FROM bills WHERE isSettled = 0")
    suspend fun getTotalOutstanding(): Double?

    @Query("SELECT * FROM bills WHERE category = :category ORDER BY createdAt DESC")
    fun getBillsByCategory(category: String): LiveData<List<Bill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill): Long

    @Update
    suspend fun updateBill(bill: Bill)

    @Delete
    suspend fun deleteBill(bill: Bill)

    @Query("DELETE FROM bills WHERE id = :billId")
    suspend fun deleteBillById(billId: Long)

    @Query("UPDATE bills SET isSettled = 1 WHERE id = :billId")
    suspend fun markBillAsSettled(billId: Long)

    @Query("SELECT * FROM bills WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchBills(query: String): LiveData<List<Bill>>
}
