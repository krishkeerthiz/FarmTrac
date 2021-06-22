package com.yourapp.farmtrac.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.sql.Date

class Communicator : ViewModel() {
    val balanceKey = MutableLiveData<String>()
    val historyNameKey = MutableLiveData<String>()
    val historyPointKey = MutableLiveData<Int>()
    val historyDateKey = MutableLiveData<Date>()

    fun setBalanceKey(key : String ){
        balanceKey.value = key
    }

    fun setHistoryKey(name : String, point : Int, date : Date){
        historyNameKey.value = name
        historyPointKey.value = point
        historyDateKey.value = date
    }

}