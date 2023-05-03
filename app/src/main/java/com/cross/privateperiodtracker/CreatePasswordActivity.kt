package com.cross.privateperiodtracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.cross.privateperiodtracker.data.generateData
import com.cross.privateperiodtracker.lib.DataManager
import com.cross.privateperiodtracker.lib.Encryptor
import com.cross.privateperiodtracker.theme.PrivatePeriodTrackerTheme
import com.cross.privateperiodtracker.theme.Typography

class CreatePasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PrivatePeriodTrackerTheme {
                CreatePassword(saveFn = { pw: String, duress: String ->
                    val encryptor = Encryptor(pw)
                    DataManager(
                        this@CreatePasswordActivity.applicationContext,
                        encryptor
                    ).saveData()

                    if (duress.isNotEmpty()) {
                        val canaryEncryptor = Encryptor(duress)
                        val dataManager = DataManager(
                            this@CreatePasswordActivity.applicationContext,
                            canaryEncryptor
                        )
                        dataManager.data = generateData()
                        dataManager.saveData()
                    }
                    finish()
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePassword(
    saveFn: (pw: String, duress: String) -> Unit
) {
    val context = LocalContext.current
    val priv_policy_url = stringResource(id = R.string.priv_policy_url)
    var pw by remember { mutableStateOf("") }
    var duress by remember { mutableStateOf("") }
    Column {
        Text(
            style = Typography.bodyLarge,
            text = stringResource(R.string.welcome_to_the_privacy_period_tracker),
            modifier = Modifier.padding(8.dp)
        )
        Text(
            style = Typography.bodySmall,
            text = stringResource(R.string.set_the_password_you_wish_to_use_to_encrypt_your_data_with_longer_is_better_and_avoid_re_using_an_existing_password_if_left_blank_no_password_will_be_required_to_access_your_data),
            modifier = Modifier.padding(8.dp)
        )
        TextField(
            value = pw,
            onValueChange = { s ->
                pw = s
            },
            Modifier
                .fillMaxWidth(1f)
                .padding(8.dp)
                .testTag("password")
        )
        Text(
            style = Typography.bodySmall,
            text = stringResource(R.string.choose_a_quot_duress_quot_password_this_is_a_password_that_when_supplied_will_silently_generate_fake_data_that_will_appear_real_and_will_delete_your_actual_data),
            modifier = Modifier.padding(8.dp)
        )
        TextField(
            value = duress,
            onValueChange = { s ->
                duress = s
            },
            Modifier
                .fillMaxWidth(1f)
                .padding(8.dp)
                .testTag("duress")
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                modifier = Modifier.padding(8.dp),
                onClick = {
                    val uri = Uri.parse(priv_policy_url)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(context, intent, null)
                }
            ) {
                Text(
                    stringResource(id = R.string.privacy_policy)
                )
            }

            Button(
                onClick = { saveFn(pw, duress) },
                modifier = Modifier.padding(8.dp),
            ) {
                Text(stringResource(id = R.string.save))
                Icon(Icons.Filled.Save, "Save")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreatePasswordPreview() {
    PrivatePeriodTrackerTheme {
        CreatePassword { _: String, _: String -> }
    }
}