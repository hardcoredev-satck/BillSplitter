package com.darshit.billsplitter.ui.addbill

import androidx.lifecycle.*
import com.darshit.billsplitter.data.model.Bill
import com.darshit.billsplitter.data.model.Participant
import com.darshit.billsplitter.data.repository.BillRepository
import kotlinx.coroutines.launch

class AddBillViewModel(private val repository: BillRepository) : ViewModel() {

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _recentParticipants = MutableLiveData<List<String>>()
    val recentParticipants: LiveData<List<String>> = _recentParticipants

    init {
        loadRecentParticipants()
    }

    private fun loadRecentParticipants() {
        viewModelScope.launch {
            _recentParticipants.value = repository.getAllParticipantNames()
        }
    }

    fun saveBill(
        title: String,
        description: String,
        totalAmount: Double,
        currency: String,
        category: String,
        date: Long,
        splitType: String,
        paidByName: String,
        participantNames: List<String>,
        customAmounts: Map<String, Double>? = null
    ) {
        if (title.isBlank()) {
            _errorMessage.value = "Please enter a bill title"
            return
        }
        if (totalAmount <= 0) {
            _errorMessage.value = "Please enter a valid amount"
            return
        }
        if (participantNames.isEmpty()) {
            _errorMessage.value = "Please add at least one participant"
            return
        }
        if (paidByName.isBlank()) {
            _errorMessage.value = "Please select who paid"
            return
        }

        val colors = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
        val participants = when (splitType) {
            "EQUAL" -> {
                val shareAmount = totalAmount / participantNames.size
                participantNames.mapIndexed { i, name ->
                    Participant(
                        billId = 0,
                        name = name,
                        shareAmount = Math.round(shareAmount * 100.0) / 100.0,
                        sharePercentage = 100.0 / participantNames.size,
                        avatarColor = colors[i % colors.size]
                    )
                }
            }
            "CUSTOM" -> {
                participantNames.mapIndexed { i, name ->
                    val amount = customAmounts?.get(name) ?: (totalAmount / participantNames.size)
                    Participant(
                        billId = 0,
                        name = name,
                        shareAmount = Math.round(amount * 100.0) / 100.0,
                        sharePercentage = (amount / totalAmount) * 100,
                        avatarColor = colors[i % colors.size]
                    )
                }
            }
            "PERCENTAGE" -> {
                val equalPercent = 100.0 / participantNames.size
                participantNames.mapIndexed { i, name ->
                    val percent = customAmounts?.get(name) ?: equalPercent
                    val amount = (percent / 100.0) * totalAmount
                    Participant(
                        billId = 0,
                        name = name,
                        shareAmount = Math.round(amount * 100.0) / 100.0,
                        sharePercentage = percent,
                        avatarColor = colors[i % colors.size]
                    )
                }
            }
            else -> emptyList()
        }

        val bill = Bill(
            title = title,
            description = description,
            totalAmount = totalAmount,
            currency = currency,
            category = category,
            date = date,
            splitType = splitType,
            paidByName = paidByName
        )

        viewModelScope.launch {
            try {
                repository.insertBill(bill, participants)
                _saveSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save bill: ${e.message}"
            }
        }
    }
}
