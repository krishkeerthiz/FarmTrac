package com.yourapp.farmtrac.ui.history

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.yourapp.farmtrac.R
import com.yourapp.farmtrac.database.CustomerDatabase
import com.yourapp.farmtrac.database.dao.CustomerHistoryDao
import com.yourapp.farmtrac.databinding.FragmentHistoryBinding
import com.yourapp.farmtrac.listAdapter.HistoryListAdapter
import com.yourapp.farmtrac.viewModel.Communicator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {
    private lateinit var customerHistoryDao: CustomerHistoryDao
    private lateinit var binding : FragmentHistoryBinding
    private val model : Communicator by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHistoryBinding.bind(view)

        customerHistoryDao = CustomerDatabase.getInstance(requireActivity()).getCustomerHistoryDao()
        val customerDao = CustomerDatabase.getInstance(requireActivity()).getCustomerDao()

        //Set empty textview for list
        binding.historyListView.emptyView = binding.historyEmptyTextView

        // AutoComplete Text View
        val autoCompleteTextView = binding.autoCompleteTextView
        lifecycleScope.launch {
            val names = customerDao.getNames()
            val autoCompleteAdapter = context?.let{ArrayAdapter(it, android.R.layout.select_dialog_item, names)}
            autoCompleteTextView.setAdapter(autoCompleteAdapter)
        }
        autoCompleteTextView.threshold = 1

        var adapter : HistoryListAdapter
        var customerHistoryListView = binding.historyListView

        lifecycleScope.launch {
            val names = customerHistoryDao.getNames().reversed()
            val points = customerHistoryDao.getPoints().reversed()
            val dates = customerHistoryDao.getDates().reversed()
            var formattedDates = mutableListOf<String>()
            //Toast.makeText(requireActivity(), "${dates[0]}", Toast.LENGTH_LONG).show()
            for(date in dates)
                formattedDates.add(SimpleDateFormat("dd-MM-yyyy", Locale.UK).format(date))

            adapter = HistoryListAdapter(requireActivity(), names, points, formattedDates)
            customerHistoryListView.adapter = adapter
            customerHistoryListView.setOnItemClickListener { parent, listView, position, id ->
                //val name = adapter.getItem(position).toString()
                val name = names[position]
                val point = points[position]
                val date = dates[position]
                model.setHistoryKey(name, point, date)

                view.findNavController().navigate(R.id.historyDetailFragment)
                }
            }

        binding.datePicker.setOnClickListener {
            val calender = Calendar.getInstance()
            val year = calender.get(Calendar.YEAR)
            val month = calender.get(Calendar.MONTH)
            val day = calender.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireActivity(), DatePickerDialog.OnDateSetListener{ view, year, monthOfYear, dayOfMonth ->
                //Toast.makeText(requireActivity(), "Date: $year/$monthOfYear/$dayOfMonth", Toast.LENGTH_SHORT).show()

                var names = mutableListOf<String>()
                var points = mutableListOf<Int>()
                var dates = mutableListOf<java.sql.Date>()
                var formattedDates = mutableListOf<String>()

                lifecycleScope.launch {
                    val records = customerHistoryDao.getAll()
                    for(record in records){
                        val date = record.date
                        if (date != null) {
                            if((date.year == (year-1900)) and (date.month == monthOfYear) and (date.date == dayOfMonth)) {
                                names.add(record.name)
                                points.add(record.points)
                                record.date?.let { it1 -> dates.add(it1) }
                                formattedDates.add(SimpleDateFormat("dd-MM-yyyy", Locale.UK).format(record.date))
                                }
                            }
                    }

                    adapter = HistoryListAdapter(requireActivity(), names, points, formattedDates)
                    customerHistoryListView.adapter = adapter
                    customerHistoryListView.setOnItemClickListener { parent, listView, position, id ->
                        //val name = adapter.getItem(position).toString()
                        val name = names[position]
                        val point = points[position]
                        val date = dates[position]
                        model.setHistoryKey(name, point, date)

                        requireView().findNavController().navigate(R.id.historyDetailFragment)
                    }
                }
            }, year, month, day)
            datePickerDialog.show()
        }


        //Ok button
        binding.button.setOnClickListener {
            val name = binding.autoCompleteTextView.text.toString()
            var names = mutableListOf<String>()
            var points = mutableListOf<Int>()
            var dates = mutableListOf<java.sql.Date>()
            var formattedDates = mutableListOf<String>()

            lifecycleScope.launch {
                val records = customerHistoryDao.getByName(name)
                for(record in records) {
                    names.add(record.name)
                    points.add(record.points)
                    record.date?.let { it1 -> dates.add(it1) }
                }
                for(date in dates)
                    formattedDates.add(SimpleDateFormat("dd-MM-yyyy", Locale.UK).format(date))

                adapter = HistoryListAdapter(requireActivity(), names, points, formattedDates)
                customerHistoryListView.adapter = adapter
                customerHistoryListView.setOnItemClickListener { parent, listView, position, id ->
                    //val name = adapter.getItem(position).toString()
                    val name = names[position]
                    val point = points[position]
                    val date = dates[position]
                    model.setHistoryKey(name, point, date)

                    view.findNavController().navigate(R.id.historyDetailFragment)
                }
            }
            // hide keyboard
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken,0)
            binding.autoCompleteTextView.text.clear()

            }


        // ok button visibility
        val watcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                binding.button.isEnabled = binding.autoCompleteTextView.text.toString().isNotEmpty()
            }
        }
        binding.autoCompleteTextView.addTextChangedListener(watcher)



        }
    }