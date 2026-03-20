package com.darshit.billsplitter.ui.settings

import androidx.lifecycle.*
import com.darshit.billsplitter.data.model.Bill
import com.darshit.billsplitter.data.model.Participant
import com.darshit.billsplitter.data.model.SettlementTransaction
import com.darshit.billsplitter.data.repository.BillRepository
import kotlinx.coroutines.launch

class BillDetailViewModel(private val repository: BillRepository) : ViewModel() {

    private val _bill = MutableLiveData<Bill?>()
    val bill: LiveData<Bill?> = _bill

    private val _participants = MutableLiveData<List<Participant>>()
    val participants: LiveData<List<Participant>> = _participants

    private val _transactions = MutableLiveData<List<SettlementTransaction>>()
    val transactions: LiveData<List<SettlementTransaction>> = _transactions

    fun loadBill(billId: Long) {
        viewModelScope.launch {
            _bill.value = repository.getBillById(billId)
            _participants.value = repository.getParticipantsForBillSync(billId)
            _transactions.value = repository.getTransactionsForBillSync(billId)
        }
    }

    fun markTransactionSettled(transactionId: Long, billId: Long) {
        viewModelScope.launch {
            repository.markTransactionSettled(transactionId)
            _transactions.value = repository.getTransactionsForBillSync(billId)
        }
    }

    fun markParticipantPaid(participantId: Long, billId: Long) {
        viewModelScope.launch {
            repository.markParticipantPaid(participantId)
            _participants.value = repository.getParticipantsForBillSync(billId)
        }
    }
}
