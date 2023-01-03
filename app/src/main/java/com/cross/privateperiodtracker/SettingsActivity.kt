package com.cross.privateperiodtracker

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private fun updatePrefs(sharedPreferences: SharedPreferences) {
            val notificationEnabled = sharedPreferences.getBoolean("enabled", true)

            findPreference<Preference>("7days")?.isEnabled = notificationEnabled
            findPreference<Preference>("6days")?.isEnabled = notificationEnabled
            findPreference<Preference>("5days")?.isEnabled = notificationEnabled
            findPreference<Preference>("4days")?.isEnabled = notificationEnabled
            findPreference<Preference>("3days")?.isEnabled = notificationEnabled
            findPreference<Preference>("2days")?.isEnabled = notificationEnabled
            findPreference<Preference>("1days")?.isEnabled = notificationEnabled
            findPreference<Preference>("0days")?.isEnabled = notificationEnabled
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            sharedPreferences.registerOnSharedPreferenceChangeListener { sp: SharedPreferences, _: String ->
                updatePrefs(sp)
            }

            updatePrefs(sharedPreferences)
        }
    }
}