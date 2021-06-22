package com.yourapp.farmtrac

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.plusAssign
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.yourapp.farmtrac.databinding.ActivityMainBinding
import com.yourapp.farmtrac.navigation.KeepStateNavigator
import com.yourapp.farmtrac.settings.SettingsActivity
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var sharedPreference: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //set language
        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        val language = preference.getString("language", "English")
        val locale = Locale(language)
        val config = resources.configuration
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val navigator = KeepStateNavigator(this, navHostFragment.childFragmentManager, R.id.nav_host_fragment)
        navController.navigatorProvider += navigator
        navController.setGraph(R.navigation.bottom_navigation_graph)

        binding.bottomNavigationView.setupWithNavController(navController)

        val appBarConfiguration = AppBarConfiguration(topLevelDestinationIds = setOf(R.id.navigation_new_entry))
        val toolbar = binding.toolbar
        //toolbar.setupWithNavController(navController, appBarConfiguration)
        toolbar.setTitle(R.string.app_name)
        setSupportActionBar(toolbar)
        toolbar.showOverflowMenu()

        sharedPreference = getSharedPreferences(getString(com.yourapp.farmtrac.R.string.preference_file_key), Context.MODE_PRIVATE )
        var initialSettingsLaunch = sharedPreference.getBoolean("firstSettings", true)

        val updateLaunch = sharedPreference.getBoolean("firstLaunch", true)
        // Update alert dialog
        if(updateLaunch and !initialSettingsLaunch){
            GetLatestVersion().execute()
            editSharedPreference("firstLaunch", false)
        }

        // Handle bug
        if(initialSettingsLaunch){
            val editor = sharedPreference.edit()
            editor.putBoolean("firstSettings", false)
            editor.commit()
            initialSettingsLaunch = sharedPreference.getBoolean("firstSettings", false)
            goToSettings()
        }

    }

    private fun editSharedPreference(key : String, value : Boolean){
        val editor = sharedPreference.edit()
        editor.putBoolean(key, value)
        editor.commit()
    }

    override fun onBackPressed() {
        if(binding.bottomNavigationView.selectedItemId != R.id.navigation_new_entry){
            navController.navigate(R.id.navigation_new_entry)
            binding.bottomNavigationView.selectedItemId = R.id.navigation_new_entry
        }
        else{
            editSharedPreference("firstLaunch", true)
            super.onBackPressed()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.farm_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.farm_settings -> {
                goToSettings()
                true
            }

            else -> {
                Toast.makeText(this, "option not selected", Toast.LENGTH_SHORT).show()
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun goToSettings(){
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    // Update alert dialog
    fun updateAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.app_name))
        builder.setMessage(getString(R.string.update_message))
        builder.setCancelable(false)

        builder.setPositiveButton(getString(R.string.update)) { dialogInterface, _ ->
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$packageName")
                )
            )
            dialogInterface.dismiss()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
            dialogInterface.cancel()
        }

        builder.show()
    }

    //Get latest version using Jsoup
    inner class GetLatestVersion : AsyncTask<String, Unit, String>() {
        private lateinit var sLatestVersion : String

        override fun doInBackground(vararg p0: String?): String {
            try{
                sLatestVersion = Jsoup.connect(
                    "https://play.google.com/store/apps/details?id=com.yourapp.farmtrac"
                ).timeout(30000)
                    .get()
                    .select("div.hAyfc:nth-child(4) > span:nth-child(2) > div:nth-child(1) > span:nth-child(1)")
                    .first()
                    .ownText()
            }
            catch (e : IOException){
                sLatestVersion = ""
                //e.printStackTrace()
            }
            return sLatestVersion
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val sCurrentVersion : String = BuildConfig.VERSION_NAME

            if(sLatestVersion != ""){
                val cVersion = sCurrentVersion.toFloat()
                val lVersion = sLatestVersion.toFloat()

                if(lVersion > cVersion)
                    updateAlertDialog()
            }


        }

    }
}