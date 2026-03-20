package com.darshit.billsplitter.data.repository

import androidx.lifecycle.LiveData
import com.darshit.billsplitter.data.db.BillDao
import com.darshit.billsplitter.data.db.ParticipantDao
import com.darshit.billsplitter.data.db.TransactionDao
import com.darshit.billsplitter.data.model.*

class BillRepository(
    private val billDao: BillDao,
    private val participantDao: ParticipantDao,
    private val transactionDao: TransactionDao
) {
    val allBills: LiveData<List<Bill>> = billDao.getAllBills()
    val activeBills: LiveData<List<Bill>> = billDao.getActiveBills()
    val settledBills: LiveData<List<Bill>> = billDao.getSettledBills()
    val allTransactions: LiveData<List<SettlementTransaction>> = transactionDao.getAllTransactions()

    suspend fun insertBill(bill: Bill, participants: List<Participant>): Long {
        val billId = billDao.insertBill(bill)
        val participantsWithBillId = participants.map { it.copy(billId = billId) }
        participantDao.insertParticipants(participantsWithBillId)

        // Generate settlement transactions
        val settlements = calculateSettlements(bill.copy(id = billId), participantsWithBillId.map { it.copy(billId = billId) })
        if (settlements.isNotEmpty()) {
            transactionDao.insertTransactions(settlements.map {
                SettlementTransaction(
                    billId = billId,
                    fromParticipant = it.from,
                    toParticipant = it.to,
                    amount = it.amount,
                    currency = bill.currency
                )
            })
        }
        return billId
    }

    suspend fun updateBill(bill: Bill) = billDao.updateBill(bill)

    suspend fun deleteBill(billId: Long) {
        participantDao.deleteParticipantsForBill(billId)
        transactionDao.deleteTransactionsForBill(billId)
        billDao.deleteBillById(billId)
    }

    suspend fun getBillById(billId: Long): Bill? = billDao.getBillById(billId)

    fun getParticipantsForBill(billId: Long): LiveData<List<Participant>> =
        participantDao.getParticipantsForBill(billId)

    suspend fun getParticipantsForBillSync(billId: Long): List<Participant> =
        participantDao.getParticipantsForBillSync(billId)

    fun getTransactionsForBill(billId: Long): LiveData<List<SettlementTransaction>> =
        transactionDao.getTransactionsForBill(billId)

    suspend fun getTransactionsForBillSync(billId: Long): List<SettlementTransaction> =
        transactionDao.getTransactionsForBillSync(billId)

    suspend fun markBillSettled(billId: Long) = billDao.markBillAsSettled(billId)

    suspend fun markParticipantPaid(participantId: Long) =
        participantDao.markParticipantPaid(participantId)

    suspend fun markTransactionSettled(transactionId: Long) =
        transactionDao.markTransactionSettled(transactionId)

    suspend fun getTotalBillCount(): Int = billDao.getTotalBillCount()

    suspend fun getTotalOutstanding(): Double = billDao.getTotalOutstanding() ?: 0.0

    suspend fun getAllParticipantNames(): List<String> = participantDao.getAllParticipantNames()

    suspend fun getAllBillsSync(): List<Bill> = billDao.getAllBillsSync()

    fun searchBills(query: String): LiveData<List<Bill>> = billDao.searchBills(query)

    // Calculate who owes whom using debt minimization algorithm
    fun calculateSettlements(bill: Bill, participants: List<Participant>): List<SettlementSuggestion> {
        if (participants.isEmpty()) return emptyList()

        val paidByName = bill.paidByName
        val balances = mutableMapOf<String, Double>()

        // Person who paid is owed the total minus their own share
        participants.forEach { p ->
            balances[p.name] = (balances[p.name] ?: 0.0) - p.shareAmount
        }

        // The payer gets credited the full amount
        balances[paidByName] = (balances[paidByName] ?: 0.0) + bill.totalAmount

        // Use greedy algorithm to minimize number of transactions
        val suggestions = mutableListOf<SettlementSuggestion>()
        val creditors = balances.filter { it.value > 0.001 }.toMutableMap()
        val debtors = balances.filter { it.value < -0.001 }.toMutableMap()

        val creditorList = creditors.entries.sortedByDescending { it.value }.toMutableList()
        val debtorList = debtors.entries.sortedBy { it.value }.toMutableList()

        var ci = 0
        var di = 0

        while (ci < creditorList.size && di < debtorList.size) {
            val creditor = creditorList[ci]
            val debtor = debtorList[di]

            val amount = minOf(creditor.value, -debtor.value)
            if (amount > 0.01) {
                suggestions.add(
                    SettlementSuggestion(
                        from = debtor.key,
                        to = creditor.key,
                        amount = Math.round(amount * 100.0) / 100.0
                    )
                )
            }

            creditorList[ci] = creditor.copy(value = creditor.value - amount)
            debtorList[di] = debtor.copy(value = debtor.value + amount)

            if (creditorList[ci].value < 0.01) ci++
            if (-debtorList[di].value < 0.01) di++
        }

        return suggestions
    }
}
