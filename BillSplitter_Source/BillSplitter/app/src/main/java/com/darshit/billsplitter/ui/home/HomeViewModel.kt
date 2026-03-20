package com.darshit.billsplitter.ui.home

import androidx.lifecycle.*
import com.darshit.billsplitter.data.model.Bill
import com.darshit.billsplitter.data.repository.BillRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: BillRepository) : ViewModel() {

    val activeBills: LiveData<List<Bill>> = repository.activeBills

    private val _totalOutstanding = MutableLiveData<Double>(0.0)
    val totalOutstanding: LiveData<Double> = _totalOutstanding

    private val _totalBillCount = MutableLiveData<Int>(0)
    val totalBillCount: LiveData<Int> = _totalBillCount

    private val _recentBills = MutableLiveData<List<Bill>>()
    val recentBills: LiveData<List<Bill>> = _recentBills

    init {
        loadStats()
        loadRecentBills()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _totalOutstanding.value = repository.getTotalOutstanding()
            _totalBillCount.value = repository.getTotalBillCount()
        }
    }

    private fun loadRecentBills() {
        viewModelScope.launch {
            val bills = repository.getAllBillsSync().take(5)
            _recentBills.value = bills
        }
    }

    fun refresh() {
        loadStats()
        loadRecentBills()
    }

    fun deleteBill(billId: Long) {
        viewModelScope.launch {
            repository.deleteBill(billId)
            loadStats()
        }
    }

    fun markBillSettled(billId: Long) {
        viewModelScope.launch {
            repository.markBillSettled(billId)
            loadStats()
        }
    }
}
