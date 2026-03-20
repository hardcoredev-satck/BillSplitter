package com.darshit.billsplitter.ui.history

import androidx.lifecycle.*
import com.darshit.billsplitter.data.model.Bill
import com.darshit.billsplitter.data.repository.BillRepository
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: BillRepository) : ViewModel() {

    val allBills: LiveData<List<Bill>> = repository.allBills

    private val _filteredBills = MutableLiveData<List<Bill>>()
    val filteredBills: LiveData<List<Bill>> = _filteredBills

    private var currentFilter = "ALL"
    private var searchQuery = ""

    fun setFilter(filter: String) {
        currentFilter = filter
        applyFilters()
    }

    fun search(query: String) {
        searchQuery = query
        applyFilters()
    }

    private fun applyFilters() {
        val bills = allBills.value ?: return
        var filtered = when (currentFilter) {
            "ACTIVE" -> bills.filter { !it.isSettled }
            "SETTLED" -> bills.filter { it.isSettled }
            else -> bills
        }
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
            }
        }
        _filteredBills.value = filtered
    }

    fun deleteBill(billId: Long) {
        viewModelScope.launch {
            repository.deleteBill(billId)
        }
    }

    fun markBillSettled(billId: Long) {
        viewModelScope.launch {
            repository.markBillSettled(billId)
        }
    }
}
