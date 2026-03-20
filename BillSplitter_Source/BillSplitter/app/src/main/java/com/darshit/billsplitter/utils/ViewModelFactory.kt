package com.darshit.billsplitter.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.darshit.billsplitter.data.repository.BillRepository
import com.darshit.billsplitter.ui.addbill.AddBillViewModel
import com.darshit.billsplitter.ui.history.HistoryViewModel
import com.darshit.billsplitter.ui.home.HomeViewModel
import com.darshit.billsplitter.ui.settings.BillDetailViewModel

class ViewModelFactory(private val repository: BillRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(repository) as T
            modelClass.isAssignableFrom(AddBillViewModel::class.java) ->
                AddBillViewModel(repository) as T
            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(repository) as T
            modelClass.isAssignableFrom(BillDetailViewModel::class.java) ->
                BillDetailViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
