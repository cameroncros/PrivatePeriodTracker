package com.cross.privateperiodtracker

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import com.cross.privateperiodtracker.theme.PrivatePeriodTrackerTheme
import com.cross.privateperiodtracker.theme.Typography

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PrivatePeriodTrackerTheme {
                Settings(exitFn = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(exitFn: () -> Unit) {
    val context = LocalContext.current
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.settings),
                    style = Typography.headlineMedium
                )
            },
            navigationIcon = {
                IconButton(onClick = { exitFn() }) {
                    Icon(Icons.Filled.ArrowBack, "Back")
                }
            }
        )
    }, content = { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(1f)
        ) {
            NotificationSettingsCard(prefs)
            AutoPopulateSettingsCard(prefs)
        }
    })
}


@Composable
private fun NotificationSettingsCard(prefs: SharedPreferences) {
    var notifications by remember { mutableStateOf(SettingsManager.checkNotificationEnabled(prefs)) }
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(1f)
    ) {
        Text(
            text = stringResource(id = R.string.notification_settings),
            modifier = Modifier.padding(16.dp),
            style = Typography.headlineSmall
        )

        SettingsSwitch(str = stringResource(id = R.string.notify_on_day),
            checked = notifications[0],
            toggleFn = { b: Boolean ->
                val tempNot = notifications.toMutableList()
                tempNot[0] = b
                notifications = tempNot.toList()
                SettingsManager.setNotificationEnabled(prefs, notifications)
            })

        for (i in 2..7) {
            SettingsSwitch(str = stringResource(id = R.string.notify_on_day_X, i),
                checked = notifications[i],
                toggleFn = { b: Boolean ->
                    val tempNot = notifications.toMutableList()
                    tempNot[i] = b
                    notifications = tempNot.toList()
                    SettingsManager.setNotificationEnabled(prefs, notifications)
                }
            )
        }
    }
}

@Composable
private fun AutoPopulateSettingsCard(prefs: SharedPreferences) {
    var autoPopulateEnabled by remember { mutableStateOf(SettingsManager.checkAutoEndPeriod(prefs)) }
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(1f)
    ) {
        Text(
            text = "Auto Estimate",
            modifier = Modifier.padding(16.dp),
            style = Typography.headlineSmall

        )

        SettingsSwitch(str = stringResource(id = R.string.auto_end_period),
            checked = autoPopulateEnabled,
            toggleFn = { b: Boolean ->
                SettingsManager.setAutoEndPeriod(prefs, b)
                autoPopulateEnabled = b
            })

        if (autoPopulateEnabled) {
            SettingsNumberPicker(str = stringResource(id = R.string.auto_end_period_after),
                value = SettingsManager.getAutoEndPeriodDays(prefs),
                suffix = stringResource(id = R.string.days),
                updateFn = { i: Long ->
                    SettingsManager.setAutoEndPeriodDays(prefs, i)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    PrivatePeriodTrackerTheme {
        Settings(exitFn = {})
    }
}

@Composable
fun SettingsSwitch(str: String, checked: Boolean, toggleFn: (b: Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(str)
        Switch(
            checked = checked,
            onCheckedChange = toggleFn
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsSwitchPreview() {
    PrivatePeriodTrackerTheme {
        SettingsSwitch(str = "Setting Label", checked = true, toggleFn = {})
    }
}

@Composable
fun SettingsNumberPicker(str: String, value: Long, suffix: String?, updateFn: (b: Long) -> Unit) {
    var number by remember { mutableStateOf("$value") }
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(str)

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                modifier = Modifier.fillMaxWidth(0.5f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                value = number,
                singleLine = true,
                onValueChange = { s ->
                    number = s
                    try {
                        updateFn(number.toLong())
                    } catch (_: NumberFormatException) {
                    }
                }
            )
            if (suffix != null) {
                Text(suffix)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsNumberPickerPreview() {
    PrivatePeriodTrackerTheme {
        SettingsNumberPicker(str = "Update After", value = 7, suffix = "days", updateFn = {})
    }
}