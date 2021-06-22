package com.yourapp.farmtrac.ui.balanceDetail

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yourapp.farmtrac.R
import com.yourapp.farmtrac.database.CustomerDatabase
import com.yourapp.farmtrac.database.dao.CustomerBalanceDao
import com.yourapp.farmtrac.databinding.FragmentBalanceDetailBinding
import com.yourapp.farmtrac.viewModel.Communicator
import kotlinx.coroutines.launch

class BalanceDetailFragment : Fragment() {
    private lateinit var binding : FragmentBalanceDetailBinding
    //val args : BalanceDetailFragmentArgs by navArgs()
    private lateinit var customerBalanceDao: CustomerBalanceDao
    private val model : Communicator by activityViewModels()
    private lateinit var key : String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //model.balanceKey.observe(viewLifecycleOwner, Observer { key = it })
        model.viewModelScope.launch {
            val balanceKey = model.balanceKey
            key = balanceKey.value.toString()
        }
        return inflater.inflate(R.layout.fragment_balance_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appbarVisibility()

        binding = FragmentBalanceDetailBinding.bind(view)
        customerBalanceDao = CustomerDatabase.getInstance(requireActivity()).getCustomerBalanceDao()

        // Button Visibility
        val watcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if(binding.editTextAmount.text.toString().isNotEmpty()){
                    binding.payButton.isEnabled = true
                    binding.totalPayButton.isEnabled = false
                }
                else{
                    binding.payButton.isEnabled = false
                    binding.totalPayButton.isEnabled = true
                }
            }
        }

        binding.editTextAmount.addTextChangedListener(watcher)

        lifecycleScope.launch {
            //Toast.makeText(requireActivity(), key, Toast.LENGTH_SHORT).show()
            val customerBalance = customerBalanceDao.getByName(key)
            binding.nameTV1.text = customerBalance.name
            binding.amountTV1.text = customerBalance.amount.toString()

            //set pay amount
            binding.totalPayButton.text = "₹${customerBalance.amount} " + getString(R.string.paid)

            // Total pay
            binding.totalPayButton.setOnClickListener {
                Toast.makeText(requireActivity(), "₹${customerBalance.amount} " + getString(R.string.paid), Toast.LENGTH_SHORT).show()
                lifecycleScope.launch {
                    customerBalanceDao.delete(customerBalance)
                }
                view.findNavController().navigate(R.id.navigation_balance)
            }

            // Partial pay
            binding.payButton.setOnClickListener {
                val partialAmount = binding.editTextAmount.text.toString().toInt()

                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken,0)

                if(partialAmount != 0){
                    Toast.makeText(requireActivity(), "₹$partialAmount " + getString(R.string.paid), Toast.LENGTH_SHORT).show()
                    lifecycleScope.launch {
                        if(partialAmount == customerBalance.amount)
                            customerBalanceDao.delete(customerBalance)
                        else
                            customerBalanceDao.updateAmount(key, customerBalance.amount - partialAmount)
                    }
                    binding.editTextAmount.text.clear()
                    view.findNavController().navigate(R.id.navigation_balance)
                }
                else{
                    Toast.makeText(requireActivity(), getString(R.string.invalid_amount), Toast.LENGTH_SHORT).show()
                    binding.editTextAmount.text.clear()
                }

            }
        }

    }

    fun appbarVisibility(){
        val navBar = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        navBar?.visibility = View.GONE

        var toolBar = activity?.findViewById<Toolbar>(R.id.toolbar)
        toolBar?.visibility = View.VISIBLE
    }


}