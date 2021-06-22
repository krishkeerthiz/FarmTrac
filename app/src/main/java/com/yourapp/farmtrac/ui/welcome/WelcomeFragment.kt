package com.yourapp.farmtrac.ui.welcome

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yourapp.farmtrac.R
import com.yourapp.farmtrac.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {
    private lateinit var binding : FragmentWelcomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWelcomeBinding.bind(view)

        val sharedPreference = activity?.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE) ?: return

        binding.nextButton.setOnClickListener{
            with(sharedPreference.edit()){
                putBoolean(getString(R.string.first_time), false)
                apply()
            }
            //val action = WelcomeFragmentDirections.actionWelcomeFragmentToNavigationNewEntry()
            view.findNavController().navigate(R.id.navigation_new_entry)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        appbarVisibility()
    }

    fun appbarVisibility(){
        val navBar = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        navBar?.visibility = View.GONE

        var toolBar = activity?.findViewById<Toolbar>(R.id.toolbar)
        toolBar?.visibility = View.GONE
    }
}