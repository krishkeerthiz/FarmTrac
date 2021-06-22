package com.yourapp.farmtrac.ui.historyDetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yourapp.farmtrac.R
import com.yourapp.farmtrac.database.CustomerDatabase
import com.yourapp.farmtrac.database.dao.CustomerHistoryDao
import com.yourapp.farmtrac.databinding.FragmentHistoryDetailBinding
import com.yourapp.farmtrac.viewModel.Communicator
import kotlinx.coroutines.launch
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*

class HistoryDetailFragment : Fragment() {
    private lateinit var binding : FragmentHistoryDetailBinding
    private lateinit var customerHistoryDao: CustomerHistoryDao
    private val model : Communicator by activityViewModels()
    private lateinit var name : String
    private  var point : Int = 0
    private lateinit var date : Date
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        model.viewModelScope.launch {
            name = model.historyNameKey.value.toString()
            point = model.historyPointKey.value!!
            date = model.historyDateKey.value!!
        }
        return inflater.inflate(R.layout.fragment_history_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHistoryDetailBinding.bind(view)
        appbarVisibility()

        customerHistoryDao = CustomerDatabase.getInstance(requireActivity()).getCustomerHistoryDao()

        lifecycleScope.launch {
            val customerHistory = customerHistoryDao.getRecord(name, point, date)
            binding.nameTV.text = customerHistory.name
            binding.pointsTV.text = customerHistory.points.toString()
            binding.workTV.text = customerHistory.work
            binding.dateTV.text = SimpleDateFormat("dd-MM-yyyy", Locale.UK).format(customerHistory.date).toString()
        }

    }

    fun appbarVisibility(){
        val navBar = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        navBar?.visibility = View.GONE

        var toolBar = activity?.findViewById<Toolbar>(R.id.toolbar)
        toolBar?.visibility = View.VISIBLE
    }
}