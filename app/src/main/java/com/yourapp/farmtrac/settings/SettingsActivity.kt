package com.yourapp.farmtrac.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.yourapp.farmtrac.MainActivity
import com.yourapp.farmtrac.R
import com.yourapp.farmtrac.databinding.ActivitySettingsBinding
import java.util.*


class SettingsActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySettingsBinding
    private lateinit var listener : SharedPreferences.OnSharedPreferenceChangeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if(savedInstanceState == null){
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }

        setSupportActionBar(binding.settingsToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // on preference change listener
        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        val pointPrice = preference.getString("points", "100")?.toInt() ?: 100

        listener = SharedPreferences.OnSharedPreferenceChangeListener(){ sharedPreferences: SharedPreferences, key: String ->

            if(key == "points"){
                val price = preference.getString("points", "100")?.toIntOrNull()
                if(price == null){
                    Toast.makeText(this, getString(R.string.invalid_point_price), Toast.LENGTH_LONG).show()
                    val editor = preference.edit()
                    editor.putString("points", pointPrice.toString())
                    editor.commit()
                }
            }

            if(key == "language"){
                //Toast.makeText(this, "working ", Toast.LENGTH_SHORT).show()
                val language = preference.getString("language", "English")
                val locale = Locale(language)
                val config = resources.configuration
                config.locale = locale
                resources.updateConfiguration(config, resources.displayMetrics)

                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)

            }
        }
        preference.registerOnSharedPreferenceChangeListener(listener)

    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}