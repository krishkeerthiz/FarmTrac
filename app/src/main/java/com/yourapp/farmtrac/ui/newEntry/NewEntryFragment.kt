package com.yourapp.farmtrac.ui.newEntry

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yourapp.farmtrac.R
import com.yourapp.farmtrac.database.CustomerDatabase
import com.yourapp.farmtrac.database.entity.Customer
import com.yourapp.farmtrac.database.entity.CustomerBalance
import com.yourapp.farmtrac.database.entity.CustomerHistory
import com.yourapp.farmtrac.databinding.FragmentNewEntryBinding
import com.yourapp.farmtrac.settings.SettingsActivity
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


class NewEntryFragment : Fragment() {
    private lateinit var binding : FragmentNewEntryBinding
    private val REQUEST_CODE = 123
    private lateinit var customerNames : List<String>
    private lateinit var preference: SharedPreferences
    private var pointPrice by Delegates.notNull<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_entry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNewEntryBinding.bind(view)

        // Settings Preference
        preference = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        pointPrice = preference.getString("points", "100")?.toInt() ?: 100

        // Shared Preference
        val sharedPreference = activity?.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE ) ?: return
        val firstTime = sharedPreference.getBoolean(getString(R.string.first_time) , true)
        appbarVisibility()

        // Welcome Screen
        if(firstTime)
        {
            val action = NewEntryFragmentDirections.actionNavigationNewEntryToWelcomeFragment()
            view.findNavController().navigate(action)
        }

        // Dropdown spinner
        val spinner = binding.workSpinner
        val spinnerAdapter =
            context?.let { ArrayAdapter(it, android.R.layout.simple_dropdown_item_1line , resources.getStringArray(R.array.works)) }
        spinner.adapter = spinnerAdapter

        // AutoComplete Textview
        val autoCompleteTextView = binding.editTextName
        val customerDao = CustomerDatabase.getInstance(requireActivity()).getCustomerDao()

        lifecycleScope.launch {
            customerNames = customerDao.getNames()
            val autoCompleteAdapter = context?.let{ ArrayAdapter(it, android.R.layout.select_dialog_item, customerNames)}
            autoCompleteTextView.setAdapter(autoCompleteAdapter)
        }
        autoCompleteTextView.threshold = 1

        //Mic
        binding.micButton.setOnClickListener{
            startSpeechRecognitionActivity()
        }

        // Submit button visibility
        val watcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                binding.submitButton.isEnabled = !(binding.editTextName.text.toString().isEmpty() ||
                        binding.editTextPoints.text.toString().isEmpty())
            }
        }

        binding.editTextName.addTextChangedListener(watcher)
        binding.editTextPoints.addTextChangedListener(watcher)


        // Room dao
        //val customerDao = CustomerDatabase.getInstance(requireActivity()).getCustomerDao()
        val customerHistoryDao = CustomerDatabase.getInstance(requireActivity()).getCustomerHistoryDao()
        val customerBalanceDao = CustomerDatabase.getInstance(requireActivity()).getCustomerBalanceDao()

        binding.submitButton.setOnClickListener{

            val name = binding.editTextName.text.toString().capitalize()
            val points = binding.editTextPoints.text.toString().toInt()
            val type = spinner.selectedItem.toString()
            var millis = System.currentTimeMillis()

            if (points != 0) {
                lifecycleScope.launch {
                    val customerNames = customerDao.getNames()
                    val customerBalanceNames = customerBalanceDao.getNames()

                    if(name !in customerNames)
                        customerDao.insert(Customer(name = name))

                    if(!binding.paidCheckBox.isChecked){
                        if(name !in customerBalanceNames){
                            val customerBalance = CustomerBalance(name = name, amount = price(points))
                            customerBalanceDao.insert(customerBalance)
                        }
                        else{
                            val customerBalance = customerBalanceDao.getByName(name = name)
                            val amount = customerBalance.amount
                            val updatedCustomerBalance = customerBalanceDao.updateAmount(name = name, amount = amount + price(points) )
                            // or customerBalance.amount += price(points)
                        }
                    }
                    val customerHistory = CustomerHistory(name = name, work = type, points = points, date = java.sql.Date(millis))
                    customerHistoryDao.insert(customerHistory)

                    Toast.makeText(requireActivity(), getString(R.string.submitted) , Toast.LENGTH_SHORT).show()
                    binding.editTextName.text.clear()
                    binding.editTextPoints.text.clear()
                }
            }
            else{
                Toast.makeText(requireActivity(), getString(R.string.invalid_point), Toast.LENGTH_SHORT).show()
                binding.editTextPoints.text.clear()
            }
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken,0)


        }
    }

    override fun onStart() {
        super.onStart()
        pointPrice = preference.getString("points", "100")?.toInt() ?: 100
    }


    private fun startSpeechRecognitionActivity(){
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
                binding.editTextName.setText(message[0])

        }
    }

    fun appbarVisibility(){
        val navBar = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        navBar?.visibility = View.VISIBLE

        val toolBar = activity?.findViewById<Toolbar>(R.id.toolbar)
        toolBar?.visibility = View.VISIBLE
    }

    fun price(point : Int) = point*pointPrice
}