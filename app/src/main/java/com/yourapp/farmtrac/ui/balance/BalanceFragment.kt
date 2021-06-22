package com.yourapp.farmtrac.ui.balance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.yourapp.farmtrac.R
import com.yourapp.farmtrac.database.CustomerDatabase
import com.yourapp.farmtrac.database.dao.CustomerBalanceDao
import com.yourapp.farmtrac.databinding.FragmentBalanceBinding
import com.yourapp.farmtrac.listAdapter.BalanceListAdapter
import com.yourapp.farmtrac.viewModel.Communicator
import kotlinx.coroutines.launch

class BalanceFragment : Fragment() {
    private val REQUEST_CODE = 123
    private lateinit var binding : FragmentBalanceBinding
    private lateinit var customerBalanceDao : CustomerBalanceDao
    private val model : Communicator by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_balance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBalanceBinding.bind(view)

        //Mic
        binding.micButton.setOnClickListener{
            startSpeechRecognitionActivity()
        }

        //Set list empty textview
        binding.balanceListView.emptyView = binding.balanceEmptyTextView

        customerBalanceDao = CustomerDatabase.getInstance(requireActivity()).getCustomerBalanceDao()

        // AutoCompleteTextView
        val autoCompleteTextView = binding.editTextSearch
        lifecycleScope.launch {
            val names = customerBalanceDao.getNames()
            val autoCompleteAdapter = context?.let{ArrayAdapter(it, android.R.layout.select_dialog_item, names)}
            autoCompleteTextView.setAdapter(autoCompleteAdapter)
        }
        autoCompleteTextView.threshold = 1

        //ListView
        var adapter : BalanceListAdapter
        var customerBalanceListView = binding.balanceListView

        //List items
        lifecycleScope.launch {
            val names = customerBalanceDao.getNames().reversed()
            val amounts = customerBalanceDao.getAmounts().reversed()
//            var formattedDates = mutableListOf<String>()
//            for(date in dates)
//                formattedDates.add(SimpleDateFormat("dd-MM-yyyy", Locale.UK).format(date))

            adapter = BalanceListAdapter(requireActivity(), names, amounts)
            customerBalanceListView.adapter = adapter

            customerBalanceListView.setOnItemClickListener { parent, listView, position, id ->
                val key = adapter.getItem(position).toString()
                model.setBalanceKey(key)
                view.findNavController().navigate(R.id.balanceDetailFragment)
            }
        }

        //Ok button
        binding.buttonOK.setOnClickListener {
            val name = binding.editTextSearch.text.toString()
            val names = mutableListOf<String>()
            val amounts = mutableListOf<Int>()

            lifecycleScope.launch {
                val record = customerBalanceDao.getByName(name)
                if(record != null){
                    names.add(record.name)
                    amounts.add(record.amount)
                }

                adapter = BalanceListAdapter(requireActivity(), names, amounts)
                customerBalanceListView.adapter = adapter

                customerBalanceListView.setOnItemClickListener { parent, listView, position, id ->
                    val key = adapter.getItem(position).toString()
                    model.setBalanceKey(key)
                    view.findNavController().navigate(R.id.balanceDetailFragment)
                }

            }
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken,0)
            binding.editTextSearch.text.clear()
        }

        // Ok button visibility
        val watcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                binding.buttonOK.isEnabled = binding.editTextSearch.text.toString().isNotEmpty()
            }
        }
        binding.editTextSearch.addTextChangedListener(watcher)

    }

    fun startSpeechRecognitionActivity(){
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val preference = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        val language = preference.getString("language", "English")

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
            //intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.package)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice searching...")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer.startListening(intent)
        if(activity?.packageManager?.let { intent.resolveActivity(it) } != null)
            startActivityForResult(intent, REQUEST_CODE)

        speechRecognizer.stopListening()
        speechRecognizer.destroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard
            val message  = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (message != null)
                binding.editTextSearch.setText(message[0])

        }
    }

}